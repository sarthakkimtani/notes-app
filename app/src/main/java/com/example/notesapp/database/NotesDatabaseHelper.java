package com.example.notesapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.notesapp.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NotesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NOTES = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_USER_EMAIL = "user_email";

    public NotesDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NOTES + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_USER_EMAIL + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NOTES + " ADD COLUMN " + COLUMN_USER_EMAIL + " TEXT");
        }
    }

    public void addNote(Note note, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, note.getId());
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());
        values.put(COLUMN_USER_EMAIL, userEmail);

        db.insert(TABLE_NOTES, null, values);
        db.close();
    }

    public void updateNote(Note note, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_CONTENT, note.getContent());

        db.update(TABLE_NOTES, values, COLUMN_ID + " = ? AND " + COLUMN_USER_EMAIL + " = ?", new String[]{note.getId(), userEmail});
        db.close();
    }

    public void deleteNote(String noteId, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_ID + " = ? AND " + COLUMN_USER_EMAIL + " = ?", new String[]{noteId, userEmail});
        db.close();
    }

    public List<Note> getAllNotes(String userEmail) {
        List<Note> notesList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_USER_EMAIL + " = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                );
                notesList.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return notesList;
    }
}