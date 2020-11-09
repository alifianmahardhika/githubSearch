package com.example.githubconsumerapp

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.githubconsumerapp.db.DatabaseContract
import com.example.githubconsumerapp.db.DatabaseContract.UserColumns.Companion.content_uri
import com.example.githubconsumerapp.helper.MappingHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject

class DetailActivity : AppCompatActivity() {
    companion object {
        const val extraUser = "extra-user"
        private val TAG = DetailActivity::class.java.simpleName
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
    }

    private val position: Int = 0
    private val detail = User()
    private lateinit var uriwithName: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val tvObject: TextView = findViewById(R.id.tv_object_received)
        val userPhoto: ImageView = findViewById(R.id.user_image)
        val fab: FloatingActionButton = findViewById(R.id.fav_btn)

        fun setStatusFab(status: Boolean){
            if (status){
                fab.setImageResource(R.drawable.star_white)
            } else {
                fab.setImageResource(R.drawable.star_white_border)
            }
        }

        fun showSnackBar(message: String){
            Snackbar.make(tvObject, message, Snackbar.LENGTH_SHORT).show()
        }

        fun insertToFavorite(){
            val intent = Intent()
            intent.putExtra(extraUser, detail)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()
            values.put(DatabaseContract.UserColumns.Column_name, detail.name)
            values.put(DatabaseContract.UserColumns.Column_avatarUrl, detail.avatarUrl)
            values.put(DatabaseContract.UserColumns.Column_company, detail.company)
            values.put(DatabaseContract.UserColumns.Column_followers, detail.followers)
            values.put(DatabaseContract.UserColumns.Column_following, detail.following)
            values.put(DatabaseContract.UserColumns.Column_location, detail.location)
            values.put(DatabaseContract.UserColumns.Column_repository, detail.repository)
            values.put(DatabaseContract.UserColumns.Column_loginName, detail.loginName)

            val result = contentResolver.insert(content_uri, values)
            if (result != null) {
                setResult(RESULT_ADD, intent)
                showSnackBar("User Added to Favorite")
            }
        }

        fun removeFromFavorite() {
            uriwithName = Uri.parse("$content_uri/${detail.loginName}")
            val result = contentResolver.delete(uriwithName, null, null)
            if (result > 0) {
                showSnackBar("User Removed from Favorite")
            }
        }

        fun checkUser() {
            GlobalScope.launch(Dispatchers.Main) {
                val deferredUsers = async(Dispatchers.IO) {
                    val cursor = contentResolver?.query(content_uri, null, null, null, null)
                    MappingHelper.mapCursorToArrayList(cursor)
                }
                val users = deferredUsers.await()
                for (i in 0 until users.size){
                    if (users[i].name == detail.name){
                        var statusFab = true
                        setStatusFab(statusFab)
                        fab.setOnClickListener{
                            if (statusFab){
                                removeFromFavorite()
                            } else {
                                insertToFavorite()
                            }
                            statusFab = !statusFab
                            setStatusFab(statusFab)
                        }
                    }
                }
            }
        }
        val title = resources.getString(R.string.detailname)
        supportActionBar?.title = title
        val data = intent.getParcelableExtra<User>(extraUser)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val client = AsyncHttpClient()
        client.addHeader("Authorization", BuildConfig.GITHUB_TOKEN)
        client.addHeader("User-Agent", "request")
        client.get("https://api.github.com/users/${data?.loginName}",
            object : AsyncHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseBody: ByteArray?
                ) {
                    val result = responseBody?.let { String(it) }
                    result?.let { Log.d(TAG, it) }
                    try {
                        if (result != null){
                            val details = JSONObject(result)
                            detail.avatarUrl = details.getString("avatar_url")
                            detail.loginName = details.getString("login")
                            detail.name = details.getString("name")
                            detail.company = details.getString("company")
                            detail.followers = details.getString("followers")
                            detail.following = details.getString("following")
                            detail.location = details.getString("location")
                            detail.repository = details.getString("public_repos")

                            val texts: String = """
                                ${resources.getString(R.string.username)}: ${detail.loginName}
                                ${resources.getString(R.string.name)} : ${detail.name}
                                ${resources.getString(R.string.company)} : ${detail.company}
                                ${resources.getString(R.string.content_tab_followers)} : ${detail.followers}
                                ${resources.getString(R.string.content_tab_following)} : ${detail.following}
                                ${resources.getString(R.string.location)} : ${detail.location}
                                ${resources.getString(R.string.repository)} : ${detail.repository}
                                """.trimIndent()

                            tvObject.text = texts
                            Glide.with(this@DetailActivity)
                                .load(detail.avatarUrl)
                                .apply(RequestOptions().override(100, 100))
                                .into(userPhoto)
                            var statusFab = false
                            setStatusFab(statusFab)
                            checkUser()
                            fab.setOnClickListener{
                                if (statusFab){
                                    removeFromFavorite()
                                } else {
                                    insertToFavorite()
                                }
                                statusFab = !statusFab
                                setStatusFab(statusFab)
                            }
                            sectionsPagerAdapter.userName = detail.loginName
                            view_pager.adapter = sectionsPagerAdapter
                            tabs.setupWithViewPager(view_pager)
                            supportActionBar?.elevation = 0f
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@DetailActivity, e.message, Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseBody: ByteArray?,
                    error: Throwable?
                ) {
                    val errorMessage = when (statusCode) {
                        401 -> "$statusCode : Bad Request"
                        403 -> "$statusCode : Forbidden"
                        404 -> "$statusCode : Not Found"
                        else -> "$statusCode : ${error?.message}"
                    }
                    Toast.makeText(this@DetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }

            })
    }
}