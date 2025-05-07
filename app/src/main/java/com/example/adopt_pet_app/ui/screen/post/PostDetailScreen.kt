package com.example.adopt_pet_app.ui.screen.post

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.adopt_pet_app.data.model.Post
import com.example.adopt_pet_app.ui.component.InfoBadge

@Composable
fun PostDetailScreen(postId: String, postViewModel: PostViewModel = viewModel()) {
    var post by remember { mutableStateOf<Post?>(null) }

    LaunchedEffect(postId) {
        postViewModel.getPostById(postId) {
            post = it
        }
    }

    post?.let { postData ->
        Column(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter(postData.imageUrl),
                contentDescription = "Pet image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(postData.petName, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text(postData.type, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    InfoBadge(
                        text = "♂ ${postData.gender}",
                        modifier = Modifier.defaultMinSize(minWidth = 80.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    InfoBadge(
                        text = "${postData.age}",
                        modifier = Modifier.defaultMinSize(minWidth = 80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Description", fontWeight = FontWeight.SemiBold)
                Text(postData.description)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { /* TODO: adopt action */ },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text("Adopt Now")
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun InfoBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF9A825))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}
