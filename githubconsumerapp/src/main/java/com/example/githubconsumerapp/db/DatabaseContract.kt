package com.example.githubconsumerapp.db

import android.net.Uri
import android.provider.BaseColumns

object DatabaseContract {
    const val AUTHORITY = "com.example.githubusersub"
    const val SCHEME = "content"
    class UserColumns : BaseColumns{
        companion object {
            const val Table_name = "favorite_users"
            const val Column_avatarUrl = "avatar_url"
            const val Column_loginName = "loginName"
            const val Column_name = "name"
            const val Column_location = "location"
            const val Column_repository = "repo"
            const val Column_company = "company"
            const val Column_followers = "followers"
            const val Column_following = "following"

            val content_uri: Uri = Uri.Builder().scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(Table_name)
                .build()
        }
    }
}