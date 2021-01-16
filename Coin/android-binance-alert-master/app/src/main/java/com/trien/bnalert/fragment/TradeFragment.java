package com.trien.bnalert.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.trien.bnalert.R;
import com.trien.bnalert.adapters.TradeAdapter;
import com.trien.bnalert.models.Trade;

import java.util.List;

import timber.log.Timber;

import static com.trien.bnalert.utils.QueryUtils.BASE_URL;
import static com.trien.bnalert.utils.QueryUtils.PARAM_LIMIT;
import static com.trien.bnalert.utils.QueryUtils.PARAM_SYMBOL;
import static com.trien.bnalert.utils.QueryUtils.RESULT_LIMIT;
import static com.trien.bnalert.utils.QueryUtils.TAG_TRADE;
import static com.trien.bnalert.utils.QueryUtils.extractRecentTradeFromJson;
import static com.trien.bnalert.utils.QueryUtils.isInternetConnected;
import static java.lang.Boolean.FALSE;

public class TradeFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    public static final String VOLLEY_REQUEST_TAG = "trade_request";
    private RecyclerView mRecyclerView;
    private TradeAdapter mAdapter;
    private Spinner spinner;
    private View loadingIndicator;
    private TextView emptyTextView;

    private SwipeRefreshLayout swipeContainer;

    // Declare Context variable at class level in Fragment
    private Context mContext;

    RequestQueue requestQueue;

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
        View rootView = inflater.inflate(R.layout.fragment_trade,
                container, false);

        requestQueue = Volley.newRequestQueue(mContext);

        loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        emptyTextView  = rootView.findViewById(R.id.tv_emptyTv);

        spinner = rootView.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                R.array.coin_pair_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                loadTrades(String.valueOf(spinner.getSelectedItem()));
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        // set up adapter
        mAdapter = new TradeAdapter(mContext);
        mAdapter.setOnItemClickListener(this);

        // set up recycler view
        mRecyclerView = rootView.findViewById(R.id.recycler_view_trade);
        mRecyclerView.setHasFixedSize(false);

        // set up layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, FALSE);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (!isInternetConnected(mContext)) {
            loadingIndicator.setVisibility(View.GONE);
            emptyTextView.setText(R.string.no_internet);
        }
        else {
            loadTrades(String.valueOf(parent.getItemAtPosition(position)));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void loadTrades(final String tradingPair) {

        // run loadingIndicator
        loadingIndicator.setVisibility(View.VISIBLE);

        Uri baseUri = Uri.parse(BASE_URL + TAG_TRADE);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(PARAM_SYMBOL, tradingPair);
        uriBuilder.appendQueryParameter(PARAM_LIMIT, String.valueOf(RESULT_LIMIT));

        StringRequest stringRequest = new StringRequest(Request.Method.GET, uriBuilder.toString(),
                response -> {

                    if (response == null) {

                        loadingIndicator.setVisibility(View.GONE);
                        emptyTextView.setText(R.string.no_data_found);

                    } else {

                        // Extract relevant fields from the JSON response and create a list of {@link Trade}s
                        List<Trade> trades = extractRecentTradeFromJson(response);

                        loadingIndicator.setVisibility(View.GONE);
                        emptyTextView.setText("");
                        mAdapter.refreshAdapter(trades);
                        // Now we call setRefreshing(false) to signal refresh has finished
                        swipeContainer.setRefreshing(false);
                    }
                }, Timber::e);
        stringRequest.setTag(VOLLEY_REQUEST_TAG);
        requestQueue.add(stringRequest);
    }
}