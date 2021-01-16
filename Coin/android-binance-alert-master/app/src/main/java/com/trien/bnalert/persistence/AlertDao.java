package com.trien.bnalert.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.trien.bnalert.models.Alert;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Data Access Object for the alerts table.
 */
@Dao
public interface AlertDao {

    /**
     * Get all the alerts from the table.
     *
     * @return all the alerts from the table
     */
    @Query("SELECT * FROM alerts")
    Flowable<List<Alert>> getAllAlerts();

    /**
     * Insert an alert in the database. If the alert already exists, replace it.
     *
     * @param alert the alert to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAlert(Alert alert);

    /**
     * Update an alert in the database.
     *
     * @param alert the alert to be updated.
     */
    @Update
    Completable update(Alert alert); // we do not need to use this as the Insert method can handle this

    /**
     * Delete 1 alert.
     */
    @Delete
    Completable deleteAlert(Alert alert);

    /**
     * Delete all alerts.
     */
    @Query("DELETE FROM alerts")
    void deleteAllAlerts();
}
