package com.trien.bnalert.fragment;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.trien.bnalert.AlarmManagerBroadcastReceiver;
import com.trien.bnalert.BuildConfig;
import com.trien.bnalert.EditAlertActivity;
import com.trien.bnalert.NewAlertActivity;
import com.trien.bnalert.R;
import com.trien.bnalert.adapters.AlertAdapter;
import com.trien.bnalert.persistence.AlertViewModel;
import com.trien.bnalert.models.Alert;
import com.trien.bnalert.persistence.Injection;
import com.trien.bnalert.persistence.ViewModelFactory;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.trien.bnalert.EditAlertActivity.EXTRA_EDIT_ALERT_SEND_BACK;

public class AlertFragment extends Fragment implements AlertAdapter.OnCustomClickListener {

    private static final String TAG = AlertFragment.class.getSimpleName();

    public static final int NEW_ALERT_ACTIVITY_REQUEST_CODE = 1;
    public static final int EDIT_ALERT_ACTIVITY_REQUEST_CODE = 2;

    private ViewModelFactory mViewModelFactory;

    private AlertViewModel mViewModel;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    AlarmManager alarmManager;

    private AlertAdapter mAlertAdapter;

    private List<Alert> alertList;

    // Declare Context variable at class level in Fragment
    private Context mContext;

    RecyclerView recyclerView;

    FloatingActionButton addButton;

    SharedPreferences prefs;

    // When the fragment is not yet attached, or was detached during the end of its lifecycle,
    // getContext() will return null. To avoid this, Initialise mContext from onAttach().
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_alert,
                container, false);

        prefs = mContext.getSharedPreferences("com.trien.bnalert", MODE_PRIVATE);

        alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        alarmManager.setAndAllowWhileIdle();
        recyclerView = rootView.findViewById(R.id.recycler_alert);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAlertAdapter = new AlertAdapter(mContext, this);
        recyclerView.setAdapter(mAlertAdapter);

        mViewModelFactory = Injection.provideViewModelFactory(mContext);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(AlertViewModel.class);

        addButton = rootView.findViewById(R.id.fab);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, NewAlertActivity.class);
            startActivityForResult(intent, NEW_ALERT_ACTIVITY_REQUEST_CODE);
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mDisposable.add(mViewModel.getAllAlerts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(alerts -> {
                    mAlertAdapter.setAlerts(alerts); alertList = alerts;
                            reScheduleAllAlertsOnceUpgradeApp(alerts);
                            enableBroadcastReceiverAfterBoot(mContext, alerts.isEmpty());
                    },
                        throwable -> Timber.tag(TAG).e("Unable to update username" + throwable)));
    }

    @Override
    public void onStop() {
        super.onStop();

        // clear all the subscriptions
        mDisposable.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        // read all alerts from the database asynchronously
       // new ReadFromDb(mContext).execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_ALERT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            Alert alert = data.getParcelableExtra(NewAlertActivity.EXTRA_NEW_ALERT);

            mDisposable.add(mViewModel.insertOrUpdateAlert(alert)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());

            createNewAlert(mContext, alert);
        }

        if (requestCode == EDIT_ALERT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Alert editedAlert = data.getParcelableExtra(EXTRA_EDIT_ALERT_SEND_BACK);

            mDisposable.add(mViewModel.insertOrUpdateAlert(editedAlert)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe());

            // cancel the old alert and replace it with a new one by creating a new alert with the same requestCode (alertId)
            createNewAlert(mContext, editedAlert);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(mContext, EditAlertActivity.class);
        intent.putExtra(EditAlertActivity.EXTRA_EDIT_ALERT, alertList.get(position));
        Timber.tag("trienzzzz2").d(String.valueOf(alertList.get(position)));
        startActivityForResult(intent, EDIT_ALERT_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onAlertBtnClick(int position) {

        Alert alert = alertList.get(position);
        Timber.tag("trienzzzz").d(alert.toString());

        if (alert.isActive()) {
            // set "active" property to false and update database
            alert.setActive(false);
            // cancel this alert
            cancelAlert(alert.getId());
        } else {
            // set "active" property to true and update database
            alert.setActive(true);
            // create a new alert
            createNewAlert(mContext, alert);
        }

        mDisposable.add(mViewModel.insertOrUpdateAlert(alert)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    @Override
    public void onDeleteBtnClick(final int position) {
        final Alert alert = alertList.get(position);
        final ContentResolver resolver = mContext.getContentResolver();

        createDialog(alert, resolver);
    }

    private void createDialog(final Alert alert, final ContentResolver resolver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Delete an alert")
                .setMessage("Delete this alert " + alert.getCoin() + " " + alert.getComparator() + " " + alert.getComparedToValue() + alert.getBaseCoin())
                .setPositiveButton("OK", (dialog, which) -> {

                    mDisposable.add(mViewModel.deleteAlert(alert)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe());

                    // cancel this alert
                    cancelAlert(alert.getId());
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                });

        builder.create().show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createNewAlert(Context context, Alert alert) {

        createNotificationChannel(context, String.valueOf(alert.getId()));

        Bundle bundle = new Bundle();
        bundle.putInt("id", alert.getId());
        bundle.putString("coin", alert.getCoin());
        bundle.putString("comparator", alert.getComparator());
        bundle.putDouble("comparedToValue", alert.getComparedToValue());
        bundle.putString("baseCoin", alert.getBaseCoin());
        bundle.putBoolean("isPersistent", alert.isPersistent());
        bundle.putDouble("interval", alert.getInterval());
        bundle.putBoolean("vibrate", alert.isVibrate());
        bundle.putBoolean("playSound", alert.isPlaySound());
        bundle.putBoolean("flashing", alert.isFlashing());
        bundle.putBoolean("isActive", alert.isActive());
        bundle.putLong("dateAdded", alert.getDateAdded());

        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                alert.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        //Interval value will be forced up to 60000 as of Android 5.1; don't rely on this to be exact.
        //Frequent alarms are bad for battery life. As of API 22, the AlarmManager will override near-future
        // and high-frequency alarm requests, delaying the alarm at least 5 seconds into the future and
        // ensuring that the repeat interval is at least 60 seconds.  If you really need to do work
        // sooner than 5 seconds, post a delayed message or runnable to a Handler.
        // the alarm will be executed immediately for the first time, then periodically as per set intervals
        // Also, if we use ELAPSED_REALTIME instead of ELAPSED_REALTIME_WAKEUP, users may find it annoying
        // because within this app features, they will receive tons of notifications at the same time once
        // the phones wake up.
        alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                (long) alert.getInterval(), pendingIntent);
    }

    private void cancelAlert(int requestCodeOrNotificationId) {
        Intent intent = new Intent(mContext, AlarmManagerBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                requestCodeOrNotificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    private void createNotificationChannel(Context context, String channelId) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Coin prices";
            String description = "From binance.com";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void reScheduleAllAlertsOnceUpgradeApp(List<Alert> alerts) {
        if (!alerts.isEmpty()) {
            // in case users upgrade to newer versions of the app, then recreate all alerts based on records in the database
            if (prefs.getBoolean(String.valueOf(BuildConfig.VERSION_CODE), true)) {
                for (Alert alert : alerts) {
                    // Do first run stuff here then set 'firstRun' as false
                    Timber.v(alert.toString());
                    createNewAlert(mContext, alert);
                }
                prefs.edit().putBoolean(String.valueOf(BuildConfig.VERSION_CODE), false).apply();
            }
        }
    }

    // if there are alerts in the list then we make the phones start an alert when the device restarts, and vice versa
    private void enableBroadcastReceiverAfterBoot(Context context, Boolean isAlertLisEmpty) {
        ComponentName receiver = new ComponentName(context, AlarmManagerBroadcastReceiver.class);
        PackageManager pm = context.getPackageManager();

        if (isAlertLisEmpty) {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
}