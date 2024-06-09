package com.example.redditapp.model

data class RedditPost(
    val id: String,
    val author: String,
    val title: String,
    val thumbnail: String,
    val createdUtc: Long,
    val numComments: Int,
    val url: String
)