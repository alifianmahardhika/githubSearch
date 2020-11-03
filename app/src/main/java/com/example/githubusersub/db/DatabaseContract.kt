package com.example.githubusersub.db

import android.provider.BaseColumns

internal class DatabaseContract {
    internal class UserColumns : BaseColumns{
        companion object {
            const val Table_name = "favorite_users"
            const val Column_Id = "id"
            const val Column_avatarUrl = "avatar_url"
            const val Column_loginName = "loginName"
            const val Column_name = "name"
            const val Column_location = "location"
            const val Column_repository = "repo"
            const val Column_company = "company"
            const val Column_followers = "followers"
            const val Column_following = "following"
        }
    }
}