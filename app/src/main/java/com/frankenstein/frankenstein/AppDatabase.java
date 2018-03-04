package com.frankenstein.frankenstein;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

/**
 * Created by davidmena on 3/4/18.
 *
 * The app Databse
 */

@Database(entities = {profileEntry.class}, version = 1, exportSchema = false)
abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;
    public abstract MyDao myDao();

    public static AppDatabase getAppDatabase(Context context) {
        Log.d("TAG", "AppDatabase accessed");
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "entry_database")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }
    public static void destroyInstance() {
        INSTANCE = null;
    }
}

