package com.coin;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    EditText text;
    Button button;
    Button price;
    Button loadData;
    Button loadAstrology;
    Thread threadLoop;
    NotificationChannel mChannel;
    Uri soundUri;
    AudioAttributes audioAttributes;
    Boolean onPause = false;
    NotificationManager notificationManager = null;
    private static final String CHANNEL_ID = "coin.price.notify";
    MonitorPrice monitorPrice = new MonitorPrice();
    long[] pattern = {0, 500, 200, 500, 200, 500, 200, 500};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        price = (Button) findViewById(R.id.button2);
        loadData = (Button) findViewById(R.id.button3);
        loadAstrology = (Button) findViewById(R.id.button8);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + getApplicationContext().getPackageName() + "/" + R.raw.lovingyou);
        mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mChannel.setSound(soundUri, audioAttributes);
        mChannel.setVibrationPattern(pattern);
        mChannel.setLightColor(Color.YELLOW);
        mChannel.enableLights(true);
        mChannel.enableVibration(true);

        notificationManager.createNotificationChannel(mChannel);

        button.setOnClickListener(v -> {
            Constant.increaseSaveCount();
            if (Constant.getSaveCount() >= 2) {
                String textsss = text.getText().toString();
                if (!textsss.contains(Constant.KEY_PERCENT)) {
                    return;
                }
                monitorPrice.saveData(getApplicationContext(), textsss);
                Constant.resetSaveCount();
                getPrice();
            }
        });

        price.setOnClickListener(v -> {
            //onPause = false;
            setOnPause(false);
            getPrice();
        });

        loadAstrology.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        });

        loadData.setOnClickListener(v -> {
            try {
                Constant.priceBoughtCoin.put(Constant.KEY_LOOP, Constant.timeDefaultFromUser);
                //onPause = false;
                setOnPause(false);
                button.setText("Save");
                text.setText(Html.fromHtml(monitorPrice.loadDataToText(getApplicationContext())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        getPrice();
        Toast.makeText(getApplicationContext(), "Loading!",
                Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        System.out.println("onResume");
        super.onResume();
        //onPause = false;
        setOnPause(false);
        getPrice();
    }

    @Override
    protected void onPause() {
        System.out.println("OnPause");
        super.onPause();
        //onPause = true;
        setOnPause(true);
        Constant.priceBoughtCoin.put(Constant.KEY_LOOP, Constant.timeDefaultFromUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");
        //onPause = true;
        setOnPause(true);
        Constant.priceBoughtCoin.put(Constant.KEY_LOOP, Constant.timeDefaultFromUser);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private synchronized void getPrice() {

        if (threadLoop != null && threadLoop.isAlive()) {
            new Thread(() -> {
                threadLoop.interrupt();
                Constant.priceBoughtCoin.put(Constant.KEY_LOOP, new BigDecimal(Constant.REAL_TIME));
            }).start();
            return;
        }

        threadLoop = new Thread(() -> {
            while (true) {
                final StringBuilder builder = new StringBuilder();
                try {
                    HashMap<String, String> hd = monitorPrice.getPrice(getApplicationContext());
                    if (hd == null) {
                        return;
                    }
                    builder.append(hd.get("DA"));
                    if (hd.containsKey("NO")) {
                        if (getOnPause() == true) {
                            notifyABC(hd.get("percent"), false);
                        }
                    }

                    runOnUiThread(() -> {
                        text.setText("");
                        text.setText(Html.fromHtml(builder.toString()));
                        button.setText("Running");
                    });

                    if (monitorPrice.getTime() == null) {
                        Thread.sleep(3 * 60000);
                        return;
                    }

                    Long time = 0L;
                    time = monitorPrice.getTime().multiply(new BigDecimal("60000")).longValue();

                    Thread.sleep(time);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        threadLoop.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyABC(String price, Boolean disable) {
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("MonitorPrice")
                .setContentText("Coin increase: " + price)
                .setAutoCancel(true)
                .setVibrate(pattern)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_HIGH)
                .setChannelId(CHANNEL_ID)
                .setSound(soundUri, audioAttributes)
                .build();

        if (disable == true) {
            mChannel.setVibrationPattern(new long[]{0L});
        } else {
            mChannel.setVibrationPattern(pattern);
        }

        notificationManager.notify(0, notification);
    }

    private synchronized boolean getOnPause() {
        return this.onPause;
    }

    private synchronized void setOnPause(boolean onPause) {
        this.onPause = onPause;
    }
}
