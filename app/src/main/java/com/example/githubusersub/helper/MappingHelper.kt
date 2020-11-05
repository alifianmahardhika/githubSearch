package com.example.githubusersub.helper

import android.database.Cursor
import com.example.githubusersub.User
import com.example.githubusersub.db.DatabaseContract

object MappingHelper {
    fun mapCursorToArrayList(userCursor: Cursor?): ArrayList<User> {
        val userList = ArrayList<User>()
        userCursor?.apply {
            while (moveToNext()) {
                val avatar = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.Column_avatarUrl))
                val loginName = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.Column_loginName))
                val name = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.Column_name))
                userList.add(User(avatar, loginName, name))
            }
        }
        return userList
    }

    fun mapCursorToObject(userCursor: Cursor?): User{
        var user = User()
        userCursor?.apply {
            moveToFirst()
            val avatar = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.Column_avatarUrl))
            val loginName = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.Column_loginName))
            val name = getString(getColumnIndexOrThrow(DatabaseContract.UserColumns.Column_name))
            user = User(avatar, loginName, name)
        }
        return user
    }
}