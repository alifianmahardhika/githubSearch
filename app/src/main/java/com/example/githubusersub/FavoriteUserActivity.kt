package com.example.githubusersub

import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.githubusersub.db.DatabaseContract.UserColumns.Companion.content_uri
import com.example.githubusersub.db.UserHelper
import com.example.githubusersub.helper.MappingHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_favorite_user.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FavoriteUserActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_STATE = "EXTRA_STATE"
    }

    private lateinit var userHelper: UserHelper
    private lateinit var rvFav: RecyclerView
    private lateinit var listAdapter: ListAdapter
    private var listUser = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_user)

        rvFav = findViewById(R.id.rv_fav)
        rvFav.setHasFixedSize(true)

        showUserFavorite(listUser)

        val handlerThread = HandlerThread("DataObserver")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)

        val myObserver = object : ContentObserver(handler){
            override fun onChange(selfChange: Boolean) {
                loadUserAsync()
            }
        }

        contentResolver.registerContentObserver(content_uri, true, myObserver)
        if (savedInstanceState == null){
            loadUserAsync()
        } else {
            val list = savedInstanceState.getParcelableArrayList<User>(EXTRA_STATE)
            if (list != null) {
                showUserFavorite(list)
            }
        }
    }

    private fun showUserFavorite(users: ArrayList<User>) {
        rvFav.layoutManager = LinearLayoutManager(this)
        listAdapter = ListAdapter(users)
        rvFav.adapter = listAdapter

        listAdapter.setOnItemClicked(object : ListAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })
    }

    private fun loadUserAsync() {
        userHelper = UserHelper.getHelperInstance(applicationContext)
        userHelper.openDatabase()
        GlobalScope.launch(Dispatchers.Main) {
            fav_progress.visibility = View.VISIBLE
            val deferredUsers = async(Dispatchers.IO) {
                val cursor = contentResolver?.query(content_uri,null,null,null,null)
                MappingHelper.mapCursorToArrayList(cursor)
            }
            fav_progress.visibility = View.INVISIBLE
            val users = deferredUsers.await()
            if (users.size > 0){
                listUser = users
                showUserFavorite(listUser)
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