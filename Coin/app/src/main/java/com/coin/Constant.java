package com.coin;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class Constant {
    public static final String KEY_LOOP = "LOOP";
    public static final String KEY_PERCENT = "PERCENT";
    public static String url = "https://api.binance.com/api/v3/ticker/price";
    public static ConcurrentSkipListMap<String, BigDecimal> priceBinanceCoin = new ConcurrentSkipListMap<String, BigDecimal>();
    public static ConcurrentSkipListMap<String, BigDecimal> priceBoughtCoin = new ConcurrentSkipListMap<String, BigDecimal>();
    public static ConcurrentSkipListMap<String, BigDecimal> notifyCoin = new ConcurrentSkipListMap<>();
    public static BigDecimal loopTIme = null;
    public static String RED_COLOR = "#EE0000";
    public static String GREEN_COLOR = "#0fbd49";
    public static String BLACK_COLOR = "#000099";
    public static int COUNT_SAVE_BUTTON = 0;
    private static Gson gson = new Gson();
    public static String REAL_TIME = "0.03";
    public static final int COUNT_TIME_OUT = 10;
    public static FearGreendy fearAndGreendy = null;

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

    public static synchronized void resetSaveCount() {
        Constant.COUNT_SAVE_BUTTON = 0;
    }

    public static synchronized void increaseSaveCount() {
        Constant.COUNT_SAVE_BUTTON++;
    }

    public static synchronized int getSaveCount() {
        return Constant.COUNT_SAVE_BUTTON;
    }

    public static synchronized HashMap<String, String> getPriceBinance() {
        HashMap<String, String> map = new HashMap<>();
        int countTimeOut = 0;
        while (true) {
            try {
                System.out.println("getPriceBinance() -> " + url);
                String dataFromB = getJsonData(Constant.url);
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
                    if (countTimeOut == COUNT_TIME_OUT) {
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

    public synchronized static FearGreendy getFearAndGreedy() {
        try {
            if (fearAndGreendy == null) {
                String url = "https://api.alternative.me/fng/";
                System.out.println("getFearAndGreedy() -> " + url);
                String data = getJsonData(url);
                Map<String, Object> map = gson.fromJson(data, Map.class);
                List index = (List) map.get("data");
                map = (Map<String, Object>) index.get(0);
                fearAndGreendy = new FearGreendy();
                fearAndGreendy.setValue(new BigDecimal(map.get("value").toString()));
                fearAndGreendy.setStatus(map.get("value_classification").toString());
            }
            return fearAndGreendy;
        } catch (Exception ex) {
            return new FearGreendy();
        }
    }

    public static String getJsonData(String url) {
        try {
            String json = Jsoup.connect(url)
                    .timeout(1 * 60000)
                    .ignoreContentType(true).execute().body();
            return json;
        } catch (Exception e) {
            return "";
        }
    }
}
