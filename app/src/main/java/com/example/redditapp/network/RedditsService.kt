package com.example.redditapp.network

import com.example.redditapp.model.RedditApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface RedditService {
    @GET("top.json")
    fun getTopPosts(): Call<RedditApiResponse>
}