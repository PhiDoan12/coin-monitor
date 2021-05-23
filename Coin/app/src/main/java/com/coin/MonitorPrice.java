package com.coin;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class MonitorPrice {
    public BigDecimal getTime() {
        return Constant.priceBoughtCoin.get(Constant.KEY_LOOP);
    }

    public HashMap<String, String> getPrice(Context context) {
        HashMap<String, String> da = new HashMap<>();
        try {
            if (Constant.priceBoughtCoin.size() == 0) {
                loadData(context);
            }

            if (Constant.priceBoughtCoin.size() == 0) {
                return null;
            }

            Constant.priceBinanceCoin.clear();

            String text = "";
            HashMap<String, String> priceMap = Constant.getPriceBinance();
            String colorCoin = "";
            for (String coin : Constant.priceBoughtCoin.keySet()) {
                if (!priceMap.containsKey(coin)) {
                    continue;
                }
                if (coin.equals(Constant.KEY_PERCENT)) {
                    continue;
                }
                if (coin.equals(Constant.KEY_LOOP)) {
                    continue;
                }
                Constant.priceBinanceCoin.put(coin, new BigDecimal(priceMap.get(coin)).setScale(5, RoundingMode.HALF_UP));
                String notifyCoinCode = Constant.notifyCoin.containsKey(coin) ? "N" : "";
                if (new BigDecimal(priceMap.get(coin)).compareTo(Constant.priceBoughtCoin.get(coin)) == -1) {
                    colorCoin = Constant.RED_COLOR;
                    if (Constant.notifyCoin.containsKey(coin)) {
                        colorCoin = Constant.BLACK_COLOR;
                    }
                    text += Constant.addColor(String.format("%s : %s :%s<br>", coin,
                            Constant.priceBinanceCoin.get(coin), notifyCoinCode), colorCoin);
                } else {
                    colorCoin = Constant.GREEN_COLOR;
                    if (Constant.notifyCoin.containsKey(coin)) {
                        colorCoin = Constant.BLACK_COLOR;
                    }
                    text += Constant.addColor(String.format("%s : %s :%s<br>", coin,
                            Constant.priceBinanceCoin.get(coin), notifyCoinCode), colorCoin);
                }
            }

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
                    colorCoin = Constant.GREEN_COLOR;
                    if (Constant.notifyCoin.containsKey(coin)) {
                        colorCoin = Constant.BLACK_COLOR;
                    }
                    text += Constant.addColor(String.format("%s%s%s%s",
                            coinNameAndBoughtPrice,
                            balanceSpace(coinNameAndBoughtPrice, signal, 20),
                            signal, caculateP.setScale(2, RoundingMode.HALF_UP)), colorCoin);
                    text += "<br>";
                } else {
                    colorCoin = Constant.RED_COLOR;
                    if (Constant.notifyCoin.containsKey(coin)) {
                        colorCoin = Constant.BLACK_COLOR;
                    }
                    text += Constant.addColor(String.format("%s%s%s%s",
                            coinNameAndBoughtPrice,
                            balanceSpace(coinNameAndBoughtPrice, signal, 20),
                            signal, caculateP.setScale(2, RoundingMode.HALF_UP)), colorCoin);
                    text += "<br>";
                }
            }

            /****************************/
            text += "<br>";
            if (Constant.getFearAndGreedy().getStatus().toUpperCase().contains("FEAR")) {
                text += Constant.addColor(Constant.getFearAndGreedy().getStatus() + " => " + Constant.getFearAndGreedy().getValue(), Constant.RED_COLOR) + " <br>";
            } else {
                text += Constant.addColor(Constant.getFearAndGreedy().getStatus() + " => " + Constant.getFearAndGreedy().getValue(), Constant.GREEN_COLOR) + " <br>";
            }

            da.put("DA", text);
        } catch (Exception e) {
            da.put("DA", e.getMessage());
            da.put("ERROR", "No Access Internet. Error:" + e);
            e.printStackTrace();
        }
        return da;
    }

    private String balanceSpace(String txt, String signal, Integer spaceStandard) {
        Integer spaceFill = spaceStandard - txt.length();
        String space = "";
        if (signal.contains("-")) {
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

    public synchronized void loadData(Context context) {
        BufferedReader br = null;
        try {
            Constant.priceBoughtCoin.clear();
            Constant.priceBinanceCoin.clear();
            Constant.notifyCoin.clear();
            String fileName = "mydatacoin.txt";
            boolean existed = fileExists(context, fileName);
            if (!existed) {
                FileOutputStream fileout = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                outputWriter.write("BTC:0: \n LOOP:5000 \n PERCENT:5000");
                outputWriter.close();
            }
            FileInputStream fis = context.openFileInput(fileName);
            br = new BufferedReader(new InputStreamReader(new BufferedInputStream(fis)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = line.split(":");
                if (d.length < 2) {
                    continue;
                }
                String coinName = d[0].trim().toUpperCase();
                BigDecimal priceCoin = new BigDecimal(d[1].replace("USDT", "").trim());
                Constant.priceBoughtCoin.put(coinName, priceCoin.setScale(5, RoundingMode.HALF_UP));
                try {
                    if (String.valueOf(d[2]).toUpperCase().trim().equals("N")) {
                        Constant.notifyCoin.put(coinName, BigDecimal.ZERO);
                    }
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
            }
            // fix missing a field
            if (!Constant.priceBoughtCoin.containsKey(Constant.KEY_PERCENT)) {
                Constant.priceBoughtCoin.put(Constant.KEY_PERCENT, new BigDecimal("5000"));
            }
            if (!Constant.priceBoughtCoin.containsKey(Constant.KEY_LOOP)) {
                Constant.priceBoughtCoin.put(Constant.KEY_LOOP, new BigDecimal("5000"));
            }
            Constant.loopTIme = Constant.priceBoughtCoin.get(Constant.KEY_LOOP);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
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
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    public void saveData(Context context, String text) {
        try {
            text += Constant.KEY_LOOP + ":5000 \n";
            text += Constant.KEY_PERCENT + ":5000 \n";
            FileOutputStream fileout = context.openFileOutput("mydatacoin.txt", Context.MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write(text);
            outputWriter.close();
            //display file saved message
            this.loadData(context);
            Toast.makeText(context, "File saved successfully!",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists() || file.length() == 0) {
            return false;
        }
        return true;
    }
}
