package com.google.mediapipe.examples.holidaykeeper;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        Button btnCalendar = findViewById(R.id.btnCalendar);
        Button btnAddEvent = findViewById(R.id.btnAddEvent);

        btnCalendar.setOnClickListener(v -> {
            Navigator.navigate(MainActivity.this, CalendarActivity.class, false);
        });

        btnAddEvent.setOnClickListener(v -> {
            Navigator.navigate(MainActivity.this, AddEventActivity.class, false);
        });
    }
}