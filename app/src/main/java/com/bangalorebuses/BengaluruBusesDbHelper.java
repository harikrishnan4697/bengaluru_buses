package com.bangalorebuses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

class BengaluruBusesDbHelper extends SQLiteAssetHelper
{
    private static final int DATABASE_VERSION = 114;
    private static final String DATABASE_NAME = "BengaluruBuses.db";

    BengaluruBusesDbHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Do nothing
    }
}