package com.trien.bnalert.loaders;

import com.trien.bnalert.models.PriceTicker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.trien.bnalert.utils.QueryUtils.PARAM_END_TIME;
import static com.trien.bnalert.utils.QueryUtils.PARAM_INTERVAL;
import static com.trien.bnalert.utils.QueryUtils.PARAM_START_TIME;
import static com.trien.bnalert.utils.QueryUtils.PARAM_SYMBOL;
import static com.trien.bnalert.utils.QueryUtils.TAG_CHART;
import static com.trien.bnalert.utils.QueryUtils.TAG_TICKER_PRICE;
import static com.trien.bnalert.utils.QueryUtils.TAG_TICKER_PRICE_24H;

public interface BinanceService {

    // Request methods, URL tags, parameters, and headers can be specified in the annotations
    // Detailed instruction: https://square.github.io/retrofit/

    // get PriceTicker list to extract the coin list
    @GET(TAG_TICKER_PRICE)
    Call<List<PriceTicker>> getPriceTickersForCoinListExtraction();

    // get ChartValue for drawing chart
    @GET(TAG_CHART)
    Call<String> getChartValue(@Query(PARAM_SYMBOL) String coinPair,
                                     @Query(PARAM_INTERVAL) String interval,
                                     @Query(PARAM_START_TIME) long startTime,
                                     @Query(PARAM_END_TIME) long endTime);

    // get PriceTicker list to see price change in 24h
    @GET(TAG_TICKER_PRICE_24H)
    Call<List<PriceTicker>> getPriceTickers24h();

    @POST("users/new")
    Call<PriceTicker> createUser(@Body PriceTicker priceTicker);
}
