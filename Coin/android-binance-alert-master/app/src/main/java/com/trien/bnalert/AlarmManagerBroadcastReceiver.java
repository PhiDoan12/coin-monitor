package com.trien.bnalert;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trien.bnalert.models.Alert;
import com.trien.bnalert.models.PriceTicker;
import com.trien.bnalert.utils.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

import timber.log.Timber;

import static android.content.Context.ALARM_SERVICE;
import static com.trien.bnalert.utils.QueryUtils.isInternetConnected;

// BroadcastReceiver class to schedule alarm for waking the CPU and perform updating app widget
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    Context mContext;
    public static final String PAIR_EXTRA = "pair_extra";
    static int count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Bundle myExtras = intent.getExtras();

        if (myExtras != null) {
            int id = myExtras.getInt("id", 0);
            final String coin = myExtras.getString("coin", "coin");
            String comparator = myExtras.getString("comparator", "comparator");
            double comparedToValue = myExtras.getDouble("comparedToValue", 0);
            String baseCoin = myExtras.getString("baseCoin", "baseCoin");
            boolean isPersistent = myExtras.getBoolean("isPersistent", true);
            double interval = myExtras.getDouble("interval", 0);
            boolean vibrate = myExtras.getBoolean("vibrate", true);
            boolean playSound = myExtras.getBoolean("playSound", true);
            boolean flashing = myExtras.getBoolean("flashing", true);
            boolean isActive = myExtras.getBoolean("isActive", true);
            long dateAdded = myExtras.getLong("dateAdded", System.currentTimeMillis());

            final Alert alert = new Alert(id, coin, comparator, comparedToValue, baseCoin, isPersistent, interval, vibrate, playSound, flashing, isActive, dateAdded);
            Timber.d(alert.toString());
            if (isInternetConnected(context)) {

                performPriceCheck(context, alert);
            }
        }
    }

    public static void performPriceCheck(final Context context, final Alert alert) {
        // Instantiate the RequestQueue.
        final RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest;

        String pricedCoin = alert.getCoin();
        String baseCoin = alert.getBaseCoin();
        String pair = pricedCoin + baseCoin;
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + pricedCoin + baseCoin;

        if (pair.equals("BTCBNB") || pair.equals("BTCETH") || pair.equals("ETHBNB")) {

            url = "https://api.binance.com/api/v3/ticker/price?symbol=" + baseCoin + pricedCoin;

            stringRequest = getStringRequestBaseCoinVsBaseCoinPair(context, url, alert);

        } else {

            stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            PriceTicker priceTicker = extractPriceTickerOnePairFromBinance(response);

                            analyseNormalResponse(context, priceTicker, alert);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Timber.d("trien1", "trien");
                    // if (error.getMessage().contains("1121")) {
//                        Timber.d("trien2", error.getMessage());

                    final String pricedCoin = alert.getCoin();
                    final String wantedBaseCoin = alert.getBaseCoin();
                    if (wantedBaseCoin.equals("USDT")) {

                        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + pricedCoin + "BTC";

                        StringRequest stringRequest = getStringRequestNonExistingPairInUsdt(context, url, alert, queue);
                        queue.add(stringRequest);
                    } else {

                        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + pricedCoin + "BTC";

                        StringRequest stringRequest = getStringRequestNonTradingPairInBnbOrEth(context, wantedBaseCoin, url, alert, queue);
                        queue.add(stringRequest);
                    }

                   /* } else {
                        Toast.makeText(getApplicationContext(), "Unable to fetch data: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }*/
                }

            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return super.getParams();
                }
            };

        }
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    @NonNull
    private static StringRequest getStringRequestNonExistingPairInUsdt(final Context context, String url, final Alert alert, final RequestQueue queue) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        final PriceTicker priceTickerByBitcoin = extractPriceTickerOnePairFromBinance(response);

                        String url = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        PriceTicker priceTickerBtcUsdt = extractPriceTickerOnePairFromBinance(response);
                                        double price = priceTickerByBitcoin.getPrice() * priceTickerBtcUsdt.getPrice();

                                        PriceTicker priceTicker = new PriceTicker(alert.getCoin() + alert.getBaseCoin(), price);
                                        analyseNormalResponse(context, priceTicker, alert);
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Timber.d("trien3", error.getMessage());
                            }
                        });
                        queue.add(stringRequest);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.d("trien4", error.getMessage());
            }
        });
    }

    @NonNull
    private static StringRequest getStringRequestNonTradingPairInBnbOrEth(final Context context, final String wantedBaseCoin, String url, final Alert alert, final RequestQueue queue) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        final PriceTicker priceTicker1 = extractPriceTickerOnePairFromBinance(response);

                        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + wantedBaseCoin + "BTC";

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        PriceTicker priceTicker2 = extractPriceTickerOnePairFromBinance(response);
                                        double price = priceTicker1.getPrice() / priceTicker2.getPrice();

                                        PriceTicker priceTicker = new PriceTicker(alert.getCoin() + alert.getBaseCoin(), price);
                                        analyseNormalResponse(context, priceTicker, alert);
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Timber.d("trien3", error.getMessage());

                            }
                        });
                        queue.add(stringRequest);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.d("trien4", error.getMessage());

            }
        });
    }

    @NonNull
    private static StringRequest getStringRequestBaseCoinVsBaseCoinPair(final Context context, final String url, final Alert alert) {
        // Request a string response from the provided URL.
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        PriceTicker priceTicker = extractPriceTickerOnePairFromBinance(response);
                        if (priceTicker != null) {
                            double price = priceTicker.getPrice();
                            if (price != 0)
                                price = 1 / price;
                            priceTicker.setPrice(price);
                        }

                        analyseNormalResponse(context, priceTicker, alert);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.d("trien", error.getMessage());

            }
        });
    }

    private static void analyseNormalResponse(Context context, PriceTicker priceTicker, Alert alert) {

        double price = priceTicker.getPrice();
        String comparator = alert.getComparator();
        double comparedToValue = alert.getComparedToValue();
        boolean conditionSatisfied;

        if (comparator.equals(">")) {
            conditionSatisfied = Double.compare(price, comparedToValue) > 0;
        } else {
            conditionSatisfied = Double.compare(price, comparedToValue) < 0;
        }

        if (conditionSatisfied) {

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(PAIR_EXTRA, priceTicker != null ? priceTicker.getCoinPair() : null);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            int iconResourceId = getIconResourceId(alert);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, String.valueOf(alert.getId()))
                    .setSmallIcon(iconResourceId)
                    //.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), iconResourceId))
                    .setContentTitle("Price reached")
                    .setContentText(String.format(Locale.getDefault(), "1 %s = %s %s (%s %s)", alert.getCoin(), price, alert.getBaseCoin(), comparator, comparedToValue))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setChannelId(Constant.CHANNEL_ID)
                    .setAutoCancel(true);

//            if (alert.isVibrate()) mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
//            if (alert.isPlaySound()) mBuilder.setSound(alarmSound);
//            if (alert.isFlashing()) mBuilder.setLights(Color.RED, 3000, 3000);

//            Random rand = new Random();
//            int notificationId = rand.nextInt(2000000000) + 1;
            // notificationId is a unique int for each notification that you must define
            Constant.notificationManager.notify(alert.getCoin().hashCode(), mBuilder.build());

            // if this alert is not not persistent, then we just cancel the alarm/job
            if (!alert.isPersistent()) {
                cancelAlarm(context, alert.getId());
            }
        }
    }

    public static void cancelAlarm(Context context, int requestCodeOrNotificationId) {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                requestCodeOrNotificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private static int getIconResourceId(Alert alert) {
        int iconResourceId = 0;
        switch (alert.getBaseCoin()) {
            case "USDT":
                iconResourceId = R.drawable.ic_usdt_small;
                break;
            case "BTC":
                iconResourceId = R.drawable.ic_btc_small;
                break;
            case "BNB":
                iconResourceId = R.drawable.ic_bnb_small;
                break;
            case "ETH":
                iconResourceId = R.drawable.ic_eth_small;
                break;
        }
        return iconResourceId;
    }

    // Return a {@link PriceTicker} object for one coin pair.

    public static PriceTicker extractPriceTickerOnePairFromBinance(String priceJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(priceJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding trades to
        PriceTicker priceTicker = null;

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(priceJSON);

            // Extract values in accordance with PriceTicker class' properties
            String coinPair = baseJsonResponse.getString("symbol");
            double price = Double.parseDouble(baseJsonResponse.getString("price"));

            // Create a new PriceTicker object
            // and url from the JSON response.
            priceTicker = new PriceTicker(coinPair, price);

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Timber.e("Problem parsing the trade JSON results", e);
        }

        // Return priceTicker
        return priceTicker;
    }
}