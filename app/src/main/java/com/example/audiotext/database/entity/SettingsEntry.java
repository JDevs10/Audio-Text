package com.example.audiotext.database.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by JL on 08/07/2019.
 */

@Entity(tableName = "settings")
public class SettingsEntry {
    @PrimaryKey(autoGenerate = false)
    private int id;
    private int speekPitch;
    private int speekSpeed;
    private String localeLanguage;
    private String localeCountry;
    private String localeVariant;

    public SettingsEntry() {
    }

    public SettingsEntry(int id, int speekPitch, int speekSpeed, String localeLanguage, String localeCountry, String localeVariant) {
        this.id = id;
        this.speekPitch = speekPitch;
        this.speekSpeed = speekSpeed;
        this.localeLanguage = localeLanguage;
        this.localeCountry = localeCountry;
        this.localeVariant = localeVariant;
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

    public String getLocaleLanguage() {
        return localeLanguage;
    }

    public void setLocaleLanguage(String localeLanguage) {
        this.localeLanguage = localeLanguage;
    }

    public String getLocaleCountry() {
        return localeCountry;
    }

    public void setLocaleCountry(String localeCountry) {
        this.localeCountry = localeCountry;
    }

    public String getLocaleVariant() {
        return localeVariant;
    }

    public void setLocaleVariant(String localeVariant) {
        this.localeVariant = localeVariant;
    }
}
