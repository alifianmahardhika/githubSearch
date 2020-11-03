package com.example.githubusersub.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.example.githubusersub.db.DatabaseContract.AUTHORITY
import com.example.githubusersub.db.DatabaseContract.UserColumns.Companion.Table_name
import com.example.githubusersub.db.DatabaseContract.UserColumns.Companion.content_uri
import com.example.githubusersub.db.UserHelper

class GithubSearchProvider : ContentProvider() {

    companion object {
        private const val user = 1
        private const val user_id = 2
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private lateinit var userHelper: UserHelper
        init {
            sUriMatcher.addURI(AUTHORITY, Table_name, user)
            sUriMatcher.addURI(AUTHORITY, "$Table_name/#", user_id)
        }
    }

    override fun onCreate(): Boolean {
        userHelper = UserHelper.getHelperInstance(context as Context)
        userHelper.openDatabase()
        return true
    }
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {

        return when (sUriMatcher.match(uri)){
            user -> userHelper.queryData()
            user_id -> userHelper.queryById(uri.lastPathSegment.toString())
            else -> null
        }
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val added: Long = when (user){
            sUriMatcher.match(uri) -> userHelper.insertData(values)
            else -> 0
        }
        context?.contentResolver?.notifyChange(content_uri, null)

        return Uri.parse("$content_uri/$added")
    }
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val updated: Int = when (user_id) {
            sUriMatcher.match(uri) -> userHelper.updateData(uri.lastPathSegment.toString(),values)
            else -> 0
        }
        context?.contentResolver?.notifyChange(content_uri, null)
        return updated
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val deleted: Int = when (user_id) {
            sUriMatcher.match(uri) -> userHelper.removeByName(uri.lastPathSegment.toString())
            else -> 0
        }
        context?.contentResolver?.notifyChange(content_uri, null)
        return deleted
    }
}
