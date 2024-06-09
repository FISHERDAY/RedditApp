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

    fun getTopPosts(): List<RedditPost>? {
        val call = redditService.getTopPosts()
        val response =
            call.execute()  // Це виконується синхронно, в реальному додатку краще використовувати асинхронний метод

        return if (response.isSuccessful) {
            val redditApiResponse = response.body()
            redditApiResponse?.data?.children?.map { it.data }
        } else {
            null
        }
    }
}