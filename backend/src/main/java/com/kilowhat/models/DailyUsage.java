package com.kilowhat.models;

public class DailyUsage {
    public int id;
    public String date; // yyyy-MM-dd, kept as String for simple JSON in/out
    public double kwh;
}
