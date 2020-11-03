package com.example.githubusersub.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.githubusersub.db.DatabaseContract.UserColumns.Companion.Table_name

internal class DatabaseHelper (context: Context) : SQLiteOpenHelper(context, db_name, null, db_version) {
    companion object {
        private const val db_name = "dbUserFavorite"
        private const val db_version = 1
        private const val sql_create_table = "CREATE TABLE $Table_name" +
                " (${DatabaseContract.UserColumns.Column_Id} INTEGER PRIMARY KEY AUTOINCREMENT," +
                " ${DatabaseContract.UserColumns.Column_loginName} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.Column_name} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.Column_avatarUrl} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.Column_company} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.Column_followers} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.Column_following} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.Column_repository} TEXT NOT NULL," +
                " ${DatabaseContract.UserColumns.Column_location} TEXT NOT NULL)"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(sql_create_table)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $Table_name")
        onCreate(db)
    }
}