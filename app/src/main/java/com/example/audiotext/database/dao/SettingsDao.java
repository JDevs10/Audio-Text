package com.example.audiotext.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.audiotext.database.entity.SettingsEntry;

import java.util.List;

/**
 * Created by JL on 08/07/2019.
 */

@Dao
public interface SettingsDao {
    @Insert
    void insertSettings(SettingsEntry mSettingsEntry);

    @Query("SELECT * FROM settings WHERE id = 1")
    List<SettingsEntry> getSettings();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSettings(SettingsEntry mSettingsEntry);

    @Query("DELETE FROM settings")
    void deleteAllSettings();
}
