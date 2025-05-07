//PostScreen.kt
package com.example.adopt_pet_app.ui.screen.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    viewModel: PostViewModel = viewModel(),
    onPostSuccess: () -> Unit
) {
    var petName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var expandedType by remember { mutableStateOf(false) }
    var expandedGender by remember { mutableStateOf(false) }

    val animalTypes = listOf("Dog", "Cat", "Bird", "Rabbit")
    val genders = listOf("Male", "Female")

    val isPosting = viewModel.isPosting
    val postSuccess = viewModel.postSuccess
    val postError = viewModel.postError
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(postSuccess) {
        if (postSuccess) onPostSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Pet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        animalTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    type = option
                                    expandedType = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expandedGender,
                    onExpandedChange = { expandedGender = !expandedGender }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedGender,
                        onDismissRequest = { expandedGender = false }
                    ) {
                        genders.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    expandedGender = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    placeholder = { Text("e.g. 2 years or 6 months") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Chọn ảnh từ thư viện")
                }
            }

            item {
                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(8.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }

            item {
                if (postError != null) {
                    Text(text = postError, color = Color.Red)
                }

                Button(
                    onClick = {
                        if (imageUri != null) {
                            viewModel.uploadImageAndSubmitPost(
                                context = context,
                                imageUri = imageUri!!,
                                userId = userId,
                                petName = petName,
                                type = type,
                                gender = gender,
                                age = age,
                                location = location,
                                description = description
                            )
                        } else {
                            viewModel.submitPost(
                                userId = userId,
                                petName = petName,
                                type = type,
                                gender = gender,
                                age = age,
                                imageUrl = "", // Hoặc xử lý logic nếu ảnh bắt buộc
                                location = location,
                                description = description
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Post")
                }

            }
        }
    }
}

