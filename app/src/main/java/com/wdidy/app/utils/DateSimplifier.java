package com.wdidy.app.utils;

import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Rascafr on 16/02/2016.
 */
public class DateSimplifier {

    private Date date;
    private SimpleDateFormat simpleDateFormat;
    private String easyDate;

    /**
     * Classic constructor
     *
     * @param date the date object to use
     */
    public DateSimplifier(Date date) {
        this.date = date;
    }

    /**
     * String constructor
     *
     * @param sDate the date in SQL format : yyyy-MM-dd HH:mm:ss
     */
    public DateSimplifier(String sDate) {
        this.simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
        this.easyDate = "non supporté";
        try {
            this.date = simpleDateFormat.parse(sDate);
            Calendar c = Calendar.getInstance();

            // Day
            int c_day = c.get(Calendar.DATE);
            int day = Integer.parseInt(DateFormat.format("dd", date).toString());
            int c_y_day = c.get(Calendar.DAY_OF_YEAR);

            // Month
            int c_month = c.get(Calendar.MONTH) + 1;
            int month = Integer.parseInt(DateFormat.format("MM", date).toString());

            // Year
            int c_year = c.get(Calendar.YEAR);
            int year = Integer.parseInt(DateFormat.format("yyyy", date).toString());

            Calendar paramCalendar = Calendar.getInstance();
            paramCalendar.set(year, month - 1, day);
            int y_day = paramCalendar.get(Calendar.DAY_OF_YEAR);

            // If same year
            if (year == c_year) {
                // If same month and same day
                if (month == c_month && day == c_day) {
                    // returns HH:mm
                    this.easyDate = new SimpleDateFormat("HH:mm", Locale.FRANCE).format(date);
                } else if (c_y_day - y_day < 7) {
                    // If same relative week, returns day name
                    this.easyDate = new SimpleDateFormat("EEE", Locale.FRANCE).format(date);
                } else if (month == c_month) {
                    // If same month only (not same relative week, not same day), returns day name and number
                    this.easyDate = new SimpleDateFormat("EEE d", Locale.FRANCE).format(date);
                } else {
                    // If not the same month, returns month + day number
                    this.easyDate = new SimpleDateFormat("d MMM", Locale.FRANCE).format(date);
                }
            } else {
                // If not the same year, return day number + month + year
                this.easyDate = new SimpleDateFormat("d MMM yyyy", Locale.FRANCE).format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getEasyDate() {
        return easyDate;
    }
}
