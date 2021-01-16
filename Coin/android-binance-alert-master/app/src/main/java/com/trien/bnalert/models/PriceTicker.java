package com.trien.bnalert.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

public class PriceTicker implements Parcelable {

    @SerializedName("symbol")
    private String coinPair;

    @SerializedName("price")
    private double price;

    private double change;

    public PriceTicker(String coinPair, double price) {
        this.coinPair = coinPair;
        this.price = price;
    }

    public PriceTicker(String coinPair, double price, double change) {
        this.coinPair = coinPair;
        this.price = price;
        this.change = change;
    }

    public PriceTicker(Parcel in) {
        this.coinPair = in.readString();
        this.price = in.readDouble();
        this.change = in.readDouble();
    }

    public String getCoinPair() {
        return coinPair;
    }

    public void setCoinPair(String coinPair) {
        this.coinPair = coinPair;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    @Override
    public String toString() {
        return "PriceTicker{" +
                "coinPair='" + coinPair + '\'' +
                ", price=" + price +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(coinPair);
        dest.writeDouble(price);
        dest.writeDouble(change);
    }

    public static final Parcelable.Creator<PriceTicker> CREATOR = new Parcelable.Creator<PriceTicker>() {

        public PriceTicker createFromParcel(Parcel in) {
            return new PriceTicker(in);
        }

        public PriceTicker[] newArray(int size) {
            return new PriceTicker[size];
        }
    };

    public static Comparator<PriceTicker> symbolAscComparator
            = new Comparator<PriceTicker>() {

        public int compare(PriceTicker ticker1, PriceTicker ticker2) {

            String symbol1 = ticker1.getCoinPair();
            String symbol2 = ticker2.getCoinPair();

            //ascending order
            return symbol1.compareTo(symbol2);

            //descending order
            //return symbol2.compareTo(symbol1);
        }

    };

    public static Comparator<PriceTicker> symbolDescComparator
            = new Comparator<PriceTicker>() {

        public int compare(PriceTicker ticker1, PriceTicker ticker2) {

            String symbol1 = ticker1.getCoinPair();
            String symbol2 = ticker2.getCoinPair();

            //ascending order
            //return symbol1.compareTo(symbol2);

            //descending order
            return symbol2.compareTo(symbol1);
        }

    };

    public static Comparator<PriceTicker> priceAscComparator
            = new Comparator<PriceTicker>() {

        public int compare(PriceTicker ticker1, PriceTicker ticker2) {

            double price1 = ticker1.getPrice();
            double price2 = ticker2.getPrice();

            return Double.compare(price1, price2);
        }

    };

    public static Comparator<PriceTicker> priceDescComparator
            = new Comparator<PriceTicker>() {

        public int compare(PriceTicker ticker1, PriceTicker ticker2) {

            double price1 = ticker1.getPrice();
            double price2 = ticker2.getPrice();

            return Double.compare(price2, price1);
        }

    };

    public static Comparator<PriceTicker> priceChangeAscComparator
            = new Comparator<PriceTicker>() {

        public int compare(PriceTicker ticker1, PriceTicker ticker2) {

            double change1 = ticker1.getChange();
            double change2 = ticker2.getChange();

            return Double.compare(change1, change2);
        }

    };

    public static Comparator<PriceTicker> priceChangeDescComparator
            = new Comparator<PriceTicker>() {

        public int compare(PriceTicker ticker1, PriceTicker ticker2) {

            double change1 = ticker1.getChange();
            double change2 = ticker2.getChange();

            return Double.compare(change2, change1);
        }

    };
}
