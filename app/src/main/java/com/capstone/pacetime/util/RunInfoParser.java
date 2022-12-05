package com.capstone.pacetime.util;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Keep;

import com.capstone.pacetime.data.RunInfo;
import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.Step;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Keep
@IgnoreExtraProperties
public class RunInfoParser {
    private static final String TAG = "RUNINFO";
    protected OffsetDateTimeParser startDateTime;
    protected OffsetDateTimeParser endDateTime;
    protected List<LocationParser> trace;
    protected List<Breath> breathItems;
    protected List<Step> stepCount;
    protected float distance;
    protected long runningTime;
    protected long pace;
    protected int cadence;
    protected boolean isBreathUsed;
    protected long dateEpochSecond;
    protected int inhale;
    protected int exhale;

    public RunInfoParser(){
        OffsetDateTimeParser startDateTime = new OffsetDateTimeParser();
        this.startDateTime = new OffsetDateTimeParser();
        this.endDateTime = new OffsetDateTimeParser();
        this.distance = 0;
        this.runningTime = 0;
        this.pace = 0;
        this.cadence = 0;
        this.breathItems = new ArrayList<>();
        this.trace = new ArrayList<>();
        this.stepCount = new ArrayList<>();
        this.isBreathUsed = true;
        this.dateEpochSecond = startDateTime.getDateEpochSecond();
        this.inhale = 0;
        this.exhale = 0;
    }

    public RunInfoParser(boolean isBreathUsed, int inhale, int exhale){
        this();
        this.isBreathUsed = isBreathUsed;
        this.inhale = inhale;
        this.exhale = exhale;
    }
    public RunInfoParser(
            OffsetDateTimeParser startDateTime,
            OffsetDateTimeParser endDateTime,
            List<LocationParser> trace,
            List<Breath> breathItems,
            List<Step> stepCount,
            float distance,
            long runningTime,
            long pace,
            int cadence,
            boolean isBreathUsed,
            long dateEpochSecond,
            int inhale,
            int exhale
    ){
        this();
        this.startDateTime      = startDateTime;
        this.endDateTime        = endDateTime;
        this.trace              = trace       ;
        this.breathItems        = breathItems ;
        this.stepCount          = stepCount   ;
        this.distance           = distance    ;
        this.runningTime        = runningTime ;
        this.pace               = pace        ;
        this.cadence            = cadence     ;
        this.isBreathUsed       = isBreathUsed;
        this.dateEpochSecond    = dateEpochSecond;
        this.inhale             = inhale;
        this.exhale             = exhale;
    }

    public RunInfoParser(RunInfo runInfo){
        this();
        OffsetDateTimeParser startDateTime = new OffsetDateTimeParser(runInfo.getStartDateTime());
        this.startDateTime      = startDateTime;
        this.endDateTime        = new OffsetDateTimeParser(runInfo.getEndDateTime());
        this.trace              = listLocTolistLocParser(runInfo.getTrace());
        this.breathItems        = runInfo.getBreathItems();
        this.stepCount          = runInfo.getStepCount();
        this.distance           = runInfo.getDistance();
        this.runningTime        = runInfo.getRunningTime();
        this.pace               = runInfo.getPace();
        this.cadence            = runInfo.getCadence();
        this.isBreathUsed       = runInfo.getIsBreathUsed();
        this.dateEpochSecond    = startDateTime.getDateEpochSecond();
        this.inhale             = runInfo.getInhale();
        this.exhale             = runInfo.getExhale();
    }


    @Keep
    @IgnoreExtraProperties
    public static class OffsetDateTimeParser {
        private int year;
        private int month;
        private int dayOfMonth;
        private int hour;
        private int minute;
        private int second;
        private int nanoSecond;
        private String offset;
        private long dateEpochSecond;



        public OffsetDateTimeParser(){
            year = 2000;
            month = 9;
            dayOfMonth = 9;
            hour = 4;
            minute = 10;
            second = 3;
            nanoSecond = 123000000;
            offset = "+09:00";
            dateEpochSecond = 123L;
        }

        public OffsetDateTimeParser(int year, int month, int dayOfMonth, int hour, int minute, int second, int nanoSecond, String offset, long dateEpochSecond){
            this();
            this.year = year;
            this.month = month;
            this.dayOfMonth = dayOfMonth;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            this.nanoSecond = nanoSecond;
            this.offset = offset;
            this.dateEpochSecond = dateEpochSecond;
        }

        public OffsetDateTimeParser(OffsetDateTime offsetDateTime){
            this();
            this.year = offsetDateTime.getYear();
            this.month = offsetDateTime.getMonth().getValue();
            this.dayOfMonth = offsetDateTime.getDayOfMonth();
            this.hour = offsetDateTime.getHour();
            this.minute = offsetDateTime.getMinute();
            this.second = offsetDateTime.getSecond();
            this.nanoSecond = offsetDateTime.getNano();
            this.offset = offsetDateTime.getOffset().getId();
            this.dateEpochSecond = offsetDateTime.toEpochSecond();
        }

        public int getYear(){
            return year;
        }

        public int getMonth(){
            return month;
        }

        public int getDayOfMonth() {
            return dayOfMonth;
        }

        public int getHour(){
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public int getSecond() {
            return second;
        }

        public int getNanoSecond() {
            return nanoSecond;
        }

        public String getOffset() {
            return offset;
        }

        public long getDateEpochSecond() {
            return dateEpochSecond;
        }

        public OffsetDateTime parserToOrigin(){
            return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoSecond, ZoneOffset.of(offset));
        }
    }

    @Keep
    @IgnoreExtraProperties
    public static class LocationParser {
//        private String provider;
//        private long time;
//        private long elapsedRealtimeNanos;
//        private double elapsedRealtimeUncertaintyNanos;
//        private double latitudes;
//        private double longitude;
//        private float horizontalAccuracyMeters;
//        private double altitude;
//        private float altitudeAccuracyMeters;
//        private float speed;
//        private float SpeedAccuracyMetersPerSecond;
//        private float bearing;
//        private float bearingAccuracyDegrees;

        private int mFieldsMask = 0;
        private String mProvider;
        private long mTimeMs;
        private long mElapsedRealtimeNs;
        private double mElapsedRealtimeUncertaintyNs;
        private double mLatitudeDegrees;
        private double mLongitudeDegrees;
        private float mHorizontalAccuracyMeters;
        private double mAltitudeMeters;
        private float mAltitudeAccuracyMeters;
        private float mSpeedMetersPerSecond;
        private float mSpeedAccuracyMetersPerSecond;
        private float mBearingDegrees;
        private float mBearingAccuracyDegrees;
        private Bundle mExtras = null;


        public LocationParser(){
            mProvider = null;
            mTimeMs = 0;
            mElapsedRealtimeNs = 0;
            mElapsedRealtimeUncertaintyNs = 0.0;
            mFieldsMask = 0;
            mLatitudeDegrees = 0;
            mLongitudeDegrees = 0;
            mAltitudeMeters = 0;
            mSpeedMetersPerSecond = 0;
            mBearingDegrees = 0;
            mHorizontalAccuracyMeters = 0;
            mAltitudeAccuracyMeters = 0;
            mSpeedAccuracyMetersPerSecond = 0;
            mBearingAccuracyDegrees = 0;
            mExtras = null;
        }

        public LocationParser(int mFieldsMask, String mProvider, long mTimeMs, long mElapsedRealtimeNs, double mElapsedRealtimeUncertaintyNs,
                              double mLatitudeDegrees,double mLongitudeDegrees, float mHorizontalAccuracyMeters, double mAltitudeMeters,
                              float mAltitudeAccuracyMeters, float mSpeedMetersPerSecond, float mSpeedAccuracyMetersPerSecond,
                              float mBearingDegrees, float mBearingAccuracyDegrees, Bundle mExtras)
        {
            this.mFieldsMask = mFieldsMask;
            this.mProvider = mProvider;
            this.mTimeMs = mTimeMs;
            this.mElapsedRealtimeNs = mElapsedRealtimeNs;
            this.mElapsedRealtimeUncertaintyNs = mElapsedRealtimeUncertaintyNs;
            this.mLatitudeDegrees = mLatitudeDegrees;
            this.mLongitudeDegrees = mLongitudeDegrees;
            this.mHorizontalAccuracyMeters = mHorizontalAccuracyMeters;
            this.mAltitudeMeters = mAltitudeMeters;
            this.mAltitudeAccuracyMeters = mAltitudeAccuracyMeters;
            this.mSpeedMetersPerSecond = mSpeedMetersPerSecond;
            this.mSpeedAccuracyMetersPerSecond = mSpeedAccuracyMetersPerSecond;
            this.mBearingDegrees = mBearingDegrees;
            this.mBearingAccuracyDegrees = mBearingAccuracyDegrees;
            this.mExtras = mExtras;
        }

        public LocationParser(Location location) {
            set(location);
        }

        /**
         * Turns this location into a copy of the given location.
         */
        public void set(Location location) {
//            this.mFieldsMask = location.mFieldsMask; // fieldmask를 가져올 방법이 없네.
            this.mProvider = location.getProvider();
            this.mTimeMs = location.getTime();
            this.mElapsedRealtimeNs = location.getElapsedRealtimeNanos();
            this.mElapsedRealtimeUncertaintyNs = location.getElapsedRealtimeUncertaintyNanos();
            this.mLatitudeDegrees = location.getLatitude();
            this.mLongitudeDegrees = location.getLongitude();
            this.mHorizontalAccuracyMeters = location.getAccuracy();
            this.mAltitudeMeters = location.getAltitude();
            this.mAltitudeAccuracyMeters = location.getVerticalAccuracyMeters();
            this.mSpeedMetersPerSecond = location.getSpeed();
            this.mSpeedAccuracyMetersPerSecond = location.getSpeedAccuracyMetersPerSecond();
            this.mBearingDegrees = location.getBearing();
            this.mBearingAccuracyDegrees = location.getBearingAccuracyDegrees();
            this.mExtras = location.getExtras();
        }

        public int getmFieldsMask() {
            return mFieldsMask;
        }

        public String getmProvider() {
            return mProvider;
        }

        public long getmTimeMs() {
            return mTimeMs;
        }

        public long getmElapsedRealtimeNs() {
            return mElapsedRealtimeNs;
        }

        public double getmElapsedRealtimeUncertaintyNs() {
            return mElapsedRealtimeUncertaintyNs;
        }

        public double getmLatitudeDegrees() {
            return mLatitudeDegrees;
        }

        public double getmLongitudeDegrees() {
            return mLongitudeDegrees;
        }

        public float getmHorizontalAccuracyMeters() {
            return mHorizontalAccuracyMeters;
        }

        public double getmAltitudeMeters() {
            return mAltitudeMeters;
        }

        public float getmAltitudeAccuracyMeters() {
            return mAltitudeAccuracyMeters;
        }

        public float getmSpeedMetersPerSecond() {
            return mSpeedMetersPerSecond;
        }

        public float getmSpeedAccuracyMetersPerSecond() {
            return mSpeedAccuracyMetersPerSecond;
        }

        public float getmBearingDegrees() {
            return mBearingDegrees;
        }

        public float getmBearingAccuracyDegrees() {
            return mBearingAccuracyDegrees;
        }

        public Bundle getmExtras() {
            return mExtras;
        }

        public Location parserToOrigin() {
            Location location = new Location(mProvider);
            location.reset();
            location.setProvider(mProvider);
            location.setTime(mTimeMs);
            location.setElapsedRealtimeNanos(mElapsedRealtimeNs);
            location.setElapsedRealtimeUncertaintyNanos(mElapsedRealtimeUncertaintyNs);
//            location.setFieldsMask(mFieldsMask); //fieldsmask set이 없음.
            location.setLatitude(mLatitudeDegrees);
            location.setLongitude(mLongitudeDegrees);
            location.setAltitude(mAltitudeMeters);
            location.setSpeed(mSpeedMetersPerSecond);
            location.setBearing(mBearingDegrees);
            location.setAccuracy(mHorizontalAccuracyMeters);
            location.setVerticalAccuracyMeters(mAltitudeAccuracyMeters);
            location.setSpeedAccuracyMetersPerSecond(mSpeedAccuracyMetersPerSecond);
            location.setBearingAccuracyDegrees(mBearingAccuracyDegrees);
            location.setExtras(mExtras);

            return location;
        }
    }

    public List<LocationParser> listLocTolistLocParser(List<Location> listLocation){
        List<LocationParser> listLocationParser = new ArrayList<>(0);
        if(listLocation != null){
            for(int i = listLocation.size() - 1; i >= 0; i--){
                listLocationParser.add(0, new LocationParser(listLocation.get(i)));
            }
        }
        return listLocationParser;
    }

    public List<Location> listLocParserTolistLoc(List<LocationParser> listLocationParser){
        List<Location> listLocation = new ArrayList<>(0);
        if(listLocationParser != null){
            for(int i = listLocationParser.size() - 1; i >= 0; i--){
                listLocation.add(0, listLocationParser.get(i).parserToOrigin());
            }
        }
        return listLocation;
    }

    public boolean getIsBreathUsed(){
        return isBreathUsed;
    }
    public OffsetDateTimeParser getStartDateTime() {
        return startDateTime;
    }
    public OffsetDateTimeParser getEndDateTime() {
        return endDateTime;
    }
    public List<LocationParser> getTrace() {
        return trace;
    }
    public List<Breath> getBreathItems() {
        return breathItems;
    }
    public float getDistance() {
        return distance;
    }
    public long getRunningTime() {
        return runningTime;
    }
    public long getPace() {
        return pace;
    }
    public int getCadence() {
        return cadence;
    }
    public List<Step> getStepCount() {
        return stepCount;
    }
    public long getDateEpochSecond() {
        return dateEpochSecond;
    }
    public int getInhale() { return inhale; }
    public int getExhale() { return exhale; }

    public RunInfo parserToOrigin(){
        return new RunInfo(startDateTime.parserToOrigin(), endDateTime.parserToOrigin(), listLocParserTolistLoc(trace), breathItems, stepCount, distance, runningTime, pace, cadence, isBreathUsed, inhale, exhale);
    }
}