package com.example.redditapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.redditapp.model.RedditPost
import com.example.redditapp.network.RedditApiClient
import com.example.redditapp.ui.theme.RedditAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedditAppTheme {
                var posts by rememberSaveable { mutableStateOf<List<RedditPost>>(emptyList()) }
                var currentPage by rememberSaveable { mutableStateOf(1) }
                var nextAfter by rememberSaveable { mutableStateOf<String?>(null) }
                val coroutineScope = rememberCoroutineScope()

                LaunchedEffect(currentPage) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val limit = 5
                        val (newPosts, newAfter) = RedditApiClient.getTopPosts(limit, nextAfter)
                        if (newPosts != null) {
                            posts = newPosts
                            nextAfter = newAfter
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(posts) { post ->
                            DisplayPost(post = post)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                if (currentPage > 1) {
                                    currentPage -= 1
                                    nextAfter = null // Сбрасываем пагинацию
                                }
                            },
                            enabled = currentPage > 1
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = {
                                currentPage += 1
                            }
                        ) {
                            Text("Next")
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

        if (post.thumbnail.isNotEmpty() && post.thumbnail != "self" && post.thumbnail != "default" &&
            post.thumbnail.endsWith(".jpg")
        ) {
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