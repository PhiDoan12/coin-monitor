package com.trien.bnalert.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class Trade implements Parcelable, Comparable<Trade> {

    private long id;
    private double price;
    private double quantity;
    private long time;
    private boolean isBuyer;
    private boolean isMaker;

    public Trade(long id, double price, double quantity, long time) {
        this.id = id;
        this.price = price;
        this.quantity = quantity;
        this.time = time;
    }

    public Trade(Parcel in) {
        this.id = in.readLong();
        this.price = in.readDouble();
        this.quantity = in.readDouble();
        this.time = in.readLong();
        isBuyer = in.readByte() != 0x00;
        isMaker = in.readByte() != 0x00;
    }

    public long getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public long getTime() {
        return time;
    }

    public boolean isBuyer() {
        return isBuyer;
    }

    public boolean isMaker() {
        return isMaker;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeLong(id);
        dest.writeDouble(price);
        dest.writeDouble(quantity);
        dest.writeLong(time);
        dest.writeByte((byte) (isBuyer ? 0x01 : 0x00));
        dest.writeByte((byte) (isMaker ? 0x01 : 0x00));

    }

    public static final Parcelable.Creator<Trade> CREATOR = new Parcelable.Creator<Trade>() {

        public Trade createFromParcel(Parcel in) {
            return new Trade(in);
        }

        public Trade[] newArray(int size) {
            return new Trade[size];
        }
    };

    @Override
    public int compareTo(@NonNull Trade trade) {
        long compareTime = trade.getTime();

        //ascending order
        //return (int) (this.time - compareTime);

        //descending order
        return ((int) compareTime - (int) this.time);
    }
}
