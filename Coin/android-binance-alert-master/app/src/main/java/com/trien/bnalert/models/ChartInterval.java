package com.trien.bnalert.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ChartInterval implements Parcelable {

    private long time;
    private double price;

    public ChartInterval(double price, long time) {
        this.price = price;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ChartInterval{" +
                "time=" + time +
                ", price=" + price +
                '}';
    }

    protected ChartInterval(Parcel in) {
        time = in.readLong();
        price = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(time);
        dest.writeDouble(price);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ChartInterval> CREATOR = new Parcelable.Creator<ChartInterval>() {
        @Override
        public ChartInterval createFromParcel(Parcel in) {
            return new ChartInterval(in);
        }

        @Override
        public ChartInterval[] newArray(int size) {
            return new ChartInterval[size];
        }
    };
}