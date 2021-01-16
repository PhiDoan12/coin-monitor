package com.trien.bnalert;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentTransaction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.trien.bnalert.fragment.AlertFragment;
import com.trien.bnalert.fragment.PriceTickerFragment;
import com.trien.bnalert.fragment.TradeFragment;
import com.trien.bnalert.utils.Constant;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.my_toolbar) Toolbar myToolbar;

    @BindView(R.id.navigation) BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {

                case R.id.nav_alert:
                    FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.container, new AlertFragment());
                    ft2.commit();
                    myToolbar.setVisibility(View.VISIBLE);
                    return true;

                case R.id.nav_prices:
                    FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.container, new PriceTickerFragment());
                    ft4.commit();
                    myToolbar.setVisibility(View.GONE);
                    return true;

                case R.id.nav_recent_trade:
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.container, new TradeFragment());
                    ft.commit();
                    myToolbar.setVisibility(View.GONE);
                    return true;
            }

            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // enable ButterKnife
        ButterKnife.bind(this);

        /*Once you enable the receiver this way, it will stay enabled, even if the user reboots the device.
        In other words, programmatically enabling the receiver overrides the manifest setting, even across
        reboots. The receiver will stay enabled until your app disables it. */
        ComponentName receiver = new ComponentName(this, AlarmManagerBroadcastReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        //changeStatusBarColor();

        setSupportActionBar(myToolbar);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, new AlertFragment());
        ft.commit();

        long[] pattern = {0, 500, 200, 500, 200, 500, 200, 500};
        Constant.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getApplicationContext().getPackageName() + "/" + R.raw.lovingyou);
        NotificationChannel mChannel = new NotificationChannel(Constant.CHANNEL_ID, Constant.CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mChannel.setSound(soundUri, audioAttributes);
        mChannel.setVibrationPattern(pattern);
        mChannel.setLightColor(Color.YELLOW);
        mChannel.enableLights(true);
        mChannel.enableVibration(true);

        Constant.notificationManager.createNotificationChannel(mChannel);

    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
