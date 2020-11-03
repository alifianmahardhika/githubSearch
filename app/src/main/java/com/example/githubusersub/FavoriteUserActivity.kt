package com.example.githubusersub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.githubusersub.db.UserHelper
import com.example.githubusersub.helper.MappingHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_favorite_user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FavoriteUserActivity : AppCompatActivity() {
    private lateinit var userHelper: UserHelper
    private lateinit var rvFav: RecyclerView
    private lateinit var listAdapter: ListAdapter
    private var listUser = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_user)

        rvFav = findViewById(R.id.rv_fav)
        rvFav.setHasFixedSize(true)

        userHelper = UserHelper.getHelperInstance(applicationContext)
        userHelper.openDatabase()
        showUserFavorite()
        loadUserAsync()
    }

    private fun showUserFavorite() {
        rvFav.layoutManager = LinearLayoutManager(this)
        listAdapter = ListAdapter(listUser)
        rvFav.adapter = listAdapter

        listAdapter.setOnItemClicked(object : ListAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })
    }

    private fun loadUserAsync() {
        GlobalScope.launch(Dispatchers.Main) {
            fav_progress.visibility = View.VISIBLE
            val deferredUsers = async(Dispatchers.IO) {
                val cursor = userHelper.queryData()
                MappingHelper.mapCursorToArrayList(cursor)
            }
            fav_progress.visibility = View.INVISIBLE
            val users = deferredUsers.await()
            if (users.size > 0){
                listUser = users
                showUserFavorite()
            } else {
                listUser = ArrayList()
                showSnackbarMessage("Tidak ada data")
            }
        }
    }

    private fun showSelectedUser(user: User) {
        val moveIntent = Intent(this, DetailActivity::class.java)
        moveIntent.putExtra(DetailActivity.extraUser, user)
        startActivity(moveIntent)
    }

    private fun showSnackbarMessage(message: String) {
        Snackbar.make(rvFav, message, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        userHelper.closeDatabase()
    }
}