package com.capstone.pacetime.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.capstone.pacetime.Breath;
import com.capstone.pacetime.BreathState;
import com.capstone.pacetime.RunningDataType;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BreathReceiver implements StartStopInterface{
    private static final String TAG = "BreathReceiver";

    private final Handler dataHandler;

    private AudioManager audioManager;
    private AudioRecord audioRecord;

    private final int AUDIO_SAMP_RATE = 44100;
    private final float recordSeconds = 0.5f;
    private short[] bufferRecord;
    private int bufferRecordSize;
    private final int MAX_QUEUE_SIZE = 11025 * 3;

    private final Queue<Short> soundQueue;

    private Handler
            soundToBreathHandler,
            saveSoundHandler
    ;
    private HandlerThread
            soundToBreathThread,
            saveSoundThread
    ;
    private Thread receiveThread;

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
        private final long timestamp;

        public SaveSoundRunnable(short[] buffer, long timestamp){
            this.buffer = buffer;
            this.timestamp = timestamp;
        }

        @Override
        public void run(){
            for(short val : buffer){
                soundQueue.add(val);
                if(soundQueue.size() > AUDIO_SAMP_RATE * recordSeconds){
                    soundQueue.poll();
                }
            }

            soundToBreathHandler.post(new SoundToBreathRunnable(soundQueue.toArray(), timestamp));
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

    class SoundToBreathRunnable implements Runnable{
        private final Short[] sound;
        private final long timestamp;

        public SoundToBreathRunnable(Object[] sound, long timestamp){
            this.sound = (Short[]) sound;
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
    };

    @SuppressLint("MissingPermission")
    public BreathReceiver(AudioManager audioManager, Handler dataHandler){
        this.audioManager = audioManager;

        bufferRecordSize = AudioRecord.getMinBufferSize(
                AUDIO_SAMP_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT
        );

        bufferRecord = new short[bufferRecordSize];

        soundQueue = new ConcurrentLinkedQueue<>();

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_CALL,
                AUDIO_SAMP_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferRecordSize);

        this.dataHandler = dataHandler;

        setPipeline();
    }

    private void setPipeline(){
        saveSoundThread = new SaveSoundThread("postSoundThread");
        soundToBreathThread = new SoundToBreathThread("soundToBreathThread");
        receiveThread = new Thread(()->{
            saveSoundThread.start();
            soundToBreathThread.start();
            while (!receiveThread.isInterrupted()) {
                int size = audioRecord.read(bufferRecord, 0, bufferRecordSize);
                saveSoundHandler.post(new SaveSoundRunnable(Arrays.copyOfRange(bufferRecord.clone(), 0, size), System.currentTimeMillis()));
            }
            saveSoundThread.interrupt();
            soundToBreathThread.interrupt();
        });
    }

    @Override
    public void start() {
        receiveThread.start();
    }

    @Override
    public void stop() {
        receiveThread.interrupt();
    }
}

class BreathAnalyzer {

}

