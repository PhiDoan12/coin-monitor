package com.trien.bnalert.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.trien.bnalert.R;
import com.trien.bnalert.models.PriceTicker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PriceTickerAdapter extends RecyclerView.Adapter<PriceTickerAdapter.SimpleViewHolder> {

    private List<PriceTicker> mPriceTickersFiltered;
    private List<PriceTicker> mPriceTickersUSDT;
    private List<PriceTicker> mPriceTickersETH;
    private List<PriceTicker> mPriceTickersBTC;
    private List<PriceTicker> mPriceTickersBNB;

    private String mBaseCoin;

    private Context mContext;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    public PriceTickerAdapter(Context context) {
       // mPriceTickers = new ArrayList<>();
        mPriceTickersFiltered = new ArrayList<>();
        mPriceTickersUSDT = new ArrayList<>();
        mPriceTickersETH = new ArrayList<>();
        mPriceTickersBTC = new ArrayList<>();
        mPriceTickersBNB = new ArrayList<>();
        mContext = context;
    }

    /*
     * A common adapter modification or reset mechanism. As with ListAdapter,
     * calling notifyDataSetChanged() will trigger the RecyclerView to update
     * the view. However, this method will not trigger any of the RecyclerView
     * animation features.
     */
    public void updateAdapterData(List<PriceTicker> tickers) {

        Collections.sort(tickers, PriceTicker.symbolAscComparator);

        for (PriceTicker ticker:tickers) {
            if (ticker.getCoinPair().endsWith("USDT"))
                mPriceTickersUSDT.add(ticker);
            else if (ticker.getCoinPair().endsWith("BNB"))
                mPriceTickersBNB.add(ticker);
            else if (ticker.getCoinPair().endsWith("ETH"))
                mPriceTickersETH.add(ticker);
            else if (ticker.getCoinPair().endsWith("BTC"))
                mPriceTickersBTC.add(ticker);
        }
    }

    @NonNull
    @Override
    public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = inflater.inflate(R.layout.price_ticker_row, container, false);

        SimpleViewHolder viewHolder = new SimpleViewHolder(root, this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder itemHolder, int position) {

        itemHolder.id.setText(String.valueOf(position + 1));

        String coinPair = String.valueOf(mPriceTickersFiltered.get(position).getCoinPair());
        String symbol = coinPair.replace(mBaseCoin, "");
        itemHolder.symbol.setText(symbol);

        String format;
        if (mBaseCoin.equals("USDT")) {
            format = "%.2f";
        } else if (mBaseCoin.equals("BNB")) {
            format = "%.5f";
        }
        else {
            format = "%.8f";
        }

        itemHolder.price.setText(String.format(Locale.getDefault(), format, mPriceTickersFiltered.get(position).getPrice()));

        double change = mPriceTickersFiltered.get(position).getChange();
        itemHolder.change.setText(String.format(Locale.getDefault(), "%.2f%%", change));
        itemHolder.change.setTextColor(change >=0 ? mContext.getResources().getColor(R.color.colorTextGreen) : mContext.getResources().getColor(android.R.color.holo_red_light));
    }

    @Override
    public int getItemCount() {
        return mPriceTickersFiltered.size();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(SimpleViewHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private PriceTickerAdapter mAdapter;

        TextView id;
        TextView symbol;
        TextView price;
        TextView change;

        public SimpleViewHolder(View itemView, PriceTickerAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            mAdapter = adapter;
            id = itemView.findViewById(R.id.id);
            symbol = itemView.findViewById(R.id.symbol);
            price = itemView.findViewById(R.id.tv_price);
            change = itemView.findViewById(R.id.tv_percent_change);
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }

    public String getBaseCoin() {
        return mBaseCoin;
    }

    public void setBaseCoin(String baseCoin) {
        this.mBaseCoin = baseCoin;

        switch (baseCoin) {
            case "USDT":
                mPriceTickersFiltered = mPriceTickersUSDT;
                break;
            case "BNB":
                mPriceTickersFiltered = mPriceTickersBNB;
                break;
            case "ETH":
                mPriceTickersFiltered = mPriceTickersETH;
                break;
            case "BTC":
                mPriceTickersFiltered = mPriceTickersBTC;
                break;
        }

        notifyDataSetChanged();
    }

    public List<PriceTicker> getPriceTickersByBaseCoin(String baseCoin) {
        List<PriceTicker> priceTickersByBaseCoin = new ArrayList<>();
        if (baseCoin.equals("BNB"))
            priceTickersByBaseCoin = mPriceTickersBNB;
        else if (baseCoin.equals("BTC"))
            priceTickersByBaseCoin = mPriceTickersBTC;
        else if (baseCoin.equals("ETH"))
            priceTickersByBaseCoin = mPriceTickersETH;
        else if (baseCoin.equals("USDT"))
            priceTickersByBaseCoin = mPriceTickersUSDT;

        return priceTickersByBaseCoin;
    }

    public void sortByPriceAscending() {
        Collections.sort(mPriceTickersFiltered, PriceTicker.priceAscComparator);
        notifyDataSetChanged();
    }

    public void sortByPriceDescending() {
        Collections.sort(mPriceTickersFiltered, PriceTicker.priceDescComparator);
        notifyDataSetChanged();
    }

    public void sortBySymbolAscending() {
        Collections.sort(mPriceTickersFiltered, PriceTicker.symbolAscComparator);
        notifyDataSetChanged();
    }

    public void sortBySymbolDescending() {
        Collections.sort(mPriceTickersFiltered, PriceTicker.symbolDescComparator);
        notifyDataSetChanged();
    }

    public void sortByChangeAscending() {
        Collections.sort(mPriceTickersFiltered, PriceTicker.priceChangeAscComparator);
        notifyDataSetChanged();
    }

    public void sortByChangeDescending() {
        Collections.sort(mPriceTickersFiltered, PriceTicker.priceChangeDescComparator);
        notifyDataSetChanged();
    }
}
