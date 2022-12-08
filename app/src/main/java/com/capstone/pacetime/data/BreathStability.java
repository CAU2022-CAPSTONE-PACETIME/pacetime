package com.capstone.pacetime.data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BreathStability implements Parcelable {
    public static final int VERY_STABLE = 1;
    public static final int QUITE_STABLE = 2;
    public static final int STABLE = 3;
    public static final int LITTLE_STABLE = 4;
    public static final int NOT_STABLE = 5;

    private final int value;
    private final int offset;

    public BreathStability(int value, int offset){
        assert value > 0 && value < 6;
        this.value = value;
        this.offset = offset;
    }

    protected BreathStability(Parcel in) {
        value = in.readInt();
        offset = in.readInt();
    }

    public static final Creator<BreathStability> CREATOR = new Creator<BreathStability>() {
        @Override
        public BreathStability createFromParcel(Parcel in) {
            return new BreathStability(in);
        }

        @Override
        public BreathStability[] newArray(int size) {
            return new BreathStability[size];
        }
    };

    public int getOffset(){
        return offset;
    }
    public int getValue(){
        return value;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(value);
        dest.writeInt(offset);
    }
}
