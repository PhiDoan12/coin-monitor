/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.trien.bnalert.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import com.trien.bnalert.R;
import com.trien.bnalert.loaders.BinanceService;
import com.trien.bnalert.models.ChartInterval;
import com.trien.bnalert.models.ChartValue;
import com.trien.bnalert.models.PriceTicker;
import com.trien.bnalert.models.Trade;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Helper methods related to requesting and receiving data from Binance.com.
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * base url for retrieving data from binance
     */
    public static final String BASE_URL = "https://api.binance.com";

    /**
     * url tag for retrieving recent trades
     */
    public static final String TAG_TRADE = "/api/v1/trades";

    /**
     * url tag for retrieving recent trades
     */
    public static final String TAG_ACC_INFO = "/api/v3/account";

    /**
     * url tag for retrieving recent trades
     */
    public static final String TAG_MY_TRADE_HISTORY = "/api/v3/myTrades";

    /**
     * url tag for retrieving price tickers
     */
    public static final String TAG_TICKER_PRICE = "/api/v3/ticker/price";

    /**
     * url tag for retrieving price tickers 24h
     */
    public static final String TAG_TICKER_PRICE_24H = "/api/v1/ticker/24hr";

    /**
     * url tag for retrieving price tickers 24h
     */
    public static final String TAG_CHART = "/api/v1/klines";

    /**
     * param symbol is to spicify which coin pair to retrieve
     */
    public static final String PARAM_SYMBOL = "symbol";

    /**
     * maximum limit of responded results is 500
     */
    public static final String PARAM_LIMIT = "limit";

    /**
     * maximum limit of responded results is 500
     */
    public static final String PARAM_INTERVAL = "interval";

    /**
     * maximum limit of responded results is 500
     */
    public static final String PARAM_START_TIME = "startTime";

    /**
     * maximum limit of responded results is 500
     */
    public static final String PARAM_END_TIME = "endTime";

    /**
     * maximum limit of responded results is 500
     */
    public static final int RESULT_LIMIT = 200;

    public static final String KLINE_INTERVAL_1MINUTE = "1m";
    public static final String KLINE_INTERVAL_3MINUTE = "3m";
    public static final String KLINE_INTERVAL_5MINUTE = "5m";
    public static final String KLINE_INTERVAL_15MINUTE = "15m";
    public static final String KLINE_INTERVAL_30MINUTE = "30m";
    public static final String KLINE_INTERVAL_1HOUR = "1h";
    public static final String KLINE_INTERVAL_2HOUR = "2h";
    public static final String KLINE_INTERVAL_4HOUR = "4h";
    public static final String KLINE_INTERVAL_6HOUR = "6h";
    public static final String KLINE_INTERVAL_8HOUR = "8h";
    public static final String KLINE_INTERVAL_12HOUR = "12h";
    public static final String KLINE_INTERVAL_1DAY = "1d";
    public static final String KLINE_INTERVAL_3DAY = "3d";
    public static final String KLINE_INTERVAL_1WEEK = "1w";
    public static final String KLINE_INTERVAL_1MONTH = "1M";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {

    }

    public static BinanceService getBinanceServiceWithGson() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(QueryUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(BinanceService.class);
    }

    /**
     * Query the dataset and return a list of {@link Trade} objects.
     */
    public static List<Trade> fetchRecentTradeData(Context context, String tradingPair) {

        Uri baseUri = Uri.parse(BASE_URL + TAG_TRADE);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter(PARAM_SYMBOL, tradingPair);
        uriBuilder.appendQueryParameter(PARAM_LIMIT, String.valueOf(RESULT_LIMIT));

        // Create URL object
        URL url = createUrl(uriBuilder.toString());
        Timber.d(String.valueOf(url));
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, context);
        } catch (IOException e) {
            Timber.e("Problem making the HTTP request.", e);
        }
        Timber.tag("trientrade").d(jsonResponse);
        // Extract relevant fields from the JSON response and create a list of {@link Trade}s
        List<Trade> trades = extractRecentTradeFromJson(jsonResponse);
        Timber.d(String.valueOf(trades));
        // Return the list of {@link Trade}s
        return trades;
    }

    public static ChartValue fetchChartData(Context context, String symbol, long startTimeInput, long endTimeInput) {

        Uri baseUri = Uri.parse(BASE_URL + TAG_CHART);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        long timeInMilisLong = endTimeInput - startTimeInput;
        String interval;

        if (timeInMilisLong / 100 < 900000L) // 900000 milliseconds = 15 minutes
            interval = KLINE_INTERVAL_15MINUTE;
        else if (timeInMilisLong / 100 < 1800000L)
            interval = KLINE_INTERVAL_30MINUTE;
        else if (timeInMilisLong / 100 < 3600000L)
            interval = KLINE_INTERVAL_1HOUR;
        else if (timeInMilisLong / 100 < 7200000L)
            interval = KLINE_INTERVAL_2HOUR;
        else if (timeInMilisLong / 100 < 14400000L)
            interval = KLINE_INTERVAL_4HOUR;
        else if (timeInMilisLong / 100 < 21600000L)
            interval = KLINE_INTERVAL_6HOUR;
        else if (timeInMilisLong / 100 < 28800000L)
            interval = KLINE_INTERVAL_8HOUR;
        else if (timeInMilisLong / 100 < 43200000L)
            interval = KLINE_INTERVAL_12HOUR;
        else if (timeInMilisLong / 100 < 86400000L)
            interval = KLINE_INTERVAL_1DAY;
        else if (timeInMilisLong / 100 < 259200000L)
            interval = KLINE_INTERVAL_3DAY;
        else if (timeInMilisLong / 100 < 604800000L)
            interval = KLINE_INTERVAL_1WEEK;
        else
            interval = KLINE_INTERVAL_1MONTH;

        uriBuilder.appendQueryParameter(PARAM_SYMBOL, symbol);
        uriBuilder.appendQueryParameter(PARAM_INTERVAL, interval);
        uriBuilder.appendQueryParameter(PARAM_START_TIME, String.valueOf(startTimeInput));
        uriBuilder.appendQueryParameter(PARAM_END_TIME, String.valueOf(endTimeInput));
        /*uriBuilder.appendQueryParameter(PARAM_START_TIME, String.valueOf(System.currentTimeMillis() - 86400000));
        uriBuilder.appendQueryParameter(PARAM_END_TIME, String.valueOf(System.currentTimeMillis()));*/

        // Create URL object
        URL url = createUrl(uriBuilder.toString());
        Timber.d(String.valueOf(url));
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, context);
        } catch (IOException e) {
            Timber.e("Problem making the HTTP request.", e);
        }

        Timber.tag("trienchart").d(jsonResponse);
        // Extract relevant fields from the JSON response and create a list of {@link Trade}s
        ChartValue chartValueData = extractChartsFromReponse(jsonResponse);
        Timber.d(String.valueOf(chartValueData));
        // Return the list of {@link Trade}s
        return chartValueData;
    }

    public static String computeChartInterval (long startTime, long endTime) {
        long timeInMilisLong = endTime - startTime;
        String interval;

        if (timeInMilisLong / 100 < 900000L) // 900000 milliseconds = 15 minutes
            interval = KLINE_INTERVAL_15MINUTE;
        else if (timeInMilisLong / 100 < 1800000L)
            interval = KLINE_INTERVAL_30MINUTE;
        else if (timeInMilisLong / 100 < 3600000L)
            interval = KLINE_INTERVAL_1HOUR;
        else if (timeInMilisLong / 100 < 7200000L)
            interval = KLINE_INTERVAL_2HOUR;
        else if (timeInMilisLong / 100 < 14400000L)
            interval = KLINE_INTERVAL_4HOUR;
        else if (timeInMilisLong / 100 < 21600000L)
            interval = KLINE_INTERVAL_6HOUR;
        else if (timeInMilisLong / 100 < 28800000L)
            interval = KLINE_INTERVAL_8HOUR;
        else if (timeInMilisLong / 100 < 43200000L)
            interval = KLINE_INTERVAL_12HOUR;
        else if (timeInMilisLong / 100 < 86400000L)
            interval = KLINE_INTERVAL_1DAY;
        else if (timeInMilisLong / 100 < 259200000L)
            interval = KLINE_INTERVAL_3DAY;
        else if (timeInMilisLong / 100 < 604800000L)
            interval = KLINE_INTERVAL_1WEEK;
        else
            interval = KLINE_INTERVAL_1MONTH;
        return interval;
    }

    /**
     * Query the dataset and return a {@link PriceTicker} object.
     */
    public static List<PriceTicker> fetchAllSymbolsFromBinance(Context context) {

        Uri baseUri = Uri.parse(BASE_URL + TAG_TICKER_PRICE);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Create URL object
        URL url = createUrl(uriBuilder.toString());
        Timber.d(String.valueOf(url));
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, context);
        } catch (IOException e) {
            Timber.e("Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Trade}s
        List<PriceTicker> symbolList = extractAllSymbolsFromBinance(jsonResponse);
        Timber.d(String.valueOf(symbolList));
        // Return the list of price tickers
        return symbolList;
    }

    /**
     * Query the dataset and return a list of {@link PriceTicker} objects.
     */
    public static List<PriceTicker> fetchPriceTickersFromBinance(Context context) {

        Uri baseUri = Uri.parse(BASE_URL + TAG_TICKER_PRICE_24H);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Create URL object
        URL url = createUrl(uriBuilder.toString());
        Timber.d(String.valueOf(url));
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, context);
        } catch (IOException e) {
            Timber.e("Problem making the HTTP request.", e);
        }
        Timber.tag("trienticker").d(jsonResponse);
        // Extract relevant fields from the JSON response and create a list of {@link Trade}s
        List<PriceTicker> priceTickers = extractPriceTickerFromBinance(jsonResponse);
        Timber.d(String.valueOf(priceTickers));
        // Return the list of price tickers
        return priceTickers;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Timber.e("Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url, Context context) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty(context.getResources().getString(R.string.http_header),
                    context.getResources().getString(R.string.binance_api_key));
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Timber.e("Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Timber.e("Problem retrieving the Trade JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Trade} objects that has been built up from
     * parsing the given JSON response.
     */
    public static List<Trade> extractRecentTradeFromJson(String tradeJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(tradeJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding trades to
        List<Trade> trades = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONArray baseJsonResponse = new JSONArray(tradeJSON);

            // For each trade in the tradeArray, create an {@link Trade} object
            for (int i = 0; i < baseJsonResponse.length(); i++) {

                // Get a single trade at position i within the list of trades
                JSONObject currentTrade = baseJsonResponse.getJSONObject(i);

                // Extract values in accordance with Trade class' properties
                long id = Long.parseLong(currentTrade.get("id").toString());
                double price = Double.parseDouble(currentTrade.get("price").toString());
                double quantity = Double.parseDouble(currentTrade.get("qty").toString());
                long time = Long.parseLong(currentTrade.get("time").toString());

                // Create a new {@link Trade} object
                // and url from the JSON response.
                Trade trade = new Trade(id, price, quantity, time);

                // Add the new {@link Trade} to the list of trades.
                trades.add(trade);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Timber.tag("QueryUtils").e("Problem parsing the trade JSON results", e);
        }

        // Return the list of trades
        return trades;
    }

    /**
     * Return a list of {@link PriceTicker} objects that has been built up from
     * parsing the given JSON response.
     */
    public static List<PriceTicker> extractPriceTickerFromBinance(String priceJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(priceJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding trades to
        List<PriceTicker> priceTickers = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONArray baseJsonResponse = new JSONArray(priceJSON);

            // For each trade in the tradeArray, create an {@link Trade} object
            for (int i = 0; i < baseJsonResponse.length(); i++) {

                // Get a single trade at position i within the list of trades
                JSONObject ticker = baseJsonResponse.getJSONObject(i);

                // Extract values in accordance with Trade class' properties
                String symbol = ticker.get("symbol").toString();
                double price = Double.parseDouble(ticker.get("lastPrice").toString());
                double change = Double.parseDouble(ticker.get("priceChangePercent").toString());

                // Create a new PriceTicker object
                // and url from the JSON response.
                PriceTicker priceTicker = new PriceTicker(symbol, price, change);

                // Add the new PriceTicker object to the list of PriceTickers.
                priceTickers.add(priceTicker);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Timber.tag("QueryUtils").e("Problem parsing the trade JSON results", e);
        }

        // Return the list of trades
        return priceTickers;
    }

    /**
     * Return a list of ChartValue objects that has been built up from
     * parsing the given JSON response.
     */
    public static ChartValue extractChartsFromReponse(String response) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(response)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding trades to
        List<ChartInterval> chartIntervals = new ArrayList<>();
        ChartValue chartValue = null;

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONArray baseJsonResponse = new JSONArray(response);
            double volume = 0;

            // For each trade in the tradeArray, create an {@link Trade} object
            for (int i = 0; i < baseJsonResponse.length(); i++) {

                // Get a single trade at position i within the list of trades
                JSONArray data = baseJsonResponse.getJSONArray(i);

                long closeTime = Long.parseLong(data.get(6).toString());
                double openPrice = Double.parseDouble(data.get(1).toString());
                double highPrice = Double.parseDouble(data.get(2).toString());
                double lowPrice = Double.parseDouble(data.get(3).toString());
                double closePrice = Double.parseDouble(data.get(4).toString());
                double avgPrice = (openPrice + highPrice + lowPrice + closePrice) / 4;

                volume += Double.parseDouble(data.get(5).toString());

                // Create a new {@link Trade} object
                // and url from the JSON response.
                ChartInterval interval = new ChartInterval(closePrice, closeTime);

                // Add the new {@link Trade} to the list of trades.
                chartIntervals.add(interval);
            }

            double startPrice0 = Double.parseDouble(baseJsonResponse.getJSONArray(0).get(1).toString());
            double startPrice1 = Double.parseDouble(baseJsonResponse.getJSONArray(0).get(2).toString());
            double startPrice2 = Double.parseDouble(baseJsonResponse.getJSONArray(0).get(3).toString());
            double startPrice3 = Double.parseDouble(baseJsonResponse.getJSONArray(0).get(4).toString());

            double yyy = (startPrice0 + startPrice3) / 2;

            double endprice0 = Double.parseDouble(baseJsonResponse.getJSONArray(baseJsonResponse.length() - 1).get(1).toString());
            double endprice1 = Double.parseDouble(baseJsonResponse.getJSONArray(baseJsonResponse.length() - 1).get(2).toString());
            double endprice2 = Double.parseDouble(baseJsonResponse.getJSONArray(baseJsonResponse.length() - 1).get(3).toString());
            double endprice3 = Double.parseDouble(baseJsonResponse.getJSONArray(baseJsonResponse.length() - 1).get(4).toString());

            double xxx = (endprice0 + endprice3) / 2;

            double change1 = (xxx / yyy) - 1;
            double change2 = (endprice3 / startPrice0) - 1;
            double change3 = (endprice0 / startPrice3) - 1;
            double change4 = (endprice3 / startPrice3) - 1;


            long startTime = Long.parseLong(baseJsonResponse.getJSONArray(0).get(0).toString());
            long endTime = Long.parseLong(baseJsonResponse.getJSONArray(baseJsonResponse.length() - 1).get(6).toString());
            double startPrice = Double.parseDouble(baseJsonResponse.getJSONArray(0).get(1).toString());
            double endPrice = Double.parseDouble(baseJsonResponse.getJSONArray(baseJsonResponse.length() - 1).get(4).toString());

            double change = ((endPrice / startPrice) - 1) * 100;
            chartValue = new ChartValue(startTime, endTime, startPrice, endPrice, change, volume, chartIntervals);

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Timber.tag("QueryUtils").e("Problem parsing the trade JSON results", e);
        }

        // Return the list of trades
        return chartValue;
    }

    /**
     * Return a list of {@link PriceTicker} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<PriceTicker> extractAllSymbolsFromBinance(String priceJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(priceJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding trades to
        List<PriceTicker> tickerList = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONArray baseJsonResponse = new JSONArray(priceJSON);

            // For each trade in the tradeArray, create an {@link Trade} object
            for (int i = 0; i < baseJsonResponse.length(); i++) {

                // Get a single trade at position i within the list of trades
                JSONObject ticker = baseJsonResponse.getJSONObject(i);

                String coinPair = ticker.getString("symbol");
                double price = Double.parseDouble(ticker.getString("price"));

                // Create a new PriceTicker object
                // and url from the JSON response.
                PriceTicker priceTicker = new PriceTicker(coinPair, price);

                // Add the new PriceTicker object to the list of PriceTickers.
                tickerList.add(priceTicker);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Timber.tag("QueryUtils").e("Problem parsing the trade JSON results", e);
        }

        // Return the list of trades
        return tickerList;
    }

    public static boolean isInternetConnected(Context context) {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;

        // If there is a network connection, return true and vice versa
        return networkInfo != null && networkInfo.isConnected();
    }

}
