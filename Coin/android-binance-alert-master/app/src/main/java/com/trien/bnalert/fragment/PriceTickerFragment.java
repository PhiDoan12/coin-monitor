package com.trien.bnalert.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trien.bnalert.R;
import com.trien.bnalert.adapters.PriceTickerAdapter;
import com.trien.bnalert.models.ChartInterval;
import com.trien.bnalert.models.ChartValue;
import com.trien.bnalert.models.PriceTicker;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

import static com.trien.bnalert.utils.QueryUtils.BASE_URL;
import static com.trien.bnalert.utils.QueryUtils.PARAM_END_TIME;
import static com.trien.bnalert.utils.QueryUtils.PARAM_INTERVAL;
import static com.trien.bnalert.utils.QueryUtils.PARAM_START_TIME;
import static com.trien.bnalert.utils.QueryUtils.PARAM_SYMBOL;
import static com.trien.bnalert.utils.QueryUtils.TAG_CHART;
import static com.trien.bnalert.utils.QueryUtils.TAG_TICKER_PRICE_24H;
import static com.trien.bnalert.utils.QueryUtils.computeChartInterval;
import static com.trien.bnalert.utils.QueryUtils.extractChartsFromReponse;
import static com.trien.bnalert.utils.QueryUtils.extractPriceTickerFromBinance;
import static com.trien.bnalert.utils.TextUtils.dateInMilisToString;
import static com.trien.bnalert.utils.TextUtils.dateStringToCalendar;
import static com.trien.bnalert.utils.TextUtils.stringToDateInMilis;
import static java.lang.Boolean.FALSE;

public class PriceTickerFragment extends Fragment implements AdapterView.OnItemClickListener {

    private Unbinder unbinder;

    @BindView(R.id.recycler_view_prices)
    RecyclerView mRecyclerView;

    @BindView(R.id.loading_indicator)
    View loadingIndicator;
    @BindView(R.id.tv_emptyTv)
    TextView emptyTextView;
    @BindView(R.id.tv_price_label)
    TextView priceLabel;
    @BindView(R.id.tv_symbol_label)
    TextView symbolLabel;
    @BindView(R.id.tv_change_label)
    TextView changeLabel;

    @BindView(R.id.tv_price)
    TextView priceTextView;
    @BindView(R.id.tv_percent_change)
    TextView changeTextView;
    @BindView(R.id.tv_volume)
    TextView volumeTextView;

    @BindView(R.id.tv_start_time)
    TextView startTimeTextView;
    @BindView(R.id.tv_end_time)
    TextView endTimeTextView;

    @BindView(R.id.spinner_usdt)
    Spinner spinnerUsdt;
    @BindView(R.id.spinner_bnb)
    Spinner spinnerBnb;
    @BindView(R.id.spinner_eth)
    Spinner spinnerEth;
    @BindView(R.id.spinner_btc)
    Spinner spinnerBtc;
    @BindView(R.id.spinner_basecoin_1)
    Spinner spinnerBaseCoin1;
    @BindView(R.id.spinner_basecoin_2)
    Spinner spinnerBaseCoin2;
    @BindView(R.id.spinner_time_picker)
    Spinner spinnerTimePicker;

    private PriceTickerAdapter mRecyclerViewAdapter;
    ArrayAdapter<CharSequence> adapterSpinnerUsdt;
    ArrayAdapter<CharSequence> adapterTimePicker;

    @BindView(R.id.chart)
    LineChart chart;
    static boolean isFirstTimePopulated = true;

    public static final String VOLLEY_REQUEST_TAG = "volley_tag";
    RequestQueue requestQueue;

    @Override
    public void onStop() {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(VOLLEY_REQUEST_TAG);
        }
    }

    // Declare Context variable at class level in Fragment
    private Context mContext;

    // When the fragment is not yet attached, or was detached during the end of its lifecycle,
    // getContext() will return null. To avoid this, Initialise mContext from onAttach().
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_prices,
                container, false);

        requestQueue = Volley.newRequestQueue(mContext);
        unbinder = ButterKnife.bind(this, rootView);

        DatePickerOnClick datePickerOnClick = new DatePickerOnClick();
        startTimeTextView.setOnClickListener(datePickerOnClick);
        startTimeTextView.setText(dateInMilisToString(System.currentTimeMillis() - 86400000L));
        endTimeTextView.setOnClickListener(datePickerOnClick);
        endTimeTextView.setText(dateInMilisToString(System.currentTimeMillis()));

        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterTimePicker = ArrayAdapter.createFromResource(mContext,
                R.array.time_frame_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterTimePicker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerTimePicker.setAdapter(adapterTimePicker);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterBaseCoin1 = ArrayAdapter.createFromResource(mContext,
                R.array.base_coin_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBaseCoin1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBaseCoin1.setAdapter(adapterBaseCoin1);
        spinnerBaseCoin1.setSelection(adapterBaseCoin1.getPosition("USDT"));

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterBaseCoin2 = ArrayAdapter.createFromResource(mContext,
                R.array.base_coin_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterBaseCoin2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerBaseCoin2.setAdapter(adapterBaseCoin2);
        spinnerBaseCoin2.setOnItemSelectedListener(new OnItemSelectedListenerForBaseCoin2());
        spinnerBaseCoin2.setSelection(adapterBaseCoin2.getPosition("USDT"));

        // set up adapter
        mRecyclerViewAdapter = new PriceTickerAdapter(mContext);
        mRecyclerViewAdapter.setOnItemClickListener(this);

        // set up recycler view
        mRecyclerView.setHasFixedSize(false);

        // set up layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, FALSE);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        // make mRecyclerView not scrollable so it scroll along with it container parent
        mRecyclerView.setNestedScrollingEnabled(false);

        priceLabel.setOnClickListener(new View.OnClickListener() {

            boolean clickedFirstTime = true;

            @Override
            public void onClick(View v) {
                if (clickedFirstTime) {
                    priceLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up, 0);
                    symbolLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    changeLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    mRecyclerViewAdapter.sortByPriceAscending();
                    clickedFirstTime = false;
                } else {
                    priceLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
                    symbolLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    changeLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    mRecyclerViewAdapter.sortByPriceDescending();
                    clickedFirstTime = true;
                }
            }
        });

        symbolLabel.setOnClickListener(new View.OnClickListener() {

            boolean clickedFirstTime = true;

            @Override
            public void onClick(View v) {
                if (clickedFirstTime) {
                    symbolLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up, 0);
                    priceLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    changeLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    mRecyclerViewAdapter.sortBySymbolAscending();
                    clickedFirstTime = false;
                } else {
                    symbolLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
                    priceLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    changeLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    mRecyclerViewAdapter.sortBySymbolDescending();
                    clickedFirstTime = true;
                }
            }
        });

        changeLabel.setOnClickListener(new View.OnClickListener() {

            boolean clickedFirstTime = true;

            @Override
            public void onClick(View v) {
                if (clickedFirstTime) {
                    changeLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_up, 0);
                    priceLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    symbolLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    mRecyclerViewAdapter.sortByChangeAscending();
                    clickedFirstTime = false;
                } else {
                    changeLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
                    priceLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    symbolLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    mRecyclerViewAdapter.sortByChangeDescending();
                    clickedFirstTime = true;
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private class StartDatePickerOnDateSet implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            Calendar inputDate = Calendar.getInstance();
            //inputDate.set(year, month, dayOfMonth, 0, 0, 0);
            inputDate.set(year, month, dayOfMonth);

            long startTime = inputDate.getTimeInMillis();
            long endTime = stringToDateInMilis(String.valueOf(endTimeTextView.getText()));

            if (startTime > endTime) {
                startTime = endTime - 86400000L;
            } else if (startTime < 1498867200000L) {
                startTime = 1498867200000L; // Sat Jul 01 2017 09:30:00 GMT+0930 (Binance server's earliest time by default)
            }

            //startTimeTextView.setText(dateInMilisToString(startTime));
            loadChartData(startTime, endTime);
            spinnerTimePicker.setSelection(adapterTimePicker.getPosition("custom"));
        }
    }

    private class EndDatePickerOnDateSet implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            Calendar inputDate = Calendar.getInstance();
            //inputDate.set(year, month, dayOfMonth, 0, 0, 0);
            inputDate.set(year, month, dayOfMonth);

            long endTime = inputDate.getTimeInMillis();
            long startTime = stringToDateInMilis(String.valueOf(startTimeTextView.getText()));

            if (endTime < startTime || endTime > System.currentTimeMillis() + 3000L) {
                endTime = System.currentTimeMillis();
            }

            //endTimeTextView.setText(dateInMilisToString(endTime));
            loadChartData(startTime, endTime);
            spinnerTimePicker.setSelection(adapterTimePicker.getPosition("custom"));
        }
    }

    private class DatePickerOnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            DatePickerFragment newFragment = new DatePickerFragment();

            if (getFragmentManager() != null) {
                if (v == startTimeTextView) {
                    newFragment.setCallBack(new StartDatePickerOnDateSet());
                    newFragment.setCalendar(dateStringToCalendar(startTimeTextView.getText().toString()));
                    newFragment.show(getFragmentManager(), "start_time");
                }
                if (v == endTimeTextView) {
                    newFragment.setCallBack(new EndDatePickerOnDateSet());
                    newFragment.setCalendar(dateStringToCalendar(endTimeTextView.getText().toString()));
                    newFragment.show(getFragmentManager(), "end_time");
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    // OnItemSelectedListener for the lower Base currency spinner that triggers the recycler view for coin price change in 24h
    private class OnItemSelectedListenerForBaseCoin2 implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            // clear the sorting arrows that display on the left side of these labels if any
            changeLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            priceLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            symbolLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            Bundle bundle = new Bundle();
            bundle.putString("base_coin", spinnerBaseCoin2.getSelectedItem().toString());

            loadTickers();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class OnItemSelectedListenerForBaseCoin1 implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            String selectedBaseCoin = parent.getItemAtPosition(position).toString();
            String selectedPricedCoin = "";
            switch (selectedBaseCoin) {
                case "USDT":

                    //spinnerUsdt.setSelection(adapterSpinnerUsdt.getPosition(selectedPricedCoin));
                    Timber.tag("trienxxx2").d(String.valueOf(adapterSpinnerUsdt.getPosition(selectedPricedCoin)));
                    spinnerUsdt.setVisibility(View.VISIBLE);
                    spinnerBnb.setVisibility(View.INVISIBLE);
                    spinnerEth.setVisibility(View.INVISIBLE);
                    spinnerBtc.setVisibility(View.INVISIBLE);
                    selectedPricedCoin = spinnerUsdt.getSelectedItem().toString();
                    break;

                case "BNB":
                    //spinnerBnb.setSelection(adapterSpinnerUsdt.getPosition(selectedPricedCoin));
                    Timber.tag("trienxxx2").d(String.valueOf(adapterSpinnerUsdt.getPosition(selectedPricedCoin)));
                    spinnerUsdt.setVisibility(View.INVISIBLE);
                    spinnerBnb.setVisibility(View.VISIBLE);
                    spinnerEth.setVisibility(View.INVISIBLE);
                    spinnerBtc.setVisibility(View.INVISIBLE);
                    selectedPricedCoin = spinnerBnb.getSelectedItem().toString();
                    break;
                case "BTC":
                    // spinnerBtc.setSelection(adapterSpinnerUsdt.getPosition(selectedPricedCoin));
                    Timber.tag("trienxxx2").d(String.valueOf(adapterSpinnerUsdt.getPosition(selectedPricedCoin)));
                    spinnerUsdt.setVisibility(View.INVISIBLE);
                    spinnerBnb.setVisibility(View.INVISIBLE);
                    spinnerEth.setVisibility(View.INVISIBLE);
                    spinnerBtc.setVisibility(View.VISIBLE);
                    selectedPricedCoin = spinnerBtc.getSelectedItem().toString();
                    break;
                case "ETH":
                    //spinnerEth.setSelection(adapterSpinnerUsdt.getPosition(selectedPricedCoin));
                    Timber.tag("trienxxx2").d(String.valueOf(adapterSpinnerUsdt.getPosition(selectedPricedCoin)));
                    spinnerUsdt.setVisibility(View.INVISIBLE);
                    spinnerBnb.setVisibility(View.INVISIBLE);
                    spinnerEth.setVisibility(View.VISIBLE);
                    spinnerBtc.setVisibility(View.INVISIBLE);
                    selectedPricedCoin = spinnerEth.getSelectedItem().toString();
                    break;
            }

            if (!spinnerTimePicker.getSelectedItem().toString().equals("1 day")) {
                spinnerTimePicker.setSelection(adapterTimePicker.getPosition("1 day"));
            } else {

                loadChartValue(getSelectedPricedCoin() + selectedBaseCoin,
                        System.currentTimeMillis() - 86400000L, System.currentTimeMillis()); // 1 day by default
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private String getSelectedPricedCoin() {
        String selectedPricedCoin = "";
        List<Spinner> pricedCoinSpinners = new ArrayList<>(Arrays.asList(spinnerUsdt, spinnerBnb, spinnerEth, spinnerBtc));
        for (Spinner spinner : pricedCoinSpinners) {
            if (spinner.getVisibility() == View.VISIBLE) {
                selectedPricedCoin = spinner.getSelectedItem().toString();
            }
        }
        return selectedPricedCoin;
    }

    private class OnItemSelectedListenerForPricedCoin implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            String coinPair = String.valueOf(parent.getItemAtPosition(position)) + spinnerBaseCoin1.getSelectedItem();

            loadChartValue(coinPair, System.currentTimeMillis() - 86400000L, System.currentTimeMillis()); // chart value in 1 day by default
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public void loadChartValue(final String coinPair,
                               final long startTime,
                               final long endTime) {

        Uri.Builder uriBuilder = createChartRequestUri(coinPair, startTime, endTime);

        // Log error here since request failed
        StringRequest stringRequest = new StringRequest(Request.Method.GET, uriBuilder.toString(),
                response -> {

                    if (response == null) {
                        Timber.tag("trienNull").d(" response is null");
                        chart.invalidate();
                        chart.clear();
                        priceTextView.setText("");
                        changeTextView.setText("");
                        volumeTextView.setText(mContext.getString(R.string.vol_label));

                    } else {

                        Timber.d(response);

                        ChartValue chartValue = extractChartsFromReponse(response);

                        initiateChart(chartValue);

                        String baseCoin = spinnerBaseCoin1.getSelectedItem().toString();
                        switch (baseCoin) {
                            case "USDT":
                                priceTextView.setText(String.format(Locale.getDefault(), "%.2f USDT", chartValue.getEndPrice()));
                                break;
                            case "BNB":
                                priceTextView.setText(String.format(Locale.getDefault(), "%.2f BNB", chartValue.getEndPrice()));
                                break;
                            case "ETH":
                                priceTextView.setText(String.format(Locale.getDefault(), "%.2f ETH", chartValue.getEndPrice()));
                                break;
                            case "BTC":
                                priceTextView.setText(String.format(Locale.getDefault(), "%.2f BTC", chartValue.getEndPrice()));
                                break;
                        }

                        changeTextView.setText(String.format(Locale.getDefault(), "%.2f%%", chartValue.getChange()));
                        changeTextView.setTextColor(chartValue.getChange() >= 0 ? mContext.getResources().getColor(R.color.colorTextGreen) : mContext.getResources().getColor(android.R.color.holo_red_light));
                        volumeTextView.setText(String.format(Locale.getDefault(), "Vol: %.2f", chartValue.getVolume()));
                        startTimeTextView.setText(dateInMilisToString(chartValue.getStartTime()));
                        endTimeTextView.setText(dateInMilisToString(chartValue.getEndTime()));
                    }
                }, Timber::e);
        stringRequest.setTag(VOLLEY_REQUEST_TAG);
        requestQueue.add(stringRequest);
    }

    private Uri.Builder createChartRequestUri(String coinPair, long startTime, long endTime) {
        Uri baseUri = Uri.parse(BASE_URL + TAG_CHART);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        String interval = computeChartInterval(startTime, endTime);
        uriBuilder.appendQueryParameter(PARAM_SYMBOL, coinPair);
        uriBuilder.appendQueryParameter(PARAM_INTERVAL, interval);
        uriBuilder.appendQueryParameter(PARAM_START_TIME, String.valueOf(startTime));
        uriBuilder.appendQueryParameter(PARAM_END_TIME, String.valueOf(endTime));
        return uriBuilder;
    }

    private void loadChartData(long startTime, long endTime) {

        if (startTime < endTime) {
            loadChartValue(getSelectedPricedCoin() + spinnerBaseCoin1.getSelectedItem(), startTime, endTime);
        }
    }

    public void loadTickers () {

        loadingIndicator.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, BASE_URL + TAG_TICKER_PRICE_24H,
                response -> {

                    if (response == null) {

                        loadingIndicator.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        emptyTextView.setText(R.string.no_data_found);

                    } else {

                        List<PriceTicker> priceTickers = extractPriceTickerFromBinance(response);

                        loadingIndicator.setVisibility(View.GONE);
                        emptyTextView.setText("");
                        mRecyclerView.setVisibility(View.VISIBLE);


                        String selectedBaseCoin = String.valueOf(spinnerBaseCoin2.getSelectedItem());
                        mRecyclerViewAdapter.updateAdapterData(priceTickers);
                        mRecyclerViewAdapter.setBaseCoin(selectedBaseCoin);

                        if (true) {
                            // Create an ArrayAdapter using the string array and a default spinner layout
                            List<CharSequence> symbolListByUsdt = new ArrayList<>();
                            List<CharSequence> symbolListByBnb = new ArrayList<>();
                            List<CharSequence> symbolListByBtc = new ArrayList<>();
                            List<CharSequence> symbolListByEth = new ArrayList<>();

                            for (PriceTicker p : mRecyclerViewAdapter.getPriceTickersByBaseCoin("USDT")) {
                                symbolListByUsdt.add(p.getCoinPair().replace("USDT", ""));
                            }
                            for (PriceTicker p : mRecyclerViewAdapter.getPriceTickersByBaseCoin("BNB")) {
                                symbolListByBnb.add(p.getCoinPair().replace("BNB", ""));
                            }
                            for (PriceTicker p : mRecyclerViewAdapter.getPriceTickersByBaseCoin("BTC")) {
                                symbolListByBtc.add(p.getCoinPair().replace("BTC", ""));
                            }
                            for (PriceTicker p : mRecyclerViewAdapter.getPriceTickersByBaseCoin("ETH")) {
                                symbolListByEth.add(p.getCoinPair().replace("ETH", ""));
                            }

                            adapterSpinnerUsdt = new ArrayAdapter<>(mContext,
                                    android.R.layout.simple_spinner_item, symbolListByUsdt);
                            ArrayAdapter<CharSequence> adapterSpinnerBnb = new ArrayAdapter<>(mContext,
                                    android.R.layout.simple_spinner_item, symbolListByBnb);
                            ArrayAdapter<CharSequence> adapterSpinnerBtc = new ArrayAdapter<>(mContext,
                                    android.R.layout.simple_spinner_item, symbolListByBtc);
                            ArrayAdapter<CharSequence> adapterSpinnerEth = new ArrayAdapter<>(mContext,
                                    android.R.layout.simple_spinner_item, symbolListByEth);

                            // Specify the layout to use when the list of choices appears
                            adapterSpinnerUsdt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            //spinnerUsdt.setSelection(adapterSpinnerUsdt.getPosition("BTC"));
                            spinnerUsdt.setOnItemSelectedListener(new OnItemSelectedListenerForPricedCoin());
                            // Apply the adapter to the spinner
                            spinnerUsdt.setAdapter(adapterSpinnerUsdt);

                            // Specify the layout to use when the list of choices appears
                            adapterSpinnerBnb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerBnb.setOnItemSelectedListener(new OnItemSelectedListenerForPricedCoin());
                            // Apply the adapter to the spinner
                            spinnerBnb.setAdapter(adapterSpinnerBnb);

                            // Specify the layout to use when the list of choices appears
                            adapterSpinnerBtc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerBtc.setOnItemSelectedListener(new OnItemSelectedListenerForPricedCoin());
                            // Apply the adapter to the spinner
                            spinnerBtc.setAdapter(adapterSpinnerBtc);

                            // Specify the layout to use when the list of choices appears
                            adapterSpinnerEth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerEth.setOnItemSelectedListener(new OnItemSelectedListenerForPricedCoin());
                            // Apply the adapter to the spinner
                            spinnerEth.setAdapter(adapterSpinnerEth);

                            spinnerBaseCoin1.setOnItemSelectedListener(new OnItemSelectedListenerForBaseCoin1());
                            spinnerTimePicker.setOnItemSelectedListener(new OnItemSelectedListenerForTimeSpinner());

                            isFirstTimePopulated = false;
                        }
                    }
                }, Timber::e);
        stringRequest.setTag(VOLLEY_REQUEST_TAG);
        requestQueue.add(stringRequest);
    }

    private class OnItemSelectedListenerForTimeSpinner implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            String selectedTimeFrame = parent.getItemAtPosition(position).toString();
            long startTime = 0;
            long endTime = 0;

            switch (selectedTimeFrame) {
                case "1 day":
                    startTime = (System.currentTimeMillis() - 86400000L);
                    endTime = (System.currentTimeMillis());
                    loadChartData(startTime, endTime);
                    break;
                case "7 days":
                    startTime = (System.currentTimeMillis() - 604800000L);
                    endTime = (System.currentTimeMillis());
                    loadChartData(startTime, endTime);
                    break;
                case "30 days":
                    startTime = (System.currentTimeMillis() - 2592000000L);
                    endTime = (System.currentTimeMillis());
                    loadChartData(startTime, endTime);
                    break;
                case "90 days":
                    startTime = (System.currentTimeMillis() - 7776000000L);
                    endTime = (System.currentTimeMillis());
                    loadChartData(startTime, endTime);
                    break;
                case "1 year":
                    startTime = (System.currentTimeMillis() - 31536000000L);
                    if (startTime < stringToDateInMilis("01/07/2017"))
                        startTime = 1498867200000L; // Sat Jul 01 2017 09:30:00 GMT+0930
                    endTime = (System.currentTimeMillis());
                    loadChartData(startTime, endTime);
                    break;
                case "all time":
                    startTime = 1498867200000L; // Sat Jul 01 2017 09:30:00 GMT+0930
                    endTime = System.currentTimeMillis();
                    loadChartData(startTime, endTime);
                    break;
                case "custom":
                    break;
            }

            /*startTimeTextView.setText(dateInMilisToString(startTime));
            endTimeTextView.setText(dateInMilisToString(endTime));*/
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }


    public void initiateChart(ChartValue chartValue) {
/*
        String pricedCoin = "BTC";
        String baseCoin = "USDT";

        double price;
        long time;*/

        List<Entry> entries = new ArrayList<>();

        for (ChartInterval data : chartValue.getIntervals()) {

            // turn your data into Entry objects
            entries.add(new Entry(data.getTime(), (float) data.getPrice()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
        dataSet.setColor(mContext.getResources().getColor(R.color.colorGraphLine));
        dataSet.setLineWidth(2);
        /*dataSet.setHighLightColor(R.color.colorTextGreen);
        dataSet.setValueTextColor(R.color.colorTextGreen);*/
       /* Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.fade_red);
        dataSet.setFillDrawable(drawable);*/
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);
        dataSet.setLabel("");
        dataSet.setFillColor(mContext.getResources().getColor(R.color.colorGraphFiller));

        dataSet.setDrawCircles(false);

        // styling, ...

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        // no description text
        chart.getDescription().setEnabled(false);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.getAxisRight().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getXAxis().setEnabled(false);
        chart.setNoDataText(mContext.getString(R.string.chart_no_data_text));
      /*  chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);*/
       /* chart.setClipToPadding(false);
        chart.setPadding(0,0,0,0);*/
        chart.getLegend().setEnabled(false);
        //chart.setExtraOffsets(0,0,0,0);
        chart.setViewPortOffsets(100, 0, 0, 0);
        chart.fitScreen();

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
       /* MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        mv.setChartView(mChart); // For bounds control
        mChart.setMarker(mv); // Set the marker to the chart*/
        chart.invalidate(); // refresh
    }
}