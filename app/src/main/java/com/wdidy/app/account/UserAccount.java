package com.wdidy.app.account;

import android.content.Context;
import android.content.SharedPreferences;

import com.wdidy.app.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 23/10/2015.
 */
public class UserAccount {

    private String name, email, city, country, password, IDuser;
    private boolean isCreated;
    private int age;

    public UserAccount () {
        this.isCreated = false;
    }

    public UserAccount(JSONObject data) throws JSONException {
        this.IDuser = data.getString("IDuser");
        this.name = data.getString("firstname") + " " + data.getString("lastname");
        this.country = data.getString("country");
        this.city = data.getString("city");
        this.isCreated = true;
    }

    public void readAccountPromPrefs (Context context) {

        SharedPreferences prefs_Read;

        prefs_Read = context.getSharedPreferences(Constants.PREFS_ACCOUNT_KEY, 0);

        this.IDuser = prefs_Read.getString(Constants.PREFS_ACCOUNT_IDUSER, "");
        this.name = prefs_Read.getString(Constants.PREFS_ACCOUNT_NAME, "");
        this.email = prefs_Read.getString(Constants.PREFS_ACCOUNT_EMAIL, "");
        this.password = prefs_Read.getString(Constants.PREFS_ACCOUNT_PASSWORD, "");
        this.age = prefs_Read.getInt(Constants.PREFS_ACCOUNT_AGE, 0);
        this.city = prefs_Read.getString(Constants.PREFS_ACCOUNT_CITY, "");
        this.country = prefs_Read.getString(Constants.PREFS_ACCOUNT_COUNTRY, "");
        this.isCreated = prefs_Read.getBoolean(Constants.PREFS_ACCOUNT_CREATED, false);
    }

    public void registerAccountInPrefs (Context context) {

        SharedPreferences prefs_Read;
        SharedPreferences.Editor prefs_Write;

        prefs_Read = context.getSharedPreferences(Constants.PREFS_ACCOUNT_KEY, 0);
        prefs_Write = prefs_Read.edit();

        prefs_Write.putString(Constants.PREFS_ACCOUNT_IDUSER, this.IDuser);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_NAME, this.name);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_EMAIL, this.email);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_PASSWORD, this.password);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_CITY, this.city);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_COUNTRY, this.country);
        prefs_Write.putInt(Constants.PREFS_ACCOUNT_AGE, this.age);
        prefs_Write.putBoolean(Constants.PREFS_ACCOUNT_CREATED, this.isCreated);
        prefs_Write.apply();
    }

    public void removeAccount (Context context) {
        this.IDuser = "";
        this.name = "";
        this.email = "";
        this.password = "";
        this.city = "";
        this.country = "";
        this.isCreated = false;
        registerAccountInPrefs(context);
    }

    public String getName() {
        return name;
    }

    public String getUserID () {
        return IDuser;
    }

    public String getEmail() {
        return email;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getPassword() {
        return password;
    }

    public int getAge() {
        return age;
    }

    public boolean isCreated() {
        return isCreated;
    }
}
