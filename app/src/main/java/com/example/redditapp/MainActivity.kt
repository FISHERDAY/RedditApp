package com.example.redditapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImagePainter
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
                var currentPage by rememberSaveable { mutableIntStateOf(1) }
                var nextAfter by rememberSaveable { mutableStateOf<String?>(null) }
                var afterList by rememberSaveable { mutableStateOf(mutableListOf<String?>()) }
                val coroutineScope = rememberCoroutineScope()
                val lazyListState = rememberLazyListState()


                LaunchedEffect(currentPage) {
                    coroutineScope.launch(Dispatchers.IO) {
                        val limit = 5
                        val (newPosts, newAfter) = RedditApiClient.getTopPosts(limit, nextAfter)
                        if (newPosts != null) {
                            posts = newPosts
                            if (currentPage > afterList.size) {
                                afterList.add(nextAfter)
                            }
                            nextAfter = newAfter
                            coroutineScope.launch {
                                lazyListState.scrollToItem(0)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = lazyListState,
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
                                    println("CURRENT PAGE:$currentPage")
                                    nextAfter = afterList[currentPage - 1]
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
    var isMenuVisible by remember { mutableStateOf(false) }

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

        val context = LocalContext.current

        val painter = rememberAsyncImagePainter(model = post.thumbnail)
        val imageModifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .clickable {
                isMenuVisible = !isMenuVisible
            }
        if (post.thumbnail.isNotEmpty() && post.thumbnail != "self" && post.thumbnail != "default" &&
            post.thumbnail.endsWith(".jpg")
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = imageModifier
            )

            DropdownMenu(
                expanded = isMenuVisible,
                onDismissRequest = { isMenuVisible = false },
                modifier = Modifier.background(Color.White)
            ) {
                DropdownMenuItem(text = {
                    Text("Save Image")
                }, onClick = {
                    val imageState = painter.state
                    if (imageState is AsyncImagePainter.State.Success) {
                        var bitmap = imageState.result.drawable.toBitmap()
                        saveBitmapToGallery(context, bitmap)
                    } else {
                        Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                    isMenuVisible = false
                })
                DropdownMenuItem(text = {
                    Text("Open URL")
                }, onClick = {
                    val url = post.thumbnail
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                    isMenuVisible = false
                })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Comments: ${post.numComments}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    val fileName = "${System.currentTimeMillis()}.jpg"
    val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }
    val imageUri = context.contentResolver.insert(imageCollection, contentValues)
    imageUri?.let { uri ->
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
        }
        MediaScannerConnection.scanFile(
            context,
            arrayOf(uri.path),
            arrayOf("image/jpeg"),
            null
        )
        Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
    } ?: run {
        Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
    }
}

fun Long.hoursAgo(): Long {
    val currentTime = System.currentTimeMillis() / 1000
    return (currentTime - this) / 3600
}