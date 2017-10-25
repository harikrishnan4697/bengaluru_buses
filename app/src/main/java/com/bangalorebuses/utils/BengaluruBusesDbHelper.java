package com.bangalorebuses.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * This helper class is used to get a readable SQLiteDatabase.
 *
 * @author Nihar Thakkar
 * @version 1.0
 * @since 5-9-2017
 */

public class BengaluruBusesDbHelper extends SQLiteAssetHelper
{
    private static final int DATABASE_VERSION = 126;
    private static final String DATABASE_NAME = "BengaluruBuses.db";

    public BengaluruBusesDbHelper(Context context)
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