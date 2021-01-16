package com.trien.bnalert.persistence;

import android.content.Context;

/**
 * Enables injection of data sources.
 */
public class Injection {

    public static AlertDataSource provideAlertDataSource(Context context) {
        AlertDatabase database = AlertDatabase.getInstance(context);
        return new LocalAlertDataSource(database.alertDao());
    }

    public static ViewModelFactory provideViewModelFactory(Context context) {
        AlertDataSource dataSource = provideAlertDataSource(context);
        return new ViewModelFactory(dataSource);
    }
}
