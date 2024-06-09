package com.example.redditapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.redditapp.network.RedditApiClient
import com.example.redditapp.ui.theme.RedditAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedditAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        fetchTopPosts()
    }
    private fun fetchTopPosts() {
        CoroutineScope(Dispatchers.IO).launch {
            val topPosts = RedditApiClient.getTopPosts()
            if (topPosts != null) {
                println("TOP POSTS")
                for (post in topPosts) {
                    Log.d("RedditPost", "ID: ${post.id}, Author: ${post.author}, Title: ${post.title}")
                }
                println("TOP POSTS ENDED")
            } else {
                Log.e("RedditPost", "Failed to fetch top posts")
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RedditAppTheme {
        Greeting("Android")
    }
}