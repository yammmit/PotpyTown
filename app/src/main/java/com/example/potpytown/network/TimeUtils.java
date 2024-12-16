package com.example.potpytown.network;

import java.util.Locale;

public class TimeUtils {
    public static String formatTimeToHHMMSS(long seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
    }
}

