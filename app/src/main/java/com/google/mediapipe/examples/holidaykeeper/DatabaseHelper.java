package com.google.mediapipe.examples.holidaykeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "HolidayKeeper.db";
    private static final int DB_VERSION = 1;
    private Context myContext;

    static final String TABLE_EVENTS = "Events";
    static final String EVENT_ID = "id_event";
    static final String EVENT_NAME = "name";
    static final String EVENT_DATE = "date";

    static final String TABLE_IDEAS = "Ideas";
    static final String IDEA_ID = "id_idea";
    static final String IDEA_NAME = "name";
    static final String IDEA_EVENT_ID = "event_id";

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.myContext = context;
        create_db();
    }

    public void create_db() {
        String dbPath = myContext.getDatabasePath(DB_NAME).getPath();

        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs();

            try (InputStream myInput = myContext.getAssets().open(DB_NAME);
                 OutputStream myOutput = new FileOutputStream(dbPath)) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
            } catch (IOException ex) {
                Log.e("DatabaseHelper", "Error copying database", ex);
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEventsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_EVENTS + " (" +
                EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EVENT_NAME + " TEXT NOT NULL, " +
                EVENT_DATE + " TEXT NOT NULL)";

        String createIdeasTable = "CREATE TABLE IF NOT EXISTS " + TABLE_IDEAS + " (" +
                IDEA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                IDEA_NAME + " TEXT NOT NULL, " +
                IDEA_EVENT_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + IDEA_EVENT_ID + ") REFERENCES " +
                TABLE_EVENTS + "(" + EVENT_ID + ") ON DELETE CASCADE)";

        db.execSQL(createEventsTable);
        db.execSQL(createIdeasTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IDEAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    public long addEvent(String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(EVENT_NAME, name);
            values.put(EVENT_DATE, date);

            result = db.insert(TABLE_EVENTS, null, values);
            Log.d("DatabaseHelper", "Event added successfully with ID: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding event: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }

        return result;
    }

    public long addIdea(long eventId, String ideaName) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(IDEA_NAME, ideaName);
            values.put(IDEA_EVENT_ID, eventId);

            result = db.insert(TABLE_IDEAS, null, values);
            Log.d("DatabaseHelper", "Idea added successfully with ID: " + result);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding idea: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }

        return result;
    }

    public SQLiteDatabase open() throws SQLException {
        String dbPath = myContext.getDatabasePath(DB_NAME).getPath();
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public List<String> getAllEventDates() {
        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + EVENT_DATE + " FROM " + TABLE_EVENTS;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(EVENT_DATE));
                    dates.add(date);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting event dates", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return dates;
    }

    public List<String> getEventsByDate(String date) {
        List<String> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + EVENT_NAME + " FROM " + TABLE_EVENTS +
                " WHERE " + EVENT_DATE + " = ?";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String eventName = cursor.getString(cursor.getColumnIndexOrThrow(EVENT_NAME));
                    events.add(eventName);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting events by date", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return events;
    }

    public EventData getEventByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        EventData eventData = null;

        String query = "SELECT * FROM " + TABLE_EVENTS +
                " WHERE " + EVENT_DATE + " = ? LIMIT 1";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(EVENT_NAME));
                String eventDate = cursor.getString(cursor.getColumnIndexOrThrow(EVENT_DATE));

                eventData = new EventData(id, name, eventDate);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting event by date", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return eventData;
    }

    public static class EventData {
        public int id;
        public String name;
        public String date;

        public EventData(int id, String name, String date) {
            this.id = id;
            this.name = name;
            this.date = date;
        }
    }

    public List<String> getIdeasForEvent(int eventId) {
        List<String> ideas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + IDEA_NAME + " FROM " + TABLE_IDEAS +
                " WHERE " + IDEA_EVENT_ID + " = ?";
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(eventId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String ideaName = cursor.getString(cursor.getColumnIndexOrThrow(IDEA_NAME));
                    ideas.add(ideaName);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting ideas for event", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return ideas;
    }

    public boolean updateEvent(int eventId, String name, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(EVENT_NAME, name);
            values.put(EVENT_DATE, date);

            int rowsAffected = db.update(TABLE_EVENTS, values,
                    EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});

            success = (rowsAffected > 0);
            Log.d("DatabaseHelper", "Event updated: " + success);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating event", e);
        } finally {
            db.close();
        }

        return success;
    }

    public boolean deleteIdeasForEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            int rowsDeleted = db.delete(TABLE_IDEAS,
                    IDEA_EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});

            success = (rowsDeleted >= 0); // Может быть 0 если идей не было
            Log.d("DatabaseHelper", "Deleted " + rowsDeleted + " ideas for event " + eventId);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting ideas for event", e);
        } finally {
            db.close();
        }

        return success;
    }

    public boolean deleteEventAndIdeas(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {

            db.beginTransaction();

            db.delete(TABLE_IDEAS, IDEA_EVENT_ID + " = ?",
                    new String[]{String.valueOf(eventId)});

            int rowsAffected = db.delete(TABLE_EVENTS, EVENT_ID + " = ?",
                    new String[]{String.valueOf(eventId)});

            db.setTransactionSuccessful();
            success = (rowsAffected > 0);

            Log.d("DatabaseHelper", "Event " + eventId + " and its ideas deleted: " + success);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting event and ideas", e);
            success = false;
        } finally {
            db.endTransaction();
            db.close();
        }

        return success;
    }
}