package com.google.mediapipe.examples.holidaykeeper;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.Intent;
import android.net.Uri;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddEventActivity extends AppCompatActivity {

    private static final String TAG = "AddEventActivity";
    private EditText editTextName;
    private EditText editTextDate;
    private LinearLayout ideasContainer;
    private ScrollView scrollViewIdeas;
    private DatabaseHelper databaseHelper;

    private List<View> ideaViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        Log.d(TAG, "onCreate: Activity created");

        databaseHelper = new DatabaseHelper(this);

        initViews();
    }

    private void initViews() {
        Button btnBackFromAdd = findViewById(R.id.btnBackFromAdd);
        Button btnBackFromAdd2 = findViewById(R.id.btnBackFromAdd2);
        Button btnSave = findViewById(R.id.button3);
        editTextName = findViewById(R.id.editTextText);
        editTextDate = findViewById(R.id.editTextDate);
        TextView textView7 = findViewById(R.id.textView7);

        ideasContainer = findViewById(R.id.linearLayoutIdeasContainer);
        scrollViewIdeas = findViewById(R.id.scrollViewIdeas);

        if (ideasContainer == null) {
            createIdeasContainer();
        }

        if (scrollViewIdeas == null) {

        }

        if (textView7 == null) {
            Log.e(TAG, "textView7 (R.id.textView7) is NULL!");
            return;
        }

        Log.d(TAG, "All views initialized successfully");

        btnBackFromAdd.setOnClickListener(v -> {
            Navigator.navigate(AddEventActivity.this, MainActivity.class, false);
        });

        btnBackFromAdd2.setOnClickListener(v -> {
            Navigator.navigate(AddEventActivity.this, MainActivity.class, false);
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEventToDatabase();
            }
        });

        textView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "textView7 onClick called - adding new idea field");
                addNewIdeaField();
            }
        });
    }

    private void createIdeasContainer() {

        ScrollView scrollView = new ScrollView(this);
        scrollView.setId(R.id.scrollViewIdeas);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        scrollView.setVisibility(View.GONE);

        ideasContainer = new LinearLayout(this);
        ideasContainer.setId(R.id.linearLayoutIdeasContainer);
        ideasContainer.setOrientation(LinearLayout.VERTICAL);
        ideasContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        scrollView.addView(ideasContainer);

        LinearLayout mainLayout = findViewById(R.id.main);
        if (mainLayout != null) {

        }
    }

    private void addNewIdeaField() {

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
        ideaEditText.setText("");
        ideaEditText.setHint("Название идеи");

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

        copyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(ideaEditText);
            }
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

        ozonTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOzonSearch(ideaEditText);
            }
        });

        Button deleteButton = new Button(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        deleteButton.setText("×");
        deleteButton.setTextSize(20);
        deleteButton.setPadding(16, 0, 16, 0);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ideasContainer.removeView(ideaRowLayout);
                ideaViews.remove(ideaRowLayout);

                updateScrollViewVisibility();
            }
        });

        textViewsLayout.addView(copyTextView);
        textViewsLayout.addView(ozonTextView);

        ideaRowLayout.addView(ideaEditText);
        ideaRowLayout.addView(textViewsLayout);
        ideaRowLayout.addView(deleteButton);

        ideasContainer.addView(ideaRowLayout);
        ideaViews.add(ideaRowLayout);

        updateScrollViewVisibility();

        if (scrollViewIdeas != null) {
            scrollViewIdeas.post(new Runnable() {
                @Override
                public void run() {
                    scrollViewIdeas.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    private void updateScrollViewVisibility() {
        if (ideasContainer != null && scrollViewIdeas != null) {
            if (ideasContainer.getChildCount() == 0) {
                scrollViewIdeas.setVisibility(View.GONE);
            } else {
                scrollViewIdeas.setVisibility(View.VISIBLE);
            }
        }
    }

    private void saveEventToDatabase() {
        try {

            String name = editTextName.getText().toString().trim();
            String date = editTextDate.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show();
                editTextName.requestFocus();
                return;
            }

            if (date.isEmpty()) {
                Toast.makeText(this, "Введите дату", Toast.LENGTH_SHORT).show();
                editTextDate.requestFocus();
                return;
            }

            if (!date.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                Toast.makeText(this, "Дата должна быть в формате дд.мм.гггг", Toast.LENGTH_SHORT).show();
                editTextDate.requestFocus();
                return;
            }

            Log.d(TAG, "Saving event to database: Name=" + name + ", Date=" + date);

            long eventId = databaseHelper.addEvent(name, date);

            if (eventId != -1) {
                Toast.makeText(this, "Событие сохранено!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Event saved successfully with ID: " + eventId);

                saveIdeasForEvent(eventId);

                editTextName.setText("");
                editTextDate.setText("");
                clearIdeaFields();

            } else {
                Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to save event to database");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving event: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveIdeasForEvent(long eventId) {
        if (ideasContainer == null || ideasContainer.getChildCount() == 0) {
            Log.d(TAG, "No ideas to save");
            return;
        }

        int savedCount = 0;

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
                            long result = databaseHelper.addIdea(eventId, ideaText);
                            if (result != -1) {
                                savedCount++;
                                Log.d(TAG, "Idea saved: " + ideaText);
                            }
                        }
                        break;
                    }
                }
            }
        }

        if (savedCount > 0) {
            Toast.makeText(this, "Сохранено " + savedCount + " идей", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Saved " + savedCount + " ideas for event ID: " + eventId);
        }
    }

    private void clearIdeaFields() {
        if (ideasContainer != null) {
            ideasContainer.removeAllViews();
            ideaViews.clear();
            updateScrollViewVisibility();
        }
    }

    private void copyToClipboard(EditText editText) {
        try {
            if (editText == null) {
                Log.e(TAG, "EditText is null");
                Toast.makeText(this, "Ошибка: поле не найдено", Toast.LENGTH_SHORT).show();
                return;
            }

            String textToCopy = editText.getText().toString().trim();

            if (textToCopy.isEmpty()) {
                Log.d(TAG, "Text to copy is empty");
                Toast.makeText(this, "Поле пустое, нечего копировать", Toast.LENGTH_SHORT).show();
                return;
            }

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard == null) {
                Log.e(TAG, "ClipboardManager is null");
                Toast.makeText(this, "Ошибка доступа к буферу обмена", Toast.LENGTH_SHORT).show();
                return;
            }

            ClipData clip = ClipData.newPlainText("gift_idea", textToCopy);
            clipboard.setPrimaryClip(clip);

            Log.d(TAG, "Text copied to clipboard: " + textToCopy);

            Toast.makeText(this, "Текст скопирован в буфер обмена", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Error copying to clipboard: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при копировании", Toast.LENGTH_SHORT).show();
        }
    }

    private void openOzonSearch(EditText editText) {
        try {

            String giftIdea = "";
            if (editText != null) {
                giftIdea = editText.getText().toString().trim();
                Log.d(TAG, "Text from editText: " + giftIdea);
            }

            if (giftIdea.isEmpty()) {
                giftIdea = "подарок";
            }

            String encodedQuery = Uri.encode(giftIdea, "UTF-8");

            String ozonUrl = "https://www.ozon.ru/search/?text=" + encodedQuery;
            Log.d(TAG, "Opening URL: " + ozonUrl);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(ozonUrl));

            Intent ozonAppIntent = new Intent(Intent.ACTION_VIEW);
            ozonAppIntent.setData(Uri.parse("ozon://search?text=" + encodedQuery));

            try {
                if (ozonAppIntent.resolveActivity(getPackageManager()) != null) {
                    Log.d(TAG, "Opening in Ozon app");
                    startActivity(ozonAppIntent);
                } else {

                    Log.d(TAG, "Ozon app not found, opening in browser");
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error starting activity: " + e.getMessage());

                try {
                    startActivity(intent);
                } catch (Exception e2) {
                    Log.e(TAG, "Second attempt failed: " + e2.getMessage());
                    Toast.makeText(AddEventActivity.this,
                            "Не удалось открыть Ozon",
                            Toast.LENGTH_SHORT).show();
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in openOzonSearch: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Ошибка при открытии Ozon", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}