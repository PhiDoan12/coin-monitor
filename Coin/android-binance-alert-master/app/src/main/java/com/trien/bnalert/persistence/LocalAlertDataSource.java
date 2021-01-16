package com.trien.bnalert.persistence;

import com.trien.bnalert.models.Alert;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Using the Room database as a data source.
 */
public class LocalAlertDataSource implements AlertDataSource {

    private final AlertDao mAlertDao;

    public LocalAlertDataSource(AlertDao AlertDao) {
        mAlertDao = AlertDao;
    }

    @Override
    public Flowable<List<Alert>> getAllAlerts() {
        return mAlertDao.getAllAlerts();
    }

    @Override
    public Completable insertOrUpdateAlert(Alert Alert) {
       return mAlertDao.insertAlert(Alert);
    }

    @Override
    public Completable deleteAnAlert(Alert alert) {
        return mAlertDao.deleteAlert(alert);
    }
}
