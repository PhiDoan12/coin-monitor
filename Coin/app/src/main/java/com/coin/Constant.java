package com.coin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentSkipListMap;

public class Constant {
    public static final String KEY_LOOP = "LOOP";
    public static final String KEY_PERCENT = "PERCENT";
    public static final String KEY_PERCENT_ABOUT = "PERCENT_ABOUT";
    public static final String KEY_PERCENT_SHOW_PRICE = "SHOW_PRICE";
    public static String KEY_PERCENT_SHOW_PRICE_VALUE = "BTC";
    public static String url = "https://api.binance.com/api/v3/ticker/price?symbol=%s";
    public static String URL_GET_PRICE_USDT_USD = "https://api.binance.us/api/v3/ticker/price?symbol=%s";
    public static ConcurrentSkipListMap<String, BigDecimal> priceBinanceCoin = new ConcurrentSkipListMap<String, BigDecimal>();
    public static ConcurrentSkipListMap<String, BigDecimal> priceBoughtCoin = new ConcurrentSkipListMap<String, BigDecimal>();
    public static ConcurrentSkipListMap<String, BigDecimal> notifyCoin = new ConcurrentSkipListMap<>();
    public static ConcurrentSkipListMap<String, BigDecimal> priceStockUs = new ConcurrentSkipListMap<String, BigDecimal>();
    public static BigDecimal timeDefaultFromUser = new BigDecimal("0");
    public static String RED_COLOR = "#EE0000";
    public static String GREEN_COLOR = "#0fbd49";
    public static int COUNT_SAVE_BUTTON = 0;
    public static String addColor(String text, String color) {
        return "<font color='" + color + "'>" + text + "</font>";
    }

    public static enum PairCoin {
        BTC,
        ETH,
        NEO,
        XRP,
        LTC,
        BCH,
        BNB,
        USDT,
        USD
    }

    public static enum US_STOCK {
        dow,
        nasdaq,
        sandp
    }

    public static synchronized void resetSaveCount(){
        Constant.COUNT_SAVE_BUTTON = 0;
    }

    public static synchronized void increaseSaveCount(){
        Constant.COUNT_SAVE_BUTTON++;
    }

    public static synchronized int getSaveCount(){
        return Constant.COUNT_SAVE_BUTTON;
    }


    public static void getPriceUsStock() throws IOException {
        String url = "https://markets.businessinsider.com/index/";
        //url = "https://google.com/search?q=";
        for(US_STOCK name : US_STOCK.values()){
            String names = "";
            switch (name){
                case dow:
                    names = "dow_jones";
                    //names = "dow+30";
                    break;
                case sandp:
                    names = "s&p_500";
                    //names = "sp500";
                    break;
                case nasdaq:
                    //names = "nasdaq_100";
                    break;
            }
            Document document = Jsoup.connect(url + names)
                    .timeout(60 * 1000)
                    .get();
            String price = document.select("span[data-format=maximumFractionDigits:2]").first().text();
            priceStockUs.put(name.name().toUpperCase(), new BigDecimal(price.replace(",", "")));
        }
    }
}