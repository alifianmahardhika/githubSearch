package com.example.githubusersub.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.githubusersub.db.DatabaseContract.UserColumns.Companion.Column_Id
import com.example.githubusersub.db.DatabaseContract.UserColumns.Companion.Column_loginName
import com.example.githubusersub.db.DatabaseContract.UserColumns.Companion.Column_name
import com.example.githubusersub.db.DatabaseContract.UserColumns.Companion.Table_name
import java.sql.SQLException

class UserHelper (context: Context) {
    companion object {
        private const val db_table = Table_name
        private lateinit var db_helper: DatabaseHelper
        private var helper_instance: UserHelper? = null
        private lateinit var database: SQLiteDatabase

        fun getHelperInstance(context: Context): UserHelper =
            helper_instance ?: synchronized(this){
                helper_instance ?: UserHelper(context)
            }
    }

    init {
        db_helper = DatabaseHelper(context)
    }

    @Throws(SQLException::class)
    fun openDatabase(){
        database = db_helper.writableDatabase
    }

    fun closeDatabase(){
        db_helper.close()

        if (database.isOpen){
            database.close()
        }
    }

    fun queryData(): Cursor{
        return database.query(
            db_table,
            null,
            null,
            null,
            null,
            null,
            "$Column_Id ASC"
        )
    }

    fun queryById(id: String): Cursor{
        return database.query(
            db_table,
            null,
            "$Column_name = ?",
            arrayOf(id),
            null,
            null,
            null,
            null
        )
    }

    fun insertData(values: ContentValues?): Long{
        return database.insert(db_table, null, values)
    }

    fun updateData(id: String, values: ContentValues?): Int{
        return database.update(db_table, values, "$Column_Id = ?", arrayOf(id))
    }

    fun removeByLogin(loginName: String): Int{
        return database.delete(db_table, "$Column_loginName = '$loginName'", null)
    }
}