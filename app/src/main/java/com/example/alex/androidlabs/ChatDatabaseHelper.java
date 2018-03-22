package com.example.alex.androidlabs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Alex on 2018-03-03.
 */

public class ChatDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "chatDatabase";
    public static int VERSION_NUM = 4;
    public static final String TABLE_NAME = "ChatMessage";
    public static final String KEY_ID = "_id";
    public static final String KEY_MESSAGE = "message";



    public ChatDatabaseHelper(Context ctx){
        super(ctx,DATABASE_NAME,null,VERSION_NUM);

    }

    public void onCreate(SQLiteDatabase db) {
        Log.i("ChatDatabaseHelper", "Calling onCreate" );
        String CREATE_CHAT_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_MESSAGE + " MESSAGE)";
        Log.i("ChatDatabaseHelper", "Testing: " + CREATE_CHAT_TABLE);
        db.execSQL(CREATE_CHAT_TABLE);
        //db.execSQL("INSERT INTO " + TABLE_NAME + " VALUES('hi')");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Log.i("ChatDatabaseHelper", "Calling onUpgrade, oldVersion " + oldVersion +" newVersion=" +  newVersion );
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Log.i("ChatDatabaseHelper", "Calling onDowngrade, oldVersion " + oldVersion +" newVersion=" +  newVersion );
    }


}
