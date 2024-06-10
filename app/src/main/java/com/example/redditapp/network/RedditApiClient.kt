package com.example.redditapp.network

import com.example.redditapp.model.RedditPost
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RedditApiClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.reddit.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val redditService: RedditService = retrofit.create(RedditService::class.java)

    fun getTopPosts(limit: Int, after: String? = null): Pair<List<RedditPost>?, String?> {
        val call = redditService.getTopPosts(limit, after)
        val response = call.execute()

        return if (response.isSuccessful) {
            val redditApiResponse = response.body()
            val posts = redditApiResponse?.data?.children?.map { it.data }
            val nextAfter = redditApiResponse?.data?.after
            Pair(posts, nextAfter)
        } else {
            Pair(null, null)
        }
    }
}