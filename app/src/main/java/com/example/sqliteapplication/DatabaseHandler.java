package com.example.sqliteapplication;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DatabaseHandler {

    private AppCompatActivity activity;

    private SQLiteDatabase database;
    private static final String DATABASE_NAME = "fcu_breakfast_v2.db";
    private static final String CREATE_MEAL_TABLE = "CREATE TABLE IF NOT EXISTS MealWithImage (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " image Blob, " +
            " name TEXT NOT NULL, " +
            " description TEXT, " +
            " price INTEGER NOT NULL)";

    public DatabaseHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void open() {
        database = activity.openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        database.execSQL(CREATE_MEAL_TABLE);
    }

    public void upgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql = "DROP TABLE " + "MealWithImage";
        sqLiteDatabase.execSQL(sql);
    }

    public void addMeal(byte[] image, String name, String description, int price) {
        ContentValues values = new ContentValues();
        values.put("image", image);
        values.put("name", name);
        values.put("description", description);
        values.put("price", price);
        database.insert("MealWithImage", null, values);
    }

    public void deleteMeal(String id) {
        database.delete("MealWithImage", "_id = ?", new String[]{id});
    }


    public Cursor getAllMeals() {
        Cursor cursor = database.rawQuery("SELECT * FROM MealWithImage", null);
        while (cursor.moveToNext()) {
            String mealName = cursor.getString(2);
        }
        Toast.makeText(activity, cursor.getCount() + " is added", Toast.LENGTH_SHORT).show();
        return cursor;
    }
}
