package com.google.mediapipe.examples.holidaykeeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private TextView tvMonth, tvYear;
    private DatabaseHelper dbHelper;
    private HashSet<String> eventDates;
    private SimpleDateFormat dbDateFormat;
    private SimpleDateFormat comparisonDateFormat;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dbDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        comparisonDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        initViews();
        loadEventDates();
        setupCalendar();
    }

    private void initViews() {
        Button btnEditEvent = findViewById(R.id.btnEditEvent);
        Button btnDelEvent = findViewById(R.id.btnDelEvent);
        Button btnBackFromCalendar = findViewById(R.id.btnBackFromCalendar);
        tableLayout = findViewById(R.id.tableLayout);
        tvMonth = findViewById(R.id.textView10);
        tvYear = findViewById(R.id.textView11);

        btnEditEvent.setOnClickListener(v -> {
            if (!selectedDate.isEmpty()) {

                Intent intent = new Intent(CalendarActivity.this, EditEventActivity.class);
                intent.putExtra("SELECTED_DATE", selectedDate);
                startActivity(intent);
            } else {
                Toast.makeText(CalendarActivity.this, "Сначала выберите дату", Toast.LENGTH_SHORT).show();
            }
        });

        btnDelEvent.setOnClickListener(v -> {
            if (!selectedDate.isEmpty()) {
                deleteEventsForSelectedDate();
            } else {
                Toast.makeText(CalendarActivity.this, "Сначала выберите дату", Toast.LENGTH_SHORT).show();
            }
        });

        btnBackFromCalendar.setOnClickListener(v -> {
            Navigator.navigate(CalendarActivity.this, MainActivity.class, false);
        });
    }

    private void deleteEventsForSelectedDate() {
        if (dbHelper == null) {
            dbHelper = new DatabaseHelper(this);
        }

        try {

            DatabaseHelper.EventData eventData = dbHelper.getEventByDate(selectedDate);

            if (eventData != null) {

                boolean deleted = dbHelper.deleteEventAndIdeas(eventData.id);

                if (deleted) {
                    Toast.makeText(this, "Событие и идеи удалены для даты: " + selectedDate,
                            Toast.LENGTH_SHORT).show();

                    loadEventDates();
                    setupCalendar();

                    hideActionButtons();

                    selectedDate = "";
                } else {
                    Toast.makeText(this, "Ошибка при удалении", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "На эту дату нет событий", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при удалении: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void hideActionButtons() {
        Button btnEditEvent = findViewById(R.id.btnEditEvent);
        Button btnDelEvent = findViewById(R.id.btnDelEvent);

        btnEditEvent.setVisibility(Button.INVISIBLE);
        btnDelEvent.setVisibility(Button.INVISIBLE);
    }

    private void loadEventDates() {
        eventDates = new HashSet<>();
        dbHelper = new DatabaseHelper(this);
        dbHelper.create_db();

        try {
            List<String> dates = dbHelper.getAllEventDates();

            for (String dateStr : dates) {
                try {
                    Date date = dbDateFormat.parse(dateStr);
                    if (date != null) {
                        String normalizedDate = dbDateFormat.format(date);
                        eventDates.add(normalizedDate);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupCalendar() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int today = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar monthCalendar = Calendar.getInstance();
        monthCalendar.set(Calendar.YEAR, currentYear);
        monthCalendar.set(Calendar.MONTH, currentMonth);
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        String[] monthNames = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        tvMonth.setText(monthNames[currentMonth]);
        tvYear.setText(String.valueOf(currentYear));

        int firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        int childCount = tableLayout.getChildCount();
        for (int i = 1; i < childCount; i++) {
            tableLayout.removeViewAt(1);
        }

        int day = 1;
        boolean monthFinished = false;

        Calendar now = Calendar.getInstance();
        int nowYear = now.get(Calendar.YEAR);
        int nowMonth = now.get(Calendar.MONTH);
        int nowDay = now.get(Calendar.DAY_OF_MONTH);

        boolean isDarkTheme = false;
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            isDarkTheme = true;
        }

        while (!monthFinished) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            for (int column = 0; column < 7; column++) {
                Button dayButton = new Button(this);
                dayButton.setLayoutParams(new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f));
                dayButton.setGravity(Gravity.CENTER);
                dayButton.setTextSize(14);

                if (isDarkTheme) {

                    dayButton.setTextColor(getResources().getColor(android.R.color.white));

                    dayButton.setBackgroundTintList(null);
                } else {

                    dayButton.setTextColor(getResources().getColor(android.R.color.black));
                }

                if ((day == 1 && column < offset) || day > daysInMonth) {
                    dayButton.setText("");
                    dayButton.setEnabled(false);
                    dayButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));

                    if (isDarkTheme) {
                        dayButton.setTextColor(getResources().getColor(android.R.color.white));
                    }
                } else {
                    dayButton.setText(String.valueOf(day));
                    dayButton.setTag(day);

                    final int currentDay = day;
                    dayButton.setOnClickListener(v -> {
                        onDaySelected(currentDay);
                    });

                    String dateStr = String.format(Locale.getDefault(), "%02d.%02d.%04d",
                            currentDay, currentMonth + 1, currentYear);

                    boolean hasEvent = eventDates.contains(dateStr);

                    boolean isToday = (day == nowDay && currentMonth == nowMonth && currentYear == nowYear);

                    if (isToday && hasEvent) {
                        dayButton.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                        dayButton.setTextColor(getResources().getColor(android.R.color.white));
                    } else if (isToday) {
                        dayButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                        dayButton.setTextColor(getResources().getColor(android.R.color.white));
                    } else if (hasEvent) {
                        dayButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

                        if (isDarkTheme) {
                            dayButton.setTextColor(getResources().getColor(android.R.color.white));
                        } else {
                            dayButton.setTextColor(getResources().getColor(android.R.color.black));
                        }
                    } else {
                        dayButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));

                        if (isDarkTheme) {
                            dayButton.setTextColor(getResources().getColor(android.R.color.white));
                        } else {
                            dayButton.setTextColor(getResources().getColor(android.R.color.black));
                        }
                    }

                    day++;
                    if (day > daysInMonth) {
                        monthFinished = true;
                    }
                }

                tableRow.addView(dayButton);
            }

            tableLayout.addView(tableRow);
        }
    }

    private void onDaySelected(int day) {
        Button btnEditEvent = findViewById(R.id.btnEditEvent);
        Button btnDelEvent = findViewById(R.id.btnDelEvent);

        btnEditEvent.setVisibility(Button.VISIBLE);
        btnDelEvent.setVisibility(Button.VISIBLE);

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        selectedDate = String.format(Locale.getDefault(), "%02d.%02d.%04d",
                day, currentMonth + 1, currentYear);

        Toast.makeText(this, "Выбрана дата: " + selectedDate, Toast.LENGTH_SHORT).show();

        loadEventsForDate(selectedDate);
    }

    private void loadEventsForDate(String date) {
        if (dbHelper != null) {
            List<String> events = dbHelper.getEventsByDate(date);
            if (!events.isEmpty()) {
                Toast.makeText(this, "События на эту дату: " + events.size(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}