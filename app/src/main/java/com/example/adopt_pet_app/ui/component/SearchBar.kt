package com.example.adopt_pet_app.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "Search for a pet...",
    onSearch: (String) -> Unit = {}
) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }

    TextField(
        value = textState,
        onValueChange = {
            textState = it
            onSearch(it.text)
        },
        placeholder = {
            Text(
                text = hint,
                fontSize = 14.sp
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color.Gray,
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedContainerColor = Color(0xFFF5F5F5)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
    )
}
