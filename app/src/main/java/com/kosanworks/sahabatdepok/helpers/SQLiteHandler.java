package com.kosanworks.sahabatdepok.helpers;

/**
 * Created by ghost on 21/08/16.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "sahabatDepok_DB";

    // Login table name
    private static final String TABLE_USER = "user";

    // Login Table Columns names
    private static final String KEY_ID = "userid";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_AVATAR = "avatar";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_CREATE_AT = "create_at";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TOKEN + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_USERNAME + " TEXT,"
                + KEY_AVATAR + " TEXT,"+ KEY_PHONE + " TEXT,"+ KEY_CREATE_AT +" TEXT)";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.e(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     */
    public void addUser(String token, String email, String username, String avatar, String phone, String create_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TOKEN, token); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_USERNAME, username); // Token
        values.put(KEY_AVATAR, avatar); // Avatar
        values.put(KEY_PHONE, phone); // Avatar
        values.put(KEY_CREATE_AT, create_at); // Avatar
        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);

        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("token", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("username", cursor.getString(3));
            user.put("avatar", cursor.getString(4));
            user.put("phone", cursor.getString(5));
            user.put("create_at", cursor.getString(6));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

//        Log.d(TAG, "Deleted all user info from sqlite");
    }

    public void updateUser(String email, String username, String avatar_link, String phone, String create_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_USERNAME, username); // Token
        values.put(KEY_AVATAR, avatar_link); // Avatar
        values.put(KEY_PHONE, phone); // Avatar
        values.put(KEY_CREATE_AT, create_at); // Avatar
        // Inserting Row
        long id = db.update(TABLE_USER, values, KEY_ID + "= ?", new String[]{String.valueOf(1)});

        db.close(); // Closing database connection
        Log.e("berbasil update foto",avatar_link);
        Log.d(TAG, "Update into sqlite: " + id);
    }

}
