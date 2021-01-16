package com.trien.bnalert.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ChartValue implements Parcelable {

    private long startTime;
    private long endTime;
    private double startPrice;
    private double endPrice;
    private double change;
    private double volume;
    private List<ChartInterval> intervals;

    public ChartValue(long startTime, long endTime, double startPrice, double endPrice, double change, double volume, List<ChartInterval> intervals) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startPrice = startPrice;
        this.endPrice = endPrice;
        this.change = change;
        this.volume = volume;
        this.intervals = intervals;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getEndPrice() {
        return endPrice;
    }

    public void setEndPrice(double endPrice) {
        this.endPrice = endPrice;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public List<ChartInterval> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<ChartInterval> intervals) {
        this.intervals = intervals;
    }

    protected ChartValue(Parcel in) {
        startTime = in.readLong();
        endTime = in.readLong();
        startPrice = in.readDouble();
        endPrice = in.readDouble();
        change = in.readDouble();
        volume = in.readDouble();
        if (in.readByte() == 0x01) {
            intervals = new ArrayList<ChartInterval>();
            in.readList(intervals, ChartInterval.class.getClassLoader());
        } else {
            intervals = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeDouble(startPrice);
        dest.writeDouble(endPrice);
        dest.writeDouble(change);
        dest.writeDouble(volume);
        if (intervals == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(intervals);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ChartValue> CREATOR = new Parcelable.Creator<ChartValue>() {
        @Override
        public ChartValue createFromParcel(Parcel in) {
            return new ChartValue(in);
        }

        @Override
        public ChartValue[] newArray(int size) {
            return new ChartValue[size];
        }
    };
}