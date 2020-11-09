package com.example.githubusersub

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.githubusersub.db.UserHelper
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var rvUser: RecyclerView
    private val listUser = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val title = resources.getString(R.string.app_name)
        setActionTitle(title)
        rvUser = findViewById(R.id.rv_user)
        rvUser.setHasFixedSize(true)

        showUserList()
    }

    override fun onDestroy() {
        super.onDestroy()
        val userHelper = UserHelper.getHelperInstance(applicationContext)
        userHelper.closeDatabase()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = resources.getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                parseData(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_change_settings) {
            val mIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(mIntent)
        }
        if (item.itemId == R.id.favorite_user_activity){
            val favIntent = Intent(this, FavoriteUserActivity::class.java)
            startActivity(favIntent)
        }
        if (item.itemId == R.id.setting_activity){
            val settingIntent = Intent(this, SettingPreferencesActivity::class.java)
            startActivity(settingIntent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showUserList() {
        rvUser.layoutManager = LinearLayoutManager(this)
        val listAdapter = ListAdapter(listUser)
        rvUser.adapter = listAdapter

        listAdapter.setOnItemClicked(object : ListAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })
    }

    private fun showSelectedUser(user: User) {
        val moveIntent = Intent(this, DetailActivity::class.java)
        moveIntent.putExtra(DetailActivity.extraUser, user)
        startActivity(moveIntent)
    }

    private fun setActionTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun parseData(query: String?) {
        progressBar.visibility = View.VISIBLE
        tv_manual.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        client.addHeader("Authorization", BuildConfig.GITHUB_TOKEN)
        client.addHeader("User-Agent", "request")
        client.get(
            "https://api.github.com/search/users?q=$query",
            object : AsyncHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseBody: ByteArray?
                ) {
                    progressBar.visibility = View.INVISIBLE
                    tv_manual.visibility = View.INVISIBLE
                    val result = String(responseBody!!)
                    Log.d(TAG, result)
                    try {
                        listUser.clear()
                        val responseObject = JSONObject(result)
                        val items = responseObject.getJSONArray("items")
                        for (position in 0 until items.length()) {
                            val item = items.getJSONObject(position)
                            val user = User()
                            user.loginName = item.getString("login")
                            user.avatarUrl = item.getString("avatar_url")
                            listUser.add(user)
                        }
                        showUserList()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseBody: ByteArray?,
                    error: Throwable?
                ) {
                    progressBar.visibility = View.INVISIBLE
                    val errorMessage = when (statusCode) {
                        401 -> "$statusCode : Bad Request"
                        403 -> "$statusCode : Forbidden"
                        404 -> "$statusCode : Not Found"
                        else -> "$statusCode : ${error?.message}"
                    }
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }

            })
    }
}