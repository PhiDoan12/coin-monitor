package com.trien.bnalert.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Comparator;

/**
 * Represents one record of the Alert table.
 */

@Entity(tableName = "alerts")
public class Alert implements Parcelable {

    /** The unique ID of an alert, should be the system time in milliseconds. */
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "alertId")
    private int id;

    /** other attributes */
    @ColumnInfo(name = "coin")
    private String coin;
    @ColumnInfo(name = "comparator")
    private String comparator;
    @ColumnInfo(name = "comparedToValue")
    private double comparedToValue;
    @ColumnInfo(name = "baseCoin")
    private String baseCoin;
    @ColumnInfo(name = "isPersistent")
    private boolean isPersistent;
    @ColumnInfo(name = "interval")
    private double interval;
    @ColumnInfo(name = "vibrate")
    private boolean vibrate;
    @ColumnInfo(name = "playSound")
    private boolean playSound;
    @ColumnInfo(name = "flashing")
    private boolean flashing;
    @ColumnInfo(name = "isActive")
    private boolean isActive;
    @ColumnInfo(name = "dateAdded")
    private long dateAdded;

    public Alert(int id, String coin, String comparator, double comparedToValue, String baseCoin, boolean isPersistent, double interval, boolean vibrate, boolean playSound, boolean flashing, boolean isActive, long dateAdded) {
        this.id = id;
        this.coin = coin;
        this.comparator = comparator;
        this.comparedToValue = comparedToValue;
        this.baseCoin = baseCoin;
        this.isPersistent = isPersistent;
        this.interval = interval;
        this.vibrate = vibrate;
        this.playSound = playSound;
        this.flashing = flashing;
        this.isActive = isActive;
        this.dateAdded = dateAdded;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", coin='" + coin + '\'' +
                ", comparator='" + comparator + '\'' +
                ", comparedToValue=" + comparedToValue +
                ", baseCoin='" + baseCoin + '\'' +
                ", isPersistent=" + isPersistent +
                ", interval=" + interval +
                ", vibrate=" + vibrate +
                ", playSound=" + playSound +
                ", flashing=" + flashing +
                ", isActive=" + isActive +
                ", dateAdded=" + dateAdded +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public double getComparedToValue() {
        return comparedToValue;
    }

    public void setComparedToValue(double comparedToValue) {
        this.comparedToValue = comparedToValue;
    }

    public String getBaseCoin() {
        return baseCoin;
    }

    public void setBaseCoin(String baseCoin) {
        this.baseCoin = baseCoin;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public void setPersistent(boolean persistent) {
        isPersistent = persistent;
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public boolean isPlaySound() {
        return playSound;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    public boolean isFlashing() {
        return flashing;
    }

    public void setFlashing(boolean flashing) {
        this.flashing = flashing;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    protected Alert(Parcel in) {
        id = in.readInt();
        coin = in.readString();
        comparator = in.readString();
        comparedToValue = in.readDouble();
        baseCoin = in.readString();
        isPersistent = in.readByte() != 0x00;
        interval = in.readDouble();
        vibrate = in.readByte() != 0x00;
        playSound = in.readByte() != 0x00;
        flashing = in.readByte() != 0x00;
        isActive = in.readByte() != 0x00;
        dateAdded = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(coin);
        dest.writeString(comparator);
        dest.writeDouble(comparedToValue);
        dest.writeString(baseCoin);
        dest.writeByte((byte) (isPersistent ? 0x01 : 0x00));
        dest.writeDouble(interval);
        dest.writeByte((byte) (vibrate ? 0x01 : 0x00));
        dest.writeByte((byte) (playSound ? 0x01 : 0x00));
        dest.writeByte((byte) (flashing ? 0x01 : 0x00));
        dest.writeByte((byte) (isActive ? 0x01 : 0x00));
        dest.writeLong(dateAdded);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Alert> CREATOR = new Parcelable.Creator<Alert>() {
        @Override
        public Alert createFromParcel(Parcel in) {
            return new Alert(in);
        }

        @Override
        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };

    public static Comparator<Alert> dateAddedAscComparator
            = (alert1, alert2) -> {

                long dateAdded1 = alert1.getDateAdded();
                long dateAdded2 = alert2.getDateAdded();

                return Long.compare(dateAdded1, dateAdded2);
            };
}