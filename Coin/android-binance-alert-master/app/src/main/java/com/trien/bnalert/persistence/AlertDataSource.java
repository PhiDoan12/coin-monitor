package com.trien.bnalert.persistence;

import com.trien.bnalert.models.Alert;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Access point for managing alert data.
 */
public interface AlertDataSource {

    /**
     * Gets the alert from the data source.
     *
     * @return the alert from the data source.
     */
    Flowable<List<Alert>> getAllAlerts();

    /**
     * Inserts the alert into the data source, or, if this is an existing alert, updates it.
     *
     * @param alert the alert to be inserted or updated.
     */
    Completable insertOrUpdateAlert(Alert alert);

    /**
     * Deletes an alert from the data source.
     */
    Completable deleteAnAlert(Alert alert);
}
