package com.pedromassango.jobschedulersample;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Constant {
    public static String coinName = "";
    public static int percent = 0;
    public static int loop = 2;
    public static BigDecimal markPrice = null;
    public static boolean firstTime = false;
    public static String url = "https://api.binance.com/api/v3/ticker/price";
    public static Gson gson = new Gson();
    public static final int COUNT_TIME_OUT = 10;
    public static int indexNotification = 0;

    public static synchronized int getNotificationIndex(){
        indexNotification++;
        return indexNotification;
    }
    public enum PairCoin {
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

    public static HashMap<String, String> getPriceBinance() {
        HashMap<String, String> map = new HashMap<>();
        int countTimeOut = 0;
        while (true) {
            try {
                System.out.println("getPriceBinance() -> " + Constant.url);
                String dataFromB = MyGETRequest(Constant.url);
                if (StringUtils.isEmpty(dataFromB)) {
                    throw new Exception("->> Binance return empty");
                }
                Type listType = new TypeToken<List<PriceBinance>>() {
                }.getType();
                List<PriceBinance> priceBinance = Constant.gson.fromJson(dataFromB, listType);
                for (PriceBinance price : priceBinance) {
                    if (price.getSymbol().contains(Constant.PairCoin.USDT.name())) {
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
            e.printStackTrace();
            return "";
        }
    }

    public static void updateMarkPrice(){
        HashMap<String, String> price = Constant.getPriceBinance();
        Constant.markPrice = new BigDecimal(price.get(Constant.coinName));
    }
}
