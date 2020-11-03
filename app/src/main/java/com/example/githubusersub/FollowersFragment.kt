package com.example.githubusersub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray


class FollowersFragment : Fragment() {
    companion object {
        private const val ARG_USERNAME = "username"
        val TAG = FollowingFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(username: String): FollowersFragment {
            val fragment = FollowersFragment()
            val bundle = Bundle()
            bundle.putString(ARG_USERNAME, username)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val listFollower = ArrayList<User>()
    private lateinit var rvFollower: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_followers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userName = arguments?.getString(ARG_USERNAME)
        rvFollower = view.findViewById(R.id.rv_followers)
        rvFollower.setHasFixedSize(true)
        getDataFromAPI(userName)
        showFollowerList()
    }

    private fun showSelectedUser(user: User) {
        val moveIntent = Intent(activity, DetailActivity::class.java)
        moveIntent.putExtra(DetailActivity.extraUser, user)
        startActivity(moveIntent)
    }

    private fun showFollowerList() {
        rvFollower.layoutManager = LinearLayoutManager(activity)
        val listAdapter = ListAdapter(listFollower)
        rvFollower.adapter = listAdapter

        listAdapter.setOnItemClicked(object : ListAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })
    }

    private fun getDataFromAPI(username: String?) {
        progressBar.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        client.addHeader("Authorization", BuildConfig.GITHUB_TOKEN)
        client.addHeader("User-Agent", "request")
        client.get(
            "https://api.github.com/users/$username/followers",
            object : AsyncHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    responseBody: ByteArray?
                ) {
                    progressBar.visibility = View.INVISIBLE
                    val result = String(responseBody!!)
                    Log.d(TAG, result)
                    try {
                        val followers = JSONArray(result)
                        for (position in 0 until followers.length()) {
                            val follower = followers.getJSONObject(position)
                            val user = User()
                            user.loginName = follower.getString("login")
                            user.avatarUrl = follower.getString("avatar_url")
                            listFollower.add(user)
                        }
                        showFollowerList()
                    } catch (e: Exception) {
                        println(e.message)
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
                    println(errorMessage)
                }

            })
    }
}