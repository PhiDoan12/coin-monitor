package com.coin;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    EditText text;
    Button button;
    Button price;
    Button loadData;
    Thread threadLoop;
    Notification notification;
    Notification notificationBTC;
    NotificationChannel mChannel;
    NotificationChannel mChannelBTC;
    Uri soundUri;
    AudioAttributes audioAttributes;
    Boolean onPause = false;
    NotificationManager notificationManager = null;
    private static final String CHANNEL_ID = "coin.price.notify";
    private static final String CHANNEL_ID_BTC = "coin.price.notify.btc";
    KeyguardManager manager = null;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        price = (Button) findViewById(R.id.button2);
        loadData = (Button) findViewById(R.id.button3);

        Thread schedulerGetPriceStockUs = new Thread(() -> {
            int times = (5 * 60 * 1000) / (4 * 1000);
            int count = 0;
            try {
                Constant.getPriceUsStock();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    Constant.resetSaveCount();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(4 * 1000);
                    count++;
                    if (count >= times) {
                        Thread scheduler = new Thread(() -> {
                            try {
                                Constant.getPriceUsStock();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        scheduler.start();
                        count = 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        schedulerGetPriceStockUs.setName("GET US STOCK");
        schedulerGetPriceStockUs.start();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.increaseSaveCount();
                if (Constant.getSaveCount() >= 2) {
                    String textsss = text.getText().toString();
                    if (!textsss.contains(Constant.KEY_PERCENT)) {
                        return;
                    }
                    MonitorPrice monitorPrice = new MonitorPrice();
                    monitorPrice.saveData(getApplicationContext(), textsss);
                    Constant.resetSaveCount();
                    getPrice();
                }
            }
        });

        price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onPause = false;
                setOnPause(false);
                getPrice();
            }
        });

        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Constant.priceBoughtCoin.put(Constant.KEY_LOOP, Constant.timeDefaultFromUser);
                    //onPause = false;
                    setOnPause(false);
                    button.setText("Save");
                    MonitorPrice monitorPrice = new MonitorPrice();
                    text.setText(Html.fromHtml(monitorPrice.loadDataToText(getApplicationContext())));
                } catch (Exception e) {

                }
            }
        });

        getPrice();
        Toast.makeText(getApplicationContext(), "Loading!",
                Toast.LENGTH_SHORT).show();
    }

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

    private synchronized void getPrice() {

        if (threadLoop != null && threadLoop.isAlive()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    threadLoop.interrupt();
                    Constant.priceBoughtCoin.put(Constant.KEY_LOOP, new BigDecimal(Constant.REAL_TIME));
                }
            }).start();
            return;
        }

        threadLoop = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                while (true) {
                    final StringBuilder builder = new StringBuilder();
                    try {
                        MonitorPrice monitorPrice = new MonitorPrice();
                        HashMap<String, String> hd = monitorPrice.getPrice(getApplicationContext());
                        if (hd == null) {
                            return;
                        }
                        builder.append(hd.get("DA"));
                        if (hd.containsKey("NO")) {
                            if (getOnPause() == true) {
                                try {
                                    notifyABC(hd.get("percent"), false);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                            } else {

                            }
                        }

                        if (getOnPause() == true && isDeviceSecured() == true) {
                            try {
                                System.out.println("SHOW_PRICE_SCREEN:  " + hd.get("SHOW_PRICE_SCREEN"));
                                notifyPriceBTC(hd.get("SHOW_PRICE_SCREEN"));
                            } catch (InterruptedException e) {
                                //e.printStackTrace();
                            }
                        }

                        if (getOnPause() == true && isDeviceSecured() == true) {
                            try {
                                if (hd.containsKey("ERROR")) {
                                    System.out.println("ERROR:  " + hd.get("ERROR"));
                                    notifyPriceBTC(hd.get("ERROR"));
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                text.setText("");
                                text.setText(Html.fromHtml(builder.toString()));
                                button.setText("Running");
                            }
                        });

                        if (monitorPrice.getTime() == null) {
                            Thread.sleep(3 * 60000);
                            return;
                        }

                        Long time = 0L;
                        if (getOnPause() == false) {
                            time = new BigDecimal(Constant.REAL_TIME).multiply(new BigDecimal("60000")).longValue();
                        } else {
                            time = monitorPrice.getTime().multiply(new BigDecimal("60000")).longValue();
                        }

                        Thread.sleep(time);

                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        });
        threadLoop.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyABC(String price, Boolean disable) throws InterruptedException {
        long[] pattern = {0, 500, 200, 500, 200, 500, 200, 500};
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (notification != null) {
            notification = new Notification.Builder(this)
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
        } else {
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
            notification = new Notification.Builder(this)
                    .setContentTitle("MonitorPrice")
                    .setContentText("Coin increase: " + price)
                    .setAutoCancel(true)
                    .setVibrate(pattern)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setChannelId(CHANNEL_ID)
                    .setSound(soundUri, audioAttributes)
                    .build();

            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(mChannel);
            }
        }
        notificationManager.notify(0, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyPriceBTC(String price) throws InterruptedException {
        if (StringUtils.isEmpty(price)) {
            return;
        }
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (notificationBTC != null) {
            notificationBTC = new Notification.Builder(this)
                    .setContentTitle("Price")
                    .setContentText(price)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setChannelId(CHANNEL_ID_BTC)
                    .build();
        } else {
            mChannelBTC = new NotificationChannel(CHANNEL_ID_BTC, CHANNEL_ID_BTC, NotificationManager.IMPORTANCE_HIGH);
            mChannelBTC.enableLights(false);
            mChannelBTC.enableVibration(false);
            notificationBTC = new Notification.Builder(this)
                    .setContentTitle("Price")
                    .setContentText(price)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setChannelId(CHANNEL_ID_BTC)
                    .build();

            if (notificationManager.getNotificationChannel(CHANNEL_ID_BTC) == null) {
                notificationManager.createNotificationChannel(mChannelBTC);
            }
        }
        notificationManager.notify(1, notificationBTC);
    }

    private boolean isDeviceSecured() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (manager == null) {
                manager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            }
            System.out.println("isDeviceSecured:" + manager.isKeyguardLocked());
            return manager.isKeyguardLocked();
        }
        System.out.println("===>  Version: isDeviceSecured:" + false);
        return false;
    }

    private synchronized boolean getOnPause() {
        return this.onPause;
    }

    private synchronized void setOnPause(boolean onPause) {
        this.onPause = onPause;
    }
}
