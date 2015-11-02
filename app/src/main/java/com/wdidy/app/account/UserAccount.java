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

    public String name, email, city, country, password;
    public int age;

    public UserAccount () {}

    public UserAccount(String name, String email, String password, String city, String country, int age) {
        this.name = name;
        this.email = email;
        this.city = city;
        this.country = country;
        this.age = age;
        this.password = password;
    }

    public UserAccount(JSONObject data) throws JSONException {
        this.name = data.getString("name") + " " + data.getString("lastname");
        this.country = data.getString("country");
        this.city = data.getString("city");
    }

    public void readAccountPromPrefs (Context context) {

        SharedPreferences prefs_Read;

        prefs_Read = context.getSharedPreferences(Constants.PREFS_ACCOUNT_KEY, 0);

        this.name = prefs_Read.getString(Constants.PREFS_ACCOUNT_NAME, "");
        this.email = prefs_Read.getString(Constants.PREFS_ACCOUNT_EMAIL, "");
        this.password = prefs_Read.getString(Constants.PREFS_ACCOUNT_PASSWORD, "");
        this.age = prefs_Read.getInt(Constants.PREFS_ACCOUNT_AGE, 0);
        this.city = prefs_Read.getString(Constants.PREFS_ACCOUNT_CITY, "");
        this.country = prefs_Read.getString(Constants.PREFS_ACCOUNT_COUNTRY, "");
    }

    public void registerAccountInPrefs (Context context) {

        SharedPreferences prefs_Read;
        SharedPreferences.Editor prefs_Write;

        prefs_Read = context.getSharedPreferences(Constants.PREFS_ACCOUNT_KEY, 0);
        prefs_Write = prefs_Read.edit();

        prefs_Write.putString(Constants.PREFS_ACCOUNT_NAME, this.name);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_EMAIL, this.email);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_PASSWORD, this.password);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_CITY, this.city);
        prefs_Write.putString(Constants.PREFS_ACCOUNT_COUNTRY, this.country);
        prefs_Write.putInt(Constants.PREFS_ACCOUNT_AGE, this.age);
        prefs_Write.apply();
    }

    public String getName() {
        return name;
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
}
