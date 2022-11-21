package com.capstone.pacetime;

import android.location.Location;

import androidx.annotation.Keep;

import com.capstone.pacetime.data.Breath;
import com.capstone.pacetime.data.Step;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Keep
@IgnoreExtraProperties
public class RunInfoParser {
    private static final String TAG = "RUNINFO";
    protected OffsetDateTimeParser startDateTime;
    protected OffsetDateTimeParser endDateTime;
    protected List<Location> trace;
    protected List<Breath> breathItems;
    protected List<Step> stepCount;
    protected float distance;
    protected long runningTime;
    protected long pace;
    protected int cadence;
    protected boolean isBreathUsed;
    protected long dateEpochSecond;

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
    }

    public RunInfoParser(boolean isBreathUsed){
        this();
        this.isBreathUsed = isBreathUsed;
    }
    public RunInfoParser(
            OffsetDateTimeParser startDateTime,
            OffsetDateTimeParser endDateTime,
            List<Location> trace,
            List<Breath> breathItems,
            List<Step> stepCount,
            float distance,
            long runningTime,
            long pace,
            int cadence,
            boolean isBreathUsed,
            long dateEpochSecond
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
        this.dateEpochSecond    = dateEpochSecond; //
    }

    public RunInfoParser(RunInfo runInfo){
        this();
        OffsetDateTimeParser startDateTime = new OffsetDateTimeParser(runInfo.getStartDateTime());
        this.startDateTime      = startDateTime;
        this.endDateTime        = new OffsetDateTimeParser(runInfo.getEndDateTime());
        this.trace              = runInfo.getTrace();
        this.breathItems        = runInfo.getBreathItems();
        this.stepCount          = runInfo.getStepCount();
        this.distance           = runInfo.getDistance();
        this.runningTime        = runInfo.getRunningTime();
        this.pace               = runInfo.getPace();
        this.cadence            = runInfo.getCadence();
        this.isBreathUsed       = runInfo.getIsBreathUsed();
        this.dateEpochSecond    = startDateTime.getDateEpochSecond();
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

    public boolean getIsBreathUsed(){
        return isBreathUsed;
    }
    public OffsetDateTimeParser getStartDateTime() {
        return startDateTime;
    }
    public OffsetDateTimeParser getEndDateTime() {
        return endDateTime;
    }
    public List<Location> getTrace() {
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

    public RunInfo parserToOrigin(){
        return new RunInfo(startDateTime.parserToOrigin(), endDateTime.parserToOrigin(), trace, breathItems, stepCount, distance, runningTime, pace, cadence, isBreathUsed);
    }
}
