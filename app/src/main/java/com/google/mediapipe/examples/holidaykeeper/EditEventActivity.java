package com.google.mediapipe.examples.holidaykeeper;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class EditEventActivity extends AppCompatActivity {

    private EditText editName, editDate;
    private LinearLayout ideasContainer;
    private ScrollView scrollViewIdeas;
    private DatabaseHelper dbHelper;
    private String selectedDate;
    private int eventId = -1;
    private List<String> eventIdeas = new ArrayList<>();

    private EditText staticEditText8;
    private TextView staticTextView26;
    private TextView staticTextView27;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        selectedDate = getIntent().getStringExtra("SELECTED_DATE");

        initViews();
        loadEventData();
    }

    private void initViews() {
        Button btnBackFromEdit = findViewById(R.id.btnBackFromEdit);
        Button btnBackFromEdit2 = findViewById(R.id.btnBackFromEdit2);
        Button btnSave = findViewById(R.id.button4);
        TextView textView29 = findViewById(R.id.textView29);

        editName = findViewById(R.id.editTextText7);
        editDate = findViewById(R.id.editTextDate4);

        staticEditText8 = findViewById(R.id.editTextText8);
        staticTextView26 = findViewById(R.id.textView26);
        staticTextView27 = findViewById(R.id.textView27);

        ideasContainer = findViewById(R.id.linearLayoutIdeasContainer);
        scrollViewIdeas = findViewById(R.id.scrollViewIdeas);

        btnBackFromEdit.setOnClickListener(v -> {
            Navigator.navigate(EditEventActivity.this, MainActivity.class, false);
        });

        btnBackFromEdit2.setOnClickListener(v -> {
            Navigator.navigate(EditEventActivity.this, CalendarActivity.class, false);
        });

        btnSave.setOnClickListener(v -> {
            saveEventData();
        });

        textView29.setOnClickListener(v -> {
            addNewIdeaField();
        });

        setupStaticFieldsHandlers();
    }

    private void setupStaticFieldsHandlers() {

        staticTextView26.setOnClickListener(v -> {
            String textToCopy = staticEditText8.getText().toString();
            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard =
                        (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Идея подарка", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Текст скопирован", Toast.LENGTH_SHORT).show();
            }
        });

        staticTextView27.setOnClickListener(v -> {
            String searchQuery = staticEditText8.getText().toString();
            if (searchQuery.isEmpty()) {
                Toast.makeText(this, "Введите название идеи для поиска", Toast.LENGTH_SHORT).show();
                return;
            }

            String ozonUrl = "https://www.ozon.ru/search/?text=" +
                    Uri.encode(searchQuery);

            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(ozonUrl)
            );
            startActivity(browserIntent);
        });
    }

    private void loadEventData() {
        dbHelper = new DatabaseHelper(this);

        if (selectedDate != null && !selectedDate.isEmpty()) {

            DatabaseHelper.EventData eventData = dbHelper.getEventByDate(selectedDate);

            if (eventData != null) {

                eventId = eventData.id;

                editName.setText(eventData.name);
                editDate.setText(eventData.date);

                loadEventIdeas(eventId);

                Toast.makeText(this, "Загружено событие: " + eventData.name, Toast.LENGTH_SHORT).show();
            } else {

                editDate.setText(selectedDate);
                editName.setText("");
                Toast.makeText(this, "Новое событие для даты: " + selectedDate, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Дата не выбрана", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadEventIdeas(int eventId) {

        clearStaticFields();

        if (ideasContainer != null) {
            ideasContainer.removeAllViews();
        }

        eventIdeas.clear();

        List<String> ideas = dbHelper.getIdeasForEvent(eventId);

        if (ideas != null && !ideas.isEmpty()) {
            eventIdeas.addAll(ideas);

            for (int i = 0; i < eventIdeas.size(); i++) {
                addIdeaField(i, eventIdeas.get(i));
            }
        }

        updateScrollViewVisibility();
    }

    private void clearStaticFields() {

        if (staticEditText8 != null) {
            staticEditText8.setText("");
            staticEditText8.setVisibility(View.GONE);
        }

        if (staticTextView26 != null) {
            staticTextView26.setVisibility(View.GONE);
        }

        if (staticTextView27 != null) {
            staticTextView27.setVisibility(View.GONE);
        }
    }

    private void addNewIdeaField() {
        int newIndex = eventIdeas.size();
        eventIdeas.add("");

        addIdeaField(newIndex, "");

        updateScrollViewVisibility();

        scrollViewIdeas.post(() -> scrollViewIdeas.fullScroll(View.FOCUS_DOWN));
    }

    private void addIdeaField(int index, String ideaText) {

        LinearLayout ideaRowLayout = new LinearLayout(this);
        ideaRowLayout.setOrientation(LinearLayout.HORIZONTAL);
        ideaRowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ideaRowLayout.setPadding(0, 8, 0, 8);

        EditText ideaEditText = new EditText(this);
        ideaEditText.setId(View.generateViewId());
        LinearLayout.LayoutParams editTextParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        editTextParams.setMargins(0, 0, 8, 0);
        ideaEditText.setLayoutParams(editTextParams);
        ideaEditText.setEms(10);
        ideaEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
        ideaEditText.setText(ideaText);
        ideaEditText.setHint("Название идеи");
        ideaEditText.setTag(index);

        LinearLayout textViewsLayout = new LinearLayout(this);
        textViewsLayout.setOrientation(LinearLayout.VERTICAL);
        textViewsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView copyTextView = new TextView(this);
        copyTextView.setId(View.generateViewId());
        copyTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        copyTextView.setText("Скопировать");
        copyTextView.setTextColor(0xFF686666);
        copyTextView.setTextSize(16);

        copyTextView.setOnClickListener(v -> {

            String textToCopy = ideaEditText.getText().toString();
            ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Идея подарка", textToCopy);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Текст скопирован", Toast.LENGTH_SHORT).show();
        });

        TextView ozonTextView = new TextView(this);
        ozonTextView.setId(View.generateViewId());
        ozonTextView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ozonTextView.setText("Найти на OZON");
        ozonTextView.setTextColor(0xFF427FBA);
        ozonTextView.setTextSize(16);

        ozonTextView.setOnClickListener(v -> {

            String searchQuery = ideaEditText.getText().toString();
            if (searchQuery.isEmpty()) {
                Toast.makeText(this, "Введите название идеи для поиска", Toast.LENGTH_SHORT).show();
                return;
            }

            String ozonUrl = "https://www.ozon.ru/search/?text=" +
                    Uri.encode(searchQuery);

            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(ozonUrl)
            );
            startActivity(browserIntent);
        });

        Button deleteButton = new Button(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        deleteButton.setText("×");
        deleteButton.setTextSize(20);
        deleteButton.setPadding(16, 0, 16, 0);

        deleteButton.setOnClickListener(v -> {

            ideasContainer.removeView(ideaRowLayout);

            Object tag = ideaEditText.getTag();
            if (tag instanceof Integer) {
                int tagIndex = (int) tag;
                if (tagIndex >= 0 && tagIndex < eventIdeas.size()) {
                    eventIdeas.remove(tagIndex);

                    updateIdeaTags();
                }
            }

            updateScrollViewVisibility();
        });

        textViewsLayout.addView(copyTextView);
        textViewsLayout.addView(ozonTextView);

        ideaRowLayout.addView(ideaEditText);
        ideaRowLayout.addView(textViewsLayout);
        ideaRowLayout.addView(deleteButton);

        ideasContainer.addView(ideaRowLayout);
    }

    private void updateIdeaTags() {

        for (int i = 0; i < ideasContainer.getChildCount(); i++) {
            View child = ideasContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View innerChild = row.getChildAt(j);
                    if (innerChild instanceof EditText) {
                        innerChild.setTag(i);
                        break;
                    }
                }
            }
        }
    }

    private void updateScrollViewVisibility() {
        if (ideasContainer.getChildCount() == 0) {
            scrollViewIdeas.setVisibility(View.GONE);
        } else {
            scrollViewIdeas.setVisibility(View.VISIBLE);
        }
    }

    private void saveEventData() {

        String name = editName.getText().toString();
        String date = editDate.getText().toString();

        if (name.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isNewEvent = (eventId == -1);

        if (isNewEvent) {

            long newEventId = dbHelper.addEvent(name, date);
            if (newEventId != -1) {
                eventId = (int) newEventId;
                Toast.makeText(this, "Событие сохранено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка сохранения события", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {

            boolean updated = dbHelper.updateEvent(eventId, name, date);
            if (updated) {
                Toast.makeText(this, "Событие обновлено", Toast.LENGTH_SHORT).show();
            }
        }

        saveIdeas();

        Navigator.navigate(this, CalendarActivity.class, false);
    }

    private void saveIdeas() {
        if (eventId == -1) {
            return;
        }

        boolean deleted = dbHelper.deleteIdeasForEvent(eventId);

        if (deleted) {
            Toast.makeText(this, "Старые идеи удалены", Toast.LENGTH_SHORT).show();
        }

        List<String> ideasToSave = new ArrayList<>();

        for (int i = 0; i < ideasContainer.getChildCount(); i++) {
            View child = ideasContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;

                for (int j = 0; j < row.getChildCount(); j++) {
                    View innerChild = row.getChildAt(j);
                    if (innerChild instanceof EditText) {
                        EditText ideaEditText = (EditText) innerChild;
                        String ideaText = ideaEditText.getText().toString().trim();

                        if (!ideaText.isEmpty()) {
                            ideasToSave.add(ideaText);
                        }
                        break;
                    }
                }
            }
        }

        int savedCount = 0;
        for (String idea : ideasToSave) {
            long result = dbHelper.addIdea(eventId, idea);
            if (result != -1) {
                savedCount++;
            }
        }

        if (savedCount > 0) {
            Toast.makeText(this, "Сохранено " + savedCount + " идей", Toast.LENGTH_SHORT).show();
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