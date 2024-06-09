package com.example.redditapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.redditapp.model.RedditPost
import com.example.redditapp.network.RedditApiClient
import com.example.redditapp.ui.theme.RedditAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedditAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val posts = remember { mutableStateOf<List<RedditPost>>(emptyList()) }
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val topPosts = RedditApiClient.getTopPosts()
                            if (topPosts != null) {
                                posts.value = topPosts.take(10) // taking first posts
                            }
                        }
                    }

                    LazyColumn(
                        contentPadding = innerPadding,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(posts.value) { post ->
                            DisplayPost(post = post)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayPost(post: RedditPost) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = "Author: ${post.author}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text = "Posted: ${post.createdUtc.hoursAgo()} hours ago",
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (post.thumbnail.isNotEmpty() && post.thumbnail != "self" && post.thumbnail != "default") {
            println("AUTHOR:" + post.author + ", THUMBNAIL:" + post.thumbnail)
            Image(
                painter = rememberAsyncImagePainter(model = post.thumbnail),
                contentDescription = null,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Comments: ${post.numComments}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

fun Long.hoursAgo(): Long {
    val currentTime = System.currentTimeMillis() / 1000
    return (currentTime - this) / 3600
}