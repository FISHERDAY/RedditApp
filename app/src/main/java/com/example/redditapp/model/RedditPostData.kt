package com.example.redditapp.model

data class RedditPostData(
    val children: List<RedditPostWrapper>,
    val after: String?
)