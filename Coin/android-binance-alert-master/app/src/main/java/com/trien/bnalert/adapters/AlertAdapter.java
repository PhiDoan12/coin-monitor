package com.trien.bnalert.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.trien.bnalert.R;
import com.trien.bnalert.models.Alert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import timber.log.Timber;


public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.SimpleViewHolder> {

    private List<Alert> alertList = new ArrayList<>();

    private OnCustomClickListener onCustomClickListener;

    private Context context;

    public AlertAdapter(Context context,
                        OnCustomClickListener onCustomClickListener) {
        this.context = context;
        this.onCustomClickListener = onCustomClickListener;
    }

    public interface OnCustomClickListener {

        void onItemClick(AdapterView<?> parent, View view, int position, long id);

        void onAlertBtnClick(int position);

        void onDeleteBtnClick(int position);
    }

    @NonNull
    @Override
    public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.alert_row, container, false);

        SimpleViewHolder viewHolder = new SimpleViewHolder(root, this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder itemHolder, int position) {
        Alert alert = alertList.get(position);

        itemHolder.coin.setText(alert.getCoin());

        String format;
        switch (alert.getBaseCoin()) {
            case "USDT":
                format = "%s %.2f %s";
                break;
            case "BNB":
                format = "%s %.5f %s";
                break;
            default:
                format = "%s %.8f %s";
                break;
        }

        itemHolder.condition.setText(String.format(Locale.getDefault(), format, alert.getComparator(), alert.getComparedToValue(), alert.getBaseCoin()));
        if (alert.isActive()) {
            itemHolder.alertButton.setImageResource(R.drawable.ic_notifications_active);
        } else {
            itemHolder.alertButton.setImageResource(R.drawable.ic_notifications_off);
        }
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    public void setAlerts(List<Alert> alerts) {
        alertList.clear();
        Collections.sort(alerts, Alert.dateAddedAscComparator);
        alertList.addAll(alerts);

        Timber.v(String.valueOf(alertList.size()));
        for (int i = 0; i < alertList.size(); i++
             ) {
            Timber.v(alertList.get(i).toString());
        }

        notifyDataSetChanged();
    }

    private void onItemHolderClick(SimpleViewHolder itemHolder) {
        if (onCustomClickListener != null) {
            onCustomClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    private void onAlertButtonClick(SimpleViewHolder itemHolder) {
        if (onCustomClickListener != null) {
            onCustomClickListener.onAlertBtnClick(itemHolder.getAdapterPosition());
        }
    }

    private void onDeleteButtonClick(SimpleViewHolder itemHolder) {
        if (onCustomClickListener != null) {
            onCustomClickListener.onDeleteBtnClick(itemHolder.getAdapterPosition());
        }
    }

    static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private AlertAdapter mAdapter;

        TextView coin;
        TextView condition;
        ImageView alertButton;
        ImageView deleteButton;

        public SimpleViewHolder(View itemView, AlertAdapter adapter) {
            super(itemView);

            mAdapter = adapter;
            coin = itemView.findViewById(R.id.coin_tv);
            condition = itemView.findViewById(R.id.condition_tv);
            alertButton = itemView.findViewById(R.id.ic_alert);
            deleteButton = itemView.findViewById(R.id.ic_delete);

            itemView.setOnClickListener(this);
            alertButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == itemView.getId()) {
                mAdapter.onItemHolderClick(this);
            }
            else if (v.getId() == alertButton.getId()) {
                mAdapter.onAlertButtonClick(this);
            }
            else if (v.getId() == deleteButton.getId()) {
                mAdapter.onDeleteButtonClick(this);
            }
        }
    }
}
