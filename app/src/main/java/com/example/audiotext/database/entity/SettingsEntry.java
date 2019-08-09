package com.example.audiotext.database.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by JL on 08/07/2019.
 */

@Entity(tableName = "settings")
public class SettingsEntry {
    @PrimaryKey(autoGenerate = false)
    private int id;
    private int speekPitch;
    private int speekSpeed;

    public SettingsEntry() {
    }

    public SettingsEntry(int id, int speekPitch, int speekSpeed) {
        this.id = id;
        this.speekPitch = speekPitch;
        this.speekSpeed = speekSpeed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSpeekPitch() {
        return speekPitch;
    }

    public void setSpeekPitch(int speekPitch) {
        this.speekPitch = speekPitch;
    }

    public int getSpeekSpeed() {
        return speekSpeed;
    }

    public void setSpeekSpeed(int speekSpeed) {
        this.speekSpeed = speekSpeed;
    }
}
