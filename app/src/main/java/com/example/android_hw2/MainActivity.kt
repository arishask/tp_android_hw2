package com.example.android_hw2


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.android.identity.util.UUID
import android.content.res.Configuration
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest


const val API = "https://api.giphy.com/v1/gifs/"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GiphyApp()
            }
        }
    }
}

sealed class GifState {
    data class Loaded(val url: String) : GifState()
    data class Failed(val id: String = UUID.randomUUID().toString()) : GifState()
}

@Composable
fun ErrorPlaceholder(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6B4EE6),
                        Color(0xFF9575CD)
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Не удалось загрузить GIF",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Нажмите, чтобы повторить",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GiphyApp() {
    var items by rememberSaveable { mutableStateOf<List<GifState>>(emptyList()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun fetchRandomGif() {
        scope.launch {
            isLoading = true
            try {
                val gifUrl = RetrofitController(API)
                    .fetchGifFromApi()
                items = listOf(GifState.Loaded(gifUrl)) + items
            } catch (e: Exception) {
                items = listOf(GifState.Failed()) + items
            }
            isLoading = false
        }
    }

    fun retryLoadingGif(failedItem: GifState.Failed) {
        scope.launch {
            try {
                val gifUrl = RetrofitController(API)
                    .fetchGifFromApi()
                items = items.map {
                    if (it is GifState.Failed && it.id == failedItem.id) {
                        GifState.Loaded(gifUrl)
                    } else {
                        it
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = { fetchRandomGif() },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Get Random Gif")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        LazyVerticalGrid(
            columns = if (isLandscape) GridCells.Fixed(2) else GridCells.Fixed(1),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (item) {
                        is GifState.Loaded -> {
                            val painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(context)
                                    .data(item.url)
                                    .crossfade(true)
                                    .build()
                            )

                            Image(
                                painter = painter,
                                contentDescription = "Random GIF",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            when (painter.state) {
                                is AsyncImagePainter.State.Loading -> {
                                    CircularProgressIndicator()
                                }
                                else -> {}
                            }
                        }
                        is GifState.Failed -> {
                            ErrorPlaceholder(onClick = { retryLoadingGif(item) })
                        }
                    }
                }
            }
        }
    }
}