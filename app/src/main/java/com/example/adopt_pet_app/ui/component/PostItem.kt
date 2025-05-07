// PostItem.kt
package com.example.adopt_pet_app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.adopt_pet_app.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.adopt_pet_app.data.model.Post

@Composable
fun PostItem(
    post: Post,
    onUserClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onUserClick(post.userId) }
        ) {
            Column {
                Text(text = post.petName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = post.createdAt?.toDate()?.toString() ?: "Unknown date",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            painter = rememberAsyncImagePainter(post.imageUrl),
            contentDescription = "Pet image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onPostClick(post.id) },
            contentScale = ContentScale.Crop
        )
    }
}



