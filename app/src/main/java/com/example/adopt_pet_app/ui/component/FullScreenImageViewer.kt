package com.example.adopt_pet_app.ui.component

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FullScreenImageViewer(
    imageUris: List<Uri>,
    startPage: Int = 0,
    onClose: () -> Unit
) {
    if (imageUris.isEmpty()) return

    val loopedList = remember { List(1000) { imageUris }.flatten() }

    val base = imageUris.size * 500 + startPage

    val pagerState = rememberPagerState(initialPage = base)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {

        HorizontalPager(
            count = loopedList.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->

            val realIndex = page % imageUris.size

            Image(
                painter = rememberAsyncImagePainter(imageUris[realIndex]),
                contentDescription = "Full Image $realIndex",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Nút đóng
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(35.dp)
            )
        }

        if (imageUris.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                val realIndex = (pagerState.currentPage % imageUris.size) + 1
                Text(
                    text = "$realIndex/${imageUris.size}",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}
