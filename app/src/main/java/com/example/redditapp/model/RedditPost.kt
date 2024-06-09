package com.example.redditapp.model

import com.google.gson.annotations.SerializedName

data class RedditPost(
    val id: String,
    @SerializedName("subreddit_name_prefixed")
    val author: String,
    val title: String,
    val thumbnail: String,
    @SerializedName("created_utc")
    val createdUtc: Long,
    @SerializedName("num_comments")
    val numComments: Int,
    val url: String
)