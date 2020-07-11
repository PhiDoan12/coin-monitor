package com.coin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public class Constant {
    public static final String KEY_LOOP = "LOOP";
    public static final String KEY_PERCENT = "PERCENT";
    public static final String KEY_PERCENT_ABOUT = "PERCENT_ABOUT";
    public static String KEY_PERCENT_SHOW_PRICE_VALUE = "BTC";
    public static String url = "https://api.binance.com/api/v3/ticker/price";
    public static ConcurrentSkipListMap<String, BigDecimal> priceBinanceCoin = new ConcurrentSkipListMap<String, BigDecimal>();
    public static ConcurrentSkipListMap<String, BigDecimal> priceBoughtCoin = new ConcurrentSkipListMap<String, BigDecimal>();
    public static ConcurrentSkipListMap<String, BigDecimal> notifyCoin = new ConcurrentSkipListMap<>();
    public static ConcurrentSkipListMap<String, BigDecimal> priceStockUs = new ConcurrentSkipListMap<String, BigDecimal>();
    public static BigDecimal timeDefaultFromUser = new BigDecimal("0");
    public static String RED_COLOR = "#EE0000";
    public static String GREEN_COLOR = "#0fbd49";
    public static int COUNT_SAVE_BUTTON = 0;
    private static Gson gson = new Gson();
    public static String REAL_TIME = "0.03";
    private static boolean getOnLocked = false;
    public static final int COUNT_TIME_OUT = 15;

    public static String addColor(String text, String color) {
        return "<font color='" + color + "'>" + text + "</font>";
    }

    public static synchronized boolean getOnLocked() {
        return getOnLocked;
    }

    public static synchronized void setOnLocked(boolean getOnLocked) {
        Constant.getOnLocked = getOnLocked;
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

    public static synchronized void resetSaveCount() {
        Constant.COUNT_SAVE_BUTTON = 0;
    }

    public static synchronized void increaseSaveCount() {
        Constant.COUNT_SAVE_BUTTON++;
    }

    public static synchronized int getSaveCount() {
        return Constant.COUNT_SAVE_BUTTON;
    }


    public static void getPriceUsStock() {
        String url = "https://markets.businessinsider.com/index/";
        for (US_STOCK name : US_STOCK.values()) {
            String names = "";
            switch (name) {
                case dow:
                    names = "dow_jones";
                    break;
                case sandp:
                    names = "s&p_500";
                    break;
                case nasdaq:
                    names = "nasdaq_100";
                    break;
            }
            String data = MyGETRequest(url + names);
            if (StringUtils.isEmpty(data)) {
                return;
            }
            Document document = Jsoup.parse(data);
            String price = document.select("span[data-format=maximumFractionDigits:2]").first().text();
            priceStockUs.put(name.name().toUpperCase(), new BigDecimal(price.replace(",", "")));
        }
    }

    public static synchronized HashMap<String, String> getPriceBinance() {
        HashMap<String, String> map = new HashMap<>();
        int countTimeOut = 0;
        while (true) {
            try {
                System.out.println("getPriceBinance() -> " + url);
                String dataFromB = MyGETRequest(Constant.url);
                if (StringUtils.isEmpty(dataFromB)) {
                    throw new Exception("->> Binance return empty");
                }
                Type listType = new TypeToken<List<PriceBinance>>() {
                }.getType();
                List<PriceBinance> priceBinance = gson.fromJson(dataFromB, listType);
                for (PriceBinance price : priceBinance) {
                    if (price.getSymbol().contains(PairCoin.USDT.name())) {
                        map.put(price.getSymbol().replace("USDT", "").toUpperCase(), price.getPrice());
                    }
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1 * 1000);
                    if(countTimeOut == COUNT_TIME_OUT){
                        break;
                    }
                    countTimeOut++;
                } catch (InterruptedException ex) {
                    //ex.printStackTrace();
                }
            }
        }
        return map;
    }

    public static String MyGETRequest(String url) {
        try {
            URL urlForGetRequest = new URL(url);
            String readLine = null;
            HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
            conection.setRequestMethod("GET");
            int responseCode = conection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conection.getInputStream()));
                StringBuffer response = new StringBuffer();
                while ((readLine = in.readLine()) != null) {
                    response.append(readLine);
                }
                in.close();
                return response.toString();
            } else {
                System.out.println("GET NOT WORKED -> " + Constant.url);
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}
