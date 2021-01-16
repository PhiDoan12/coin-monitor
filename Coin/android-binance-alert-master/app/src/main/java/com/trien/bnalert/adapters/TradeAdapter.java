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
import com.trien.bnalert.models.Trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.trien.bnalert.utils.TextUtils.formatFbLikeDateTime;

public class TradeAdapter extends RecyclerView.Adapter<TradeAdapter.SimpleViewHolder> {

    private List<Trade> tradeList;

    private Context mContext;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    public TradeAdapter(Context context) {
        tradeList = new ArrayList<>();
        mContext = context;
    }

    /*
     * A common adapter modification or reset mechanism. As with ListAdapter,
     * calling notifyDataSetChanged() will trigger the RecyclerView to update
     * the view. However, this method will not trigger any of the RecyclerView
     * animation features.
     */
    public void refreshAdapter(List<Trade> trades) {
        tradeList.clear();
        Collections.sort(trades);
        tradeList.addAll(trades);

        notifyDataSetChanged();
    }

    /*
     * Inserting a new item at the head of the list. This uses a specialized
     * RecyclerView method, notifyItemRemoved(), to trigger any enabled item
     * animations in addition to updating the view.
     */
    public void removeItem(int position) {
        if (position >= tradeList.size()) return;

        tradeList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = inflater.inflate(R.layout.trade_row, container, false);

        SimpleViewHolder viewHolder = new SimpleViewHolder(root, this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder itemHolder, int position) {

        double qty = tradeList.get(position).getQuantity();

        if (qty == (int)qty) {
            itemHolder.qty.setText(String.valueOf((int)qty));
        }
        else {
            itemHolder.qty.setText(String.valueOf(qty));
        }

        itemHolder.price.setText(String.format(Locale.getDefault(), "%.8f", tradeList.get(position).getPrice()));
        
        itemHolder.time.setText(formatFbLikeDateTime(tradeList.get(position).getTime()));
    }

    @Override
    public int getItemCount() {
        return tradeList.size();
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

        private TradeAdapter mAdapter;

        TextView price;
        TextView qty;
        TextView time;

        public SimpleViewHolder(View itemView, TradeAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);

            mAdapter = adapter;
            price = itemView.findViewById(R.id.tv_price);
            qty = itemView.findViewById(R.id.tv_qty);
            time = itemView.findViewById(R.id.tv_time);
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }
}
