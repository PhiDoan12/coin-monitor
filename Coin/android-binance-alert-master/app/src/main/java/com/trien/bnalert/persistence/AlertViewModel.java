package com.trien.bnalert.persistence;

import androidx.lifecycle.ViewModel;

import com.trien.bnalert.models.Alert;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * View Model for the {@link com.trien.bnalert.fragment.AlertFragment}
 */
public class AlertViewModel extends ViewModel {

    private final AlertDataSource mDataSource;

    private List<Alert> alertList;

    public AlertViewModel(AlertDataSource dataSource) {
        mDataSource = dataSource;
    }

    /**
     * Get the alert list.
     *
     * @return a {@link Flowable<List<Alert>>} that will emit every time the alert list has been updated.
     */
    public Flowable<List<Alert>> getAllAlerts() {

        return mDataSource.getAllAlerts()
                // for every emission of the user, get the user name
                .map(alerts -> alerts);

    }

    /**
     * Insert an alert.
     *
     * @param alert the new alert
     * @return a {@link Completable} that completes when the alert is updated
     */
    public Completable insertOrUpdateAlert(final Alert alert) {
        // if there's no such alert, create a new alert.
        // if we already have an alert, then update it, but since the alert is immutable,
        // when we pass the alert parameter, that alert object needs to have the same alertId with the old one.
        return mDataSource.insertOrUpdateAlert(alert);
    }

    /**
     * Delete an alert.
     *
     * @param alert the new alert
     * @return a {@link Completable} that completes when the alert is updated
     */
    public Completable deleteAlert(final Alert alert) {
        // if there's no such alert, create a new alert.
        // if we already have an alert, then update it, but since the alert is immutable,
        // when we pass the alert parameter, that alert object needs to have the same alertId with the old one.
        return mDataSource.deleteAnAlert(alert);
    }
}
