package com.example.githubconsumerapp

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


class FollowingFragment : Fragment() {
    companion object {
        private const val ARG_USERNAME = "username"
        val TAG = FollowingFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(username: String): FollowingFragment {
            val fragment = FollowingFragment()
            val bundle = Bundle()
            bundle.putString(ARG_USERNAME, username)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val listFollowing = ArrayList<User>()
    private lateinit var rvFollowing: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userName = arguments?.getString(ARG_USERNAME)
        rvFollowing = view.findViewById(R.id.rv_following)
        rvFollowing.setHasFixedSize(true)
        getDataFromAPI(userName)
        showFollowingList()
    }

    private fun showSelectedUser(user: User) {
        val moveIntent = Intent(activity, DetailActivity::class.java)
        moveIntent.putExtra(DetailActivity.extraUser, user)
        startActivity(moveIntent)
    }

    private fun showFollowingList() {
        rvFollowing.layoutManager = LinearLayoutManager(activity)
        val listAdapter = ListAdapter(listFollowing)
        rvFollowing.adapter = listAdapter

        listAdapter.setOnItemClicked(object : ListAdapter.OnItemClickCallback {
            override fun onItemClicked(data: User) {
                showSelectedUser(data)
            }
        })
    }

    private fun getDataFromAPI(username: String?) {
        progressBar.visibility = View.VISIBLE
        val client = AsyncHttpClient()
        client.addHeader("Authorization", "token f120b4f606b7f3a02636de98323c52055c3d62ea")
        client.addHeader("User-Agent", "request")
        client.get(
            "https://api.github.com/users/$username/following",
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
                            listFollowing.add(user)
                        }
                        showFollowingList()
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