package com.capstone.pacetime.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.enums.BreathState;
import com.capstone.pacetime.data.enums.RunningDataType;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BreathReceiver implements ReceiverLifeCycleInterface {
    private static final String TAG = "BreathReceiver";

    private Handler dataHandler;

    private final AudioRecord audioRecord;

    private final int AUDIO_SAMP_RATE = 44100;
    private final float recordSeconds = 0.5f;
    private final short[] bufferRecord;
    private final int bufferRecordSize;
    private final int MAX_QUEUE_SIZE = 11025 * 3;

    private final ConcurrentLinkedQueue<Short> soundQueue;

    private Handler
            receiveHandler,
            soundToBreathHandler,
            saveSoundHandler
    ;
    private HandlerThread
            soundToBreathThread,
            saveSoundThread
    ;
    private Thread receiveThread;
    private SoundReceiveRunnable receiveRunnable;

    class SoundReceiveRunnable implements Runnable{
        boolean isRunning = false;
        boolean paused = false;
        final Object pauseLock = new Object();

        @Override
        public void run() {
            isRunning = true;
            saveSoundThread.start();
            soundToBreathThread.start();

            audioRecord.startRecording();

            Log.d(TAG, "Breath Thread Start");

            while (isRunning) {
                synchronized (pauseLock) {
                    if (!isRunning) {
                        break;
                    }
                    if (paused) {
                        try {
                            Log.d(TAG, "Breath Thread Paused");
                            pauseLock.wait();
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                int size = audioRecord.read(bufferRecord, 0, bufferRecordSize);
                if(size < 0){
                    continue;
                }
//                Log.i(TAG, "BufferSize: " + size);
                saveSoundHandler.post(new SaveSoundRunnable(Arrays.copyOfRange(bufferRecord, 0, size)));
            }

            Log.d(TAG, "Breath Thread Interrupted");
            saveSoundThread.interrupt();
            soundToBreathThread.interrupt();
        }

        public void stop(){
            isRunning = false;
        }

        public void pause(){
            paused = true;
        }

        public void resume(){
            synchronized (pauseLock){
                paused = false;
                pauseLock.notifyAll();
            }
        }
    }

    class SaveSoundThread extends HandlerThread{

        public SaveSoundThread(String name) {
            super(name);
        }

        @Override
        public synchronized void start() {
            super.start();
            saveSoundHandler = new Handler(getLooper());
        }

        @Override
        public void interrupt() {
            super.interrupt();
            saveSoundHandler = null;
        }
    }

    class SaveSoundRunnable implements Runnable{
        private final short[] buffer;

        public SaveSoundRunnable(short[] buffer){
            this.buffer = buffer;
        }

        @Override
        public void run(){
            for(short val : buffer){
                synchronized (soundQueue){
                    soundQueue.add(val);
                    if(soundQueue.size() > AUDIO_SAMP_RATE * recordSeconds){
                        soundQueue.poll();
                    }
//                    soundQueue.put(val);


                }
            }
        }
    }

    public void doConvert(long timestamp){
        if(soundQueue.size() >= 22050){
            soundToBreathHandler.post(new SoundToBreathRunnable(soundQueue.toArray(), timestamp));

        }
        else {
            Log.d(TAG, "SoundQueue is Empty");
        }
    }

    class SoundToBreathThread extends HandlerThread{

        public SoundToBreathThread(String name) {
            super(name);
        }

        @Override
        public synchronized void start() {
            super.start();
            soundToBreathHandler = new Handler(getLooper());
        }

        @Override
        public void interrupt() {
            super.interrupt();
            soundToBreathHandler = null;
        }
    }

    private PytorchModule module;

    static class PytorchModule{
        private Module module;

        PytorchModule(String path){
            if(module == null){
                if(path == null || path.isEmpty()){
                    return;
                }
                module = Module.load(path);
            }

        }
        BreathState convert(FloatBuffer buf){
            Tensor inputTensor = Tensor.fromBlob(buf, new long[]{1, 22050});
            Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();

            float val = outputTensor.getDataAsFloatArray()[0];
            if(val <= 0.5){
//                Log.d(TAG, "Breath: EXHALE");
                return BreathState.EXHALE;
            }
            else{
//                Log.d(TAG, "Breath: INHALE");
                return BreathState.INHALE;
            }
        }
    }

    class SoundToBreathRunnable implements Runnable{
        private final Short[] sound;
        private final long timestamp;
//        private final IntBuffer sound;

        public SoundToBreathRunnable(Object[] sound, long timestamp){
            this.sound = new Short[22050]; // 22050

            Log.i(TAG, "S2B soundSize : " + sound.length);

            int lim = Math.min(sound.length, 22050);

            for(int i = 0; i < lim; i++){
                this.sound[i] = (Short)sound[i];
            }

            this.timestamp = timestamp;
        }
//        public SoundToBreathRunnable(int offset, long timestamp){
//            int[] soundBuffer = new int[22050];
//
//            soundQueue.get(soundBuffer, offset, 22050);
//            sound = IntBuffer.wrap(soundBuffer);
//
//            this.timestamp = timestamp;
//        }
        @Override
        public void run() {
            // TODO: Pytorch Implementation Required
            FloatBuffer buf = ByteBuffer.allocateDirect(22050*4).order(ByteOrder.nativeOrder()).asFloatBuffer();

            for(Short s : sound){
                buf.put(s);
            }

            Message msg = new Message();
//            msg.obj = new Breath(module.convert(buf), timestamp);
            msg.obj = new Breath(module.convert(buf), timestamp);
            msg.arg1 = RunningDataType.BREATH.ordinal();
            dataHandler.sendMessage(msg);
        }
    }

    public static String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    @SuppressLint("MissingPermission")
    public BreathReceiver(AudioManager audioManager, Context context){
        try {
            String asset = "CpuOptmodel.pt";
            File file = new File(context.getFilesDir(), asset);
            InputStream inStream = context.getAssets().open(asset);

            FileOutputStream outStream = new FileOutputStream(file, false);
            byte[] byteBuffer = new byte[4 * 1024];

            int read;

            while (true) {
                read = inStream.read(byteBuffer);
                if (read == -1) {
                    break;
                }
                outStream.write(byteBuffer, 0, read);
            }
            outStream.flush();
            module = new PytorchModule(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            module = null;
        }

        bufferRecordSize = AudioRecord.getMinBufferSize(
                AUDIO_SAMP_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT
        );

        bufferRecord = new short[bufferRecordSize+1];

        soundQueue = new ConcurrentLinkedQueue<>();
//        soundQueue = ByteBuffer.allocateDirect(22050*10).asIntBuffer();
//        soundQueue.limit(22050);

        audioRecord = new AudioRecord(
//                MediaRecorder.AudioSource.MIC,
                AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                AUDIO_SAMP_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferRecordSize);

        this.dataHandler = null;

        setPipeline();
    }

    public void setDataHandler(Handler dataHandler){
        this.dataHandler = dataHandler;
    }

    private void setPipeline(){
        receiveRunnable = new SoundReceiveRunnable();
        saveSoundThread = new SaveSoundThread("postSoundThread");
        soundToBreathThread = new SoundToBreathThread("soundToBreathThread");
        receiveThread = new Thread(receiveRunnable);
    }

    @Override
    public void start() {
        if(!receiveThread.isAlive()){
            receiveThread.start();
            Log.d(TAG, "Breath Start");
        }
        else{
            receiveRunnable.resume();
            Log.d(TAG, "Breath Resume");
        }
    }

    @Override
    public void stop() {
        receiveRunnable.stop();
        receiveThread.interrupt();
        dataHandler = null;
    }

    @Override
    public void pause() {
        receiveRunnable.pause();
    }

    @Override
    public void resume() {
        receiveRunnable.resume();
    }
}