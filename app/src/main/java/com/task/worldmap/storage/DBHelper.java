package com.task.worldmap.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SavedLocation";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_LOCATIONS = "tableLocation";
    public static final String COL_SEARCHED_ITEM = "locality";
    public static final String COL_TITLE = "title";
    public static final String COL_COMPLETE_LOCATION = "completeLocation";
    private Context context;
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }




    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_LOCALITY = "CREATE TABLE IF NOT EXISTS "
                + TABLE_LOCATIONS
                + " ("
                + COL_TITLE
                + " TEXT, "
                + COL_COMPLETE_LOCATION
                + " TEXT, "
                + COL_SEARCHED_ITEM
                + " TEXT )";

        sqLiteDatabase.execSQL(CREATE_LOCALITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +TABLE_LOCATIONS);
    }



    public List<String> getSearchedLocationIfExist(String searchedWord){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> listOfLocation = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM tableLocation where locality like '%" + searchedWord + "%' OR title like '%" + searchedWord + "%'",null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                listOfLocation.add(cursor.getString(cursor.getColumnIndex("title")) + " - "+cursor.getString(cursor.getColumnIndex("locality")));
            }

            }
        return listOfLocation;
    }

    public int getRowCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        String countQuery = "SELECT  * FROM tableLocation";
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null) {
            count = cursor.getCount();
        }
        if (db != null) {
            db.close();
        }
        if (cursor != null) {
            cursor.close();
        }
        return count;
    }


    public void insertSearchLocality(String locality,String titlte){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SEARCHED_ITEM, locality);
        values.put(COL_TITLE, titlte);
        values.put(COL_COMPLETE_LOCATION, titlte+" - "+locality);
        db.insert(TABLE_LOCATIONS, null,values);
    }

    public boolean checkWhereLocationIsAvailableOrNot(String location) {
        boolean result =false;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tableLocation where completeLocation =? ",new String[]{location});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                result = true;
            }
        }
        return result;
    }
}
