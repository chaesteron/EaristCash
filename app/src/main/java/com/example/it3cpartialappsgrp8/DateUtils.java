package com.example.it3cpartialappsgrp8;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    public static long convertDateTimeToTimestamp(String createdAt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            Date date = sdf.parse(createdAt);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public  static String convertTimestampToDateTime(  long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.ENGLISH);
            Date date = new Date(timestamp);
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String formatDateTimeToWordDate(String dateTime){
        String formattedDate = "";
        // Specify the input date format
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.ENGLISH);

            // Specify the desired output date format
        DateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.ENGLISH);
        outputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        try {
            // Parse the input date string
            Date date = inputFormat.parse(dateTime);

            // Format the date to the desired output format
            formattedDate = outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  formattedDate;
    }
}
