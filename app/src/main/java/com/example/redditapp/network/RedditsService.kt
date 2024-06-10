package com.example.redditapp.network

import com.example.redditapp.model.RedditApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RedditService {
    @GET("top.json")
    fun getTopPosts(
        @Query("limit") limit: Int,
        @Query("after") after: String? = null
    ): Call<RedditApiResponse>
}