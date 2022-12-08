package com.capstone.pacetime;

import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.RealTimeRunInfo;
import com.capstone.pacetime.data.BreathStability;
import com.capstone.pacetime.data.RunInfo;
import com.capstone.pacetime.data.enums.BreathState;
import com.capstone.pacetime.data.enums.RunningDataType;
import com.capstone.pacetime.data.enums.RunningState;
import com.capstone.pacetime.data.Step;
import com.capstone.pacetime.receiver.BreathReceiver;
import com.capstone.pacetime.receiver.GPSReceiver;
import com.capstone.pacetime.receiver.ReceiverLifeCycleInterface;
import com.capstone.pacetime.receiver.StepCounter;
import com.google.android.gms.location.LocationServices;

import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Timer;
import java.util.TimerTask;

public class RunningManager implements ReceiverLifeCycleInterface {
    private static final String TAG = "RunningManager";
    private final GPSReceiver gpsReceiver;
    private final BreathReceiver breathReceiver;
    private final StepCounter stepCounter;

    private BreathAnalyzer breathAnalyzer;
    private BreathAlarm breathAlarm;

    private StepAnalyzer stepAnalyzer;

    private UpdateTask updateTask;
    private final Timer updateTimer;

    private final RealTimeRunInfo runInfo;
    private RunningState state;

    private Handler handler;
    private final HandlerThread thread ;

    class DataThread extends HandlerThread{

        public DataThread(String name) {
            super(name);
        }

        @Override
        public synchronized void start() {
            super.start();
            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    if(msg.arg1 == RunningDataType.BREATH.ordinal() && runInfo.getIsBreathUsed()){
                        runInfo.addBreathItem((Breath)msg.obj);
                        breathAnalyzer.putBreathState((Breath)msg.obj);
//                        breathAlarm.play();

                        BreathStability latestStability =breathAnalyzer.getLatestStability();
                        if(latestStability != null){
                            if(latestStability.getValue() == BreathStability.NOT_STABLE){
                                breathAlarm.play();
                            }
                            else{
                                breathAlarm.stop();
                            }
                        }
                        Log.d(TAG, "Breath: " + ((Breath) msg.obj).getBreathState().name());
                    }
                    else if(msg.arg1 == RunningDataType.LOCATION.ordinal()){
                        Bundle data = msg.getData();
                        if(data == null){
                            return ;
                        }
                        Location loc = data.getParcelable("location");
                        Log.d(TAG, "GPS: " + loc.toString());

                        runInfo.addTrace(loc);

                    }else if(msg.arg1 == RunningDataType.STEP.ordinal()){
                        Step step = (Step)msg.obj;
                        runInfo.addStepCount(step);
                        stepAnalyzer.putStep(step);
                        Log.d(TAG, "Step: " + step.getCount());
                        if(runInfo.getIsBreathUsed()) {
                            assert breathReceiver != null;
                            breathReceiver.doConvert(step.getTimestamp());
                        }
                    }
                }
            };

            gpsReceiver.setDataHandler(handler);
            if(runInfo.getIsBreathUsed()){
                assert breathReceiver != null;
                breathReceiver.setDataHandler(handler);
            }
            stepCounter.setDataHandler(handler);
        }
    }

    public static class BreathAnalyzer{
        private final int inhaleCnt;
        private final int exhaleCnt;
        private final int patternLen;

        private final double[] preComputedArray;
        private final int biasWindowSize, stableWindowSize;
        private int bias, count, trigger;
        private final Deque<Double> breathDeque;
        private final Deque<Double> dSDeque;
        private final ArrayList<BreathStability> stabilities;

        public BreathAnalyzer(int inhaleCnt, int exhaleCnt){
            this.inhaleCnt = inhaleCnt;
            this.exhaleCnt = exhaleCnt;
            this.patternLen = inhaleCnt + exhaleCnt;
            this.biasWindowSize = 20;
            this.stableWindowSize = 100;
            this.preComputedArray = new double[inhaleCnt + exhaleCnt];
            initPreArray();
            this.breathDeque = new ArrayDeque<>();
            this.dSDeque = new ArrayDeque<>();
            this.stabilities = new ArrayList<>();
            this.bias = 0;
            this.trigger = 0;
        }

        private void initPreArray(){
            for(int i = 0; i < inhaleCnt; i++){
                preComputedArray[i] = (sin(PI * (i + 1) / (inhaleCnt + 2)) /2 + 0.5f);
            }
            for(int i = 0; i < exhaleCnt; i++){
                preComputedArray[i + inhaleCnt] = (sin(PI * (i + 1) / (exhaleCnt + 2) + PI) /2 + 0.5f);
            }
        }

        public static ArrayList<BreathStability> getStabilityList(RunInfo info){
            BreathAnalyzer ba = new BreathAnalyzer(info.getInhale(), info.getExhale());
            for(Breath item: info.getBreathItems()){
                ba.putBreathState(item);
            }
            return ba.getStabilities();
        }

        public void putBreathState(Breath breath){
            breathDeque.add((double) breath.getValue());
            dSDeque.add(pow((double) breath.getValue() - preComputedArray[(bias+count)%patternLen], 2));
            if(breathDeque.size() >= stableWindowSize + 1) {
                breathDeque.pollFirst();
                dSDeque.pollFirst();
            }if(breathDeque.size() > 1 && count % 10 == 0){
                if(isBiased()){
                    computeBias();
                }
                if(count >= stableWindowSize + trigger){
                    stabilities.add(getStability());
                }
            }
            count++;
        }
        private boolean isBiased(){
            final float threshold = 0.25f;

            if(dSDeque.size() <20 ){
                return false;
            }

            double var = 0;
            Iterator<Double> iter = dSDeque.descendingIterator();
            for(int i = 0; i < 20; i++){
                var += iter.next();
            }

            var /= 20;

            Log.d(TAG, "BIAS VAR: " + var);

            return var >= threshold;
        }

        private BreathStability getStability(){
            final float threshold = 0.3f;

            OptionalDouble optVAR = dSDeque.stream().mapToDouble(d -> d).average();

            assert optVAR.isPresent();

            double var = optVAR.getAsDouble();

            Log.d(TAG, "STAB VAR: " + var);
            if(var > threshold){
                return new BreathStability(BreathStability.NOT_STABLE, count);
            }else if(var >threshold * 0.8){
                return new BreathStability(BreathStability.LITTLE_STABLE, count);
            }else if(var > threshold * 0.7){
                return new BreathStability(BreathStability.STABLE, count);
            }else if(var > threshold * 0.6){
                return new BreathStability(BreathStability.QUIET_STABLE, count);
            }else{
                return new BreathStability(BreathStability.VERY_STABLE, count);
            }
        }

        private void computeBias(){
            int minBias = 0;
            double minVar = 1;
            for(int b = 0; b < patternLen; b++){
                if(b == bias){
                    continue;
                }
                double var = 0;
                Iterator<Double> iter = breathDeque.descendingIterator();
                for(int i = 0; i < biasWindowSize/2; i++){
                    var += pow(preComputedArray[(b + count - i) % patternLen] - iter.next(), 2);
                }
                var /= biasWindowSize;
                if(var < minVar){
                    minVar = var;
                    minBias = b;
                }
            }
            bias = minBias;

            Log.d(TAG, "BIAS MINVAR: " + minVar);
            Log.d(TAG, "BIAS NEW: " + bias);
        }

        public ArrayList<BreathStability> getStabilities(){
            return stabilities;
        }

        public BreathStability getLatestStability(){
            if(stabilities.isEmpty()){
                return null;
            }
            return stabilities.get(stabilities.size() - 1);
        }

        public void clear(){
            breathDeque.clear();
            dSDeque.clear();
            trigger = count;
        }
    }

    public static class BreathAlarm{
//        private final AudioTrack track;
//        private final int minSize;
//        private final int SAMPLE_RATE=11025;
//        private final short[] buffer;
        private final Runnable playThread;

        BreathAlarm(AudioManager audioManager){
//            minSize = AudioTrack.getMinBufferSize(
//                    SAMPLE_RATE,
//                    AudioFormat.CHANNEL_OUT_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT
//            );
//
//            Log.d(TAG, "MINSIZE: " + minSize);
//
//            track = new AudioTrack.Builder()
//                    .setAudioAttributes(
//                            new AudioAttributes.Builder()
//                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
//                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                                    .setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_SYSTEM)
//                                    .build()
//                    )
//                    .setAudioFormat(new AudioFormat.Builder()
//                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                            .setSampleRate(SAMPLE_RATE)
//                            .build()
//                    )
//                    .setBufferSizeInBytes(minSize)
//                    .build();

            /*
            *
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minSize,
                    AudioTrack.MODE_STREAM
            * */
//            buffer = new short[]{
//                    8130, 15752, 32695, 12253, 4329,
//                    -3865, -19032, -32722, -16160, -466,
//                    8130, 15752, 22389, 27625, 31134, 32695, 32210,
//                    29711, 25354, 19410, 12253, 4329, -3865, -11818, -19032,
//                    -25055, -29511, -32121, -32722, -31276, -27874, -22728,
//                    -16160, -8582, -466
//            };

            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM
                    , (int)(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
                    , AudioManager.FLAG_PLAY_SOUND);

            playThread = () -> {
//                track.write(buffer, 0, buffer.length);
//                Log.d(TAG, "PLAY SOUND");
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, audioManager.getStreamVolumeDb(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioDeviceInfo.TYPE_BLUETOOTH_SCO));

            };
        }

        private boolean run = false;
        private long period;
        public void play() {
            Log.d(TAG, "PLAY SOUND");
//            run=true;
            new Thread(playThread).start();
        }
        public void stop(){
            if(run){
                Log.d(TAG, "STOP SOUND");

                run = false;
//                playThread.interrupt();
            }
        }
    }

    public static class StepAnalyzer{
        private final Deque<Step> stepDeque;

        private long period;

        public StepAnalyzer(){
            period = 0;
            stepDeque = new ArrayDeque<>();
        }

        public void putStep(Step step){
            long before = 0;
            if(stepDeque.size() != 0){
                before = stepDeque.getLast().getTimestamp();
            }

            stepDeque.add(step);

            if(stepDeque.size() == 1)
                return;
            period += stepDeque.getLast().getTimestamp() - before;

            if(stepDeque.size() > 100){
                long first = stepDeque.getFirst().getTimestamp();
                stepDeque.pollFirst();
                period -= stepDeque.getFirst().getTimestamp() - first;
            }
        }
        public long getPeriod(){
            return period / 99;
        }
    }

    public RunningManager(AppCompatActivity activity, RealTimeRunInfo runInfo){
        this.runInfo = runInfo;

        updateTimer = new Timer();

        thread = new DataThread("DataHandlerThread");

        SensorManager sensorManager = (SensorManager) activity.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        gpsReceiver = new GPSReceiver(
                LocationServices.getFusedLocationProviderClient(activity),
                (LocationManager)activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE)
        );
        if(runInfo.getIsBreathUsed()) {
            breathReceiver = new BreathReceiver(
                    (AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE),
                    activity.getApplicationContext()
            );
            breathAnalyzer = new BreathAnalyzer(runInfo.getInhale(), runInfo.getExhale());
            breathAlarm = new BreathAlarm((AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE));
        } else{
            breathReceiver = null;
            breathAnalyzer = null;
        }
        stepCounter = new StepCounter(sensorManager);
        stepAnalyzer = new StepAnalyzer();

    }

    public void setState(RunningState state){
        this.state = state;
    }
    public RunningState getState(){
        return this.state;
    }

    class UpdateTask extends TimerTask {
        private boolean paused = false;
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            if(paused)
                return;
            runInfo.update();
        }

        public void pause(){
            paused = true;
            runInfo.update();
        }

        public void resume(){
            paused = false;
        }
    }

    @Override
    public void start(){
        thread.start();
        this.updateTask = new UpdateTask();
        updateTimer.schedule(updateTask, 1000, 500);

        gpsReceiver.start();
        stepCounter.start();
        if(runInfo.getIsBreathUsed())
            breathReceiver.start();

        runInfo.setStartDateTime(OffsetDateTime.now());
        runInfo.setEndDateTime(OffsetDateTime.now());
        setState(RunningState.RUN);
    }

    @Override
    public void stop() {
        setState(RunningState.STOP);
        if(updateTask != null){
            updateTask.cancel();
            updateTask = null;
        }
        gpsReceiver.stop();
        if(runInfo.getIsBreathUsed())
            breathReceiver.stop();
        stepCounter.stop();
        thread.interrupt();
    }

    @Override
    public void pause() {
        gpsReceiver.pause();
        if(runInfo.getIsBreathUsed()){
            breathAnalyzer.clear();
            breathReceiver.pause();
        }
        stepCounter.pause();
        updateTask.pause();
        setState(RunningState.PAUSE);
//        if(updateTask != null){
//            updateTask.cancel();
//            updateTask = null;
//        }

    }

    @Override
    public void resume() {
        setState(RunningState.RUN);
        runInfo.setEndDateTime(OffsetDateTime.now());
        runInfo.updateLastDateTime();
        updateTask.resume();
        gpsReceiver.resume();
        if(runInfo.getIsBreathUsed())
            breathReceiver.resume();

        stepCounter.resume();
    }

    public static String[] getPermissionSets(){
        String[] ret = new String[
                BreathReceiver.PERMISSIONS.length +
                        GPSReceiver.PERMISSIONS.length +
                        StepCounter.PERMISSIONS.length
                ];

        int cnt = 0;
        for(String[] permissions : new String[][]{ BreathReceiver.PERMISSIONS, GPSReceiver.PERMISSIONS, StepCounter.PERMISSIONS }){
            for(String permission : permissions){
                ret[cnt] = permission;
                cnt++;
            }
        }

        return ret;
    }

    public ArrayList<BreathStability> getStabilities(){
        if(runInfo.getIsBreathUsed()){
            return breathAnalyzer.getStabilities();
        } else {
            return null;
        }
    }
}
