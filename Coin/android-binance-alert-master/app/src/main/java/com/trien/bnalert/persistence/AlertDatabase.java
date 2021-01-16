package com.trien.bnalert.persistence;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.trien.bnalert.models.Alert;

/**
 * The Room database that contains the Alerts table
 */
@Database(entities = {Alert.class}, version = 1)
public abstract class AlertDatabase extends RoomDatabase {

    private static volatile AlertDatabase INSTANCE;

    public abstract AlertDao alertDao();

    public static AlertDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AlertDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AlertDatabase.class, "Binance.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
