package com.coin;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MonitorPrice {
    private static Gson gson = new Gson();

    public BigDecimal getTime() {
        return Constant.priceBoughtCoin.get(Constant.KEY_LOOP);
    }

    public HashMap<String, String> getPrice(Context context) {
        HashMap<String, String> da = new HashMap<>();
        try {
            if (Constant.priceBoughtCoin.size() == 0) {
                loadData(context);
                Constant.timeDefaultFromUser = Constant.priceBoughtCoin.get(Constant.KEY_LOOP);
            }

            if (Constant.priceBoughtCoin.size() == 0) {
                return null;
            }

            Constant.priceBinanceCoin.clear();

            String text = "";
            for (String coin : Constant.priceBoughtCoin.keySet()) {
                if (coin.equals(Constant.KEY_PERCENT)) {
                    continue;
                }
                if (coin.equals(Constant.KEY_LOOP)) {
                    continue;
                }
                if (coin.equals(Constant.KEY_PERCENT_ABOUT)) {
                    continue;
                }
                if(coin.equalsIgnoreCase(Constant.US_STOCK.dow.toString())
                        || coin.equalsIgnoreCase(Constant.US_STOCK.sandp.toString())
                        || coin.equalsIgnoreCase(Constant.US_STOCK.nasdaq.toString()) ){
                    continue;
                }

                String coinPair = coin + Constant.PairCoin.USDT.name();
                String Url = Constant.url;
                if(coin.equalsIgnoreCase(Constant.PairCoin.USDT.toString())){
                    coinPair = coin + Constant.PairCoin.USD.name();
                    Url = Constant.URL_GET_PRICE_USDT_USD;
                }
                Document doc = Jsoup.connect(String.format(Url, coinPair))
                        .ignoreContentType(true).timeout(30 * 1000).get();
                String dataFromB = doc.text();
                PriceBinace priceFromB = gson.fromJson(dataFromB, PriceBinace.class);
                if (priceFromB == null) {
                    System.out.println(coin);
                    continue;
                }
                Constant.priceBinanceCoin.put(coin, new BigDecimal(priceFromB.getPrice()).setScale(5, RoundingMode.HALF_UP));
                String notifyCoinCode = Constant.notifyCoin.containsKey(coin) ? "N" : "";
                if(new BigDecimal(priceFromB.getPrice()).compareTo(Constant.priceBoughtCoin.get(coin)) == -1){
                    text += Constant.addColor(String.format("%s : %s :%s<br>", coin,
                            Constant.priceBinanceCoin.get(coin), notifyCoinCode), Constant.RED_COLOR);
                }else{
                    text += Constant.addColor(String.format("%s : %s :%s<br>", coin,
                            Constant.priceBinanceCoin.get(coin), notifyCoinCode), Constant.GREEN_COLOR);
                }
            }
            ////////////////////////
            for(Constant.US_STOCK us : Constant.US_STOCK.values()){
                if(!Constant.priceBoughtCoin.containsKey(us.name().toUpperCase())){
                    Constant.priceBoughtCoin.put(us.name().toUpperCase(), new BigDecimal("0"));
                }
                if(!Constant.priceStockUs.containsKey(us.name().toUpperCase())){
                    continue;
                }
                if(Constant.priceStockUs.get(us.name().toUpperCase()).compareTo(Constant.priceBoughtCoin.get(us.name().toUpperCase())) == -1){
                    text += Constant.addColor(String.format("%s : %s :%s<br>", us.name().toUpperCase(),
                            Constant.priceStockUs.get(us.name().toUpperCase()), ""), Constant.RED_COLOR);
                }else{
                    text += Constant.addColor(String.format("%s : %s :%s<br>", us.name().toUpperCase(),
                            Constant.priceStockUs.get(us.name().toUpperCase()), ""), Constant.GREEN_COLOR);
                }
            }
            ///////////////////////

            BigDecimal percentIncrese = BigDecimal.ZERO;
            text += "<br>----------Compare To Old Price----------<br><br>";

            for (String coin : Constant.priceBinanceCoin.keySet()) {
                if (Constant.priceBoughtCoin.get(coin).compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                BigDecimal caculateP = caculatePercent(Constant.priceBinanceCoin.get(coin), coin, Constant.priceBoughtCoin);
                if (Constant.notifyCoin.containsKey(coin)) {
                    if (percentIncrese.compareTo(BigDecimal.ZERO) == 0) {
                        percentIncrese = caculateP;
                    }
                    if (Constant.priceBoughtCoin.get(Constant.KEY_PERCENT).compareTo(BigDecimal.ZERO) == -1) {
                        if (percentIncrese.compareTo(caculateP) == 1) {
                            percentIncrese = caculateP;
                        }
                    } else {
                        if (percentIncrese.compareTo(caculateP) == -1) {
                            percentIncrese = caculateP;
                        }
                    }
                }
                String signal = "";
                if (caculateP.compareTo(BigDecimal.ZERO) == 1 || caculateP.compareTo(BigDecimal.ZERO) == 0) {
                    signal = "+";
                } else {
                    signal = "-";
                }

                String coinNameAndBoughtPrice = String.format("%s(%s)", coin, Constant.priceBoughtCoin.get(coin));

                if (caculateP.compareTo(BigDecimal.ZERO) == 1 || caculateP.compareTo(BigDecimal.ZERO) == 0) {
                    text += Constant.addColor(String.format("%s%s%s%s",
                            coinNameAndBoughtPrice,
                            balanceSpace(coinNameAndBoughtPrice, signal, 20),
                            signal, caculateP.setScale(2, RoundingMode.HALF_UP)), Constant.GREEN_COLOR);
                    text += "<br>";
                } else {
                    text += Constant.addColor(String.format("%s%s%s%s",
                            coinNameAndBoughtPrice,
                            balanceSpace(coinNameAndBoughtPrice, signal, 20),
                            signal, caculateP.setScale(2, RoundingMode.HALF_UP)), Constant.RED_COLOR);
                    text += "<br>";
                }
            }

            ////////////////////////
            for(Constant.US_STOCK us : Constant.US_STOCK.values()){
                if (Constant.priceBoughtCoin.get(us.name().toUpperCase()).compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                if (!Constant.priceStockUs.containsKey(us.name().toUpperCase())) {
                    continue;
                }
                BigDecimal caculateP = caculatePercent(Constant.priceStockUs.get(us.name().toUpperCase()), us.name().toUpperCase(), Constant.priceBoughtCoin);
                String signal = "";
                if (caculateP.compareTo(BigDecimal.ZERO) == 1 || caculateP.compareTo(BigDecimal.ZERO) == 0) {
                    signal = "+";
                } else {
                    signal = "-";
                }

                String coinNameAndBoughtPrice = String.format("%s(%s)", us.name().toUpperCase(), Constant.priceBoughtCoin.get(us.name().toUpperCase()));

                if (caculateP.compareTo(BigDecimal.ZERO) == 1 || caculateP.compareTo(BigDecimal.ZERO) == 0) {
                    text += Constant.addColor(String.format("%s%s%s%s",
                            coinNameAndBoughtPrice,
                            balanceSpace(coinNameAndBoughtPrice, signal, 20),
                            signal, caculateP.setScale(2, RoundingMode.HALF_UP)), Constant.GREEN_COLOR);
                    text += "<br>";
                } else {
                    text += Constant.addColor(String.format("%s%s%s%s",
                            coinNameAndBoughtPrice,
                            balanceSpace(coinNameAndBoughtPrice, signal, 20),
                            signal, caculateP.setScale(2, RoundingMode.HALF_UP)), Constant.RED_COLOR);
                    text += "<br>";
                }
            }
            ///////////////////////

            text += "-------------------------------------------------------<br>";
            text += Constant.KEY_LOOP + ":" + "2" + "<br>";
            text += Constant.KEY_PERCENT + ":" + Constant.priceBoughtCoin.get(Constant.KEY_PERCENT).intValue() + "<br>";
            text += Constant.KEY_PERCENT_ABOUT + ":" +
                    (Constant.priceBoughtCoin.containsKey(Constant.KEY_PERCENT_ABOUT) ?
                            Constant.priceBoughtCoin.get(Constant.KEY_PERCENT_ABOUT).intValue() : "0")
                    + "<br>";


            System.out.println("Percent:" + percentIncrese);
            da.put("DA", text);
            // compare <> 0
            if (percentIncrese.compareTo(BigDecimal.ZERO) == 1 || percentIncrese.compareTo(BigDecimal.ZERO) == -1) {
                da.put("percent", percentIncrese.toString() + " %");
            }

            // if contain KEY_ABOUT
            if(Constant.priceBoughtCoin.containsKey(Constant.KEY_PERCENT_ABOUT)
                    && Constant.priceBoughtCoin.get(Constant.KEY_PERCENT_ABOUT).compareTo(BigDecimal.ZERO) != 0){
                if (percentIncrese.compareTo(BigDecimal.ZERO) == -1) {
                    if (percentIncrese.compareTo(
                            Constant.priceBoughtCoin.get(Constant.KEY_PERCENT_ABOUT)
                                    .multiply(new BigDecimal("-1"))) == -1
                            || percentIncrese.compareTo(
                                    Constant.priceBoughtCoin.get(Constant.KEY_PERCENT_ABOUT)
                                            .multiply(new BigDecimal("-1"))) == 0) {
                        da.put("NO", "A");
                    }
                } else {
                    if (percentIncrese.compareTo(Constant.priceBoughtCoin.get(Constant.KEY_PERCENT_ABOUT)) == 1
                            || percentIncrese.compareTo(Constant.priceBoughtCoin.get(Constant.KEY_PERCENT_ABOUT)) == 0) {
                        da.put("NO", "A");
                    }
                }
                return da;
            }
            // if set < 0 else set >\0
            if (Constant.priceBoughtCoin.get(Constant.KEY_PERCENT).compareTo(BigDecimal.ZERO) == -1) {
                if (percentIncrese.compareTo(Constant.priceBoughtCoin.get(Constant.KEY_PERCENT)) == -1
                        || percentIncrese.compareTo(Constant.priceBoughtCoin.get(Constant.KEY_PERCENT)) == 0) {
                    da.put("NO", "A");
                }
            } else {
                if (percentIncrese.compareTo(Constant.priceBoughtCoin.get(Constant.KEY_PERCENT)) == 1
                        || percentIncrese.compareTo(Constant.priceBoughtCoin.get(Constant.KEY_PERCENT)) == 0) {
                    da.put("NO", "A");
                }
            }
        } catch (InterruptedIOException ex) {
        } catch (IOException e) {
            Log.e("", "", e);
            da.put("DA", "No Access Internet");
        } catch (Exception e) {
            Log.e("", "", e);
            da.put("DA", e.getMessage());
        }
        return da;
    }

    private String balanceSpace(String txt, String signal, Integer spaceStandard) {
        Integer spaceFill = spaceStandard - txt.length();
        String space = "";
        if(signal.contains("-")){
            spaceFill += 20;
        }
        for (int i = 0; i < spaceFill; i++) {
            space += "&nbsp;";
        }
        return space;
    }

    private BigDecimal caculatePercent(BigDecimal price, String coin, ConcurrentSkipListMap<String, BigDecimal> dataCoin) {
        BigDecimal profit = price.subtract(dataCoin.get(coin));
        BigDecimal percent = profit.divide(dataCoin.get(coin), 4, RoundingMode.HALF_UP);
        percent = percent.multiply(new BigDecimal(100));
        return percent;
    }

    public void loadData(Context context) {
        BufferedReader br = null;
        try {
            Constant.priceBoughtCoin.clear();
            Constant.priceBinanceCoin.clear();
            Constant.notifyCoin.clear();
            String fileName = "mydatacoin.txt";
            boolean existed = fileExists(context, fileName);
            if(!existed){
                FileOutputStream fileout = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                outputWriter.write("BTC:0: \n LOOP:5 \n PERCENT:5");
                outputWriter.close();
            }
            FileInputStream fis = context.openFileInput(fileName);
            br = new BufferedReader(new InputStreamReader(new BufferedInputStream(fis)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = line.split(":");
                if(d.length < 2){
                    continue;
                }
                String coinName = d[0].trim().toUpperCase();
                BigDecimal priceCoin = new BigDecimal(d[1].replace("USDT", "").trim());
                Constant.priceBoughtCoin.put(coinName, priceCoin.setScale(5, RoundingMode.HALF_UP));
                try {
                    if (String.valueOf(d[2]).toUpperCase().trim().equals("N")) {
                        Constant.notifyCoin.put(coinName, BigDecimal.ZERO);
                        Constant.KEY_PERCENT_SHOW_PRICE_VALUE = coinName.toUpperCase();
                    }
                } catch (Exception ex) {

                }
            }
            br.close();
        } catch (Exception e) {
            Log.e("", "", e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.e("", "", e);
            }
        }
    }

    public String loadDataToText(Context context) {
        BufferedReader br = null;
        String config = "";
        try {
            String fileName = "mydatacoin.txt";
            FileInputStream fis = context.openFileInput(fileName);
            br = new BufferedReader(new InputStreamReader(new BufferedInputStream(fis)));
            String line;
            while ((line = br.readLine()) != null) {
                config += line + "<br>";
            }
            br.close();
        } catch (Exception e) {
            Log.e("", "", e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.e("", "", e);
            }
        }
        return config;
    }

    public void saveData(Context context, String text) {
        String fileName = "mydatacoin.txt";
        try {
            FileOutputStream fileout = context.openFileOutput("mydatacoin.txt", Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(text);
            outputWriter.close();
            //display file saved message
            this.loadData(context);
            Toast.makeText(context, "File saved successfully!",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("", "", e);
        }
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }
}