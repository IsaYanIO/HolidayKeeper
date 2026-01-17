package com.google.mediapipe.examples.holidaykeeper;

import android.app.Activity;
import android.content.Intent;

public class Navigator {
    public static void navigate(Activity from, Class to, boolean replace) {
        Intent intent = new Intent(from, to);
        from.startActivity(intent);
        if (replace) {
            from.finish();
        }
    }
}
