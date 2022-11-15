package com.capstone.pacetime.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.BreathState;
import com.capstone.pacetime.RunningDataType;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BreathReceiver implements ReceiverLifeCycleInterface {
    private static final String TAG = "BreathReceiver";

    private Handler dataHandler;

    private final AudioManager audioManager;
    private final AudioRecord audioRecord;

    private final int AUDIO_SAMP_RATE = 44100;
    private final float recordSeconds = 0.5f;
    private final short[] bufferRecord;
    private final int bufferRecordSize;
    private final int MAX_QUEUE_SIZE = 11025 * 3;

    private final Queue<Short> soundQueue;

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
        Object pauseLock = new Object();

        @Override
        public void run() {
            saveSoundThread.start();
            soundToBreathThread.start();

            while (isRunning) {
                synchronized (pauseLock) {
                    if (!isRunning) {
                        break;
                    }
                    if (paused) {
                        try {
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
                saveSoundHandler.post(new SaveSoundRunnable(Arrays.copyOfRange(bufferRecord.clone(), 0, size)));
            }
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
                }
            }
        }
    }

    public void doConvert(long timestamp){
        if(!soundQueue.isEmpty())
            soundToBreathHandler.post(new SoundToBreathRunnable(soundQueue.toArray(), timestamp));
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

    class SoundToBreathRunnable implements Runnable{
        private final Short[] sound;
        private final long timestamp;

        public SoundToBreathRunnable(Object[] sound, long timestamp){
            this.sound = new Short[22050]; // 22050

            for(int i = 0; i < 22050; i++){
                this.sound[i] = (Short)sound[i];
            }

            this.timestamp = timestamp;
        }

        @Override
        public void run() {
            // TODO: Tensorflow Implementation Required

            Message msg = new Message();
            msg.obj = new Breath(BreathState.INHALE, timestamp);
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
    public BreathReceiver(AudioManager audioManager){
        this.audioManager = audioManager;

        bufferRecordSize = AudioRecord.getMinBufferSize(
                AUDIO_SAMP_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT
        );

        bufferRecord = new short[bufferRecordSize];

        soundQueue = new ConcurrentLinkedQueue<>();

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
        if(!receiveThread.isAlive())
            receiveThread.start();
        else{
            receiveRunnable.resume();
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