package com.example.adopt_pet_app.ui.screen.post

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.adopt_pet_app.ui.component.FullScreenImageViewer
import com.example.adopt_pet_app.ui.components.ImageGalleryPreview
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.auth.FirebaseAuth


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun PostScreen(
    navController: NavController,
    viewModel: PostViewModel = viewModel(),
    onPostSuccess: () -> Unit,
    postIdToEdit: String? = null
) {
    // Form state
    var petName by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var existingImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isEditMode by remember { mutableStateOf(false) }
    var adopted by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedGender by remember { mutableStateOf(false) }

    val animalTypes = listOf("Dog", "Cat", "Bird", "Rabbit")
    val genders = listOf("Male", "Female")

    val isPosting = viewModel.isPosting
    val postSuccess = viewModel.postSuccess
    val postError = viewModel.postError

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val context = LocalContext.current

    LaunchedEffect(postIdToEdit) {
        if (postIdToEdit != null) {
            viewModel.getPostById(postIdToEdit) { post ->
                post?.let {
                    isEditMode = true
                    petName = it.petName
                    type = it.type
                    gender = it.gender
                    age = it.age
                    location = it.location
                    description = it.description
                    existingImageUrls = it.imageUrls
                    adopted = it.adopted
                }
            }
        }
    }

    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        imageUris = imageUris + uris
    }

    val isFormValid by derivedStateOf {
        petName.isNotBlank() &&
                type.isNotBlank() &&
                gender.isNotBlank() &&
                age.isNotBlank() &&
                location.isNotBlank() &&
                description.isNotBlank() &&
                (imageUris.isNotEmpty() || existingImageUrls.isNotEmpty())
    }

    LaunchedEffect(postSuccess) {
        if (postSuccess) onPostSuccess()
    }

    // Full-screen state
    var showFullScreen by remember { mutableStateOf(false) }
    var startPage by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = if (isEditMode) "Edit Post" else "Create Post",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

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
                Button(onClick = { multipleImagePickerLauncher.launch("image/*") }) {
                    Text("Chọn ảnh từ thư viện")
                }
            }

            // Hiển thị ảnh cũ (từ URL)
            item {
                if (existingImageUrls.isNotEmpty()) {
                    Text("Ảnh hiện tại:", style = MaterialTheme.typography.labelMedium)
                    ImageGalleryPreview(
                        imageUris = existingImageUrls.map { Uri.parse(it) },
                        onRemoveImage = { uri ->
                            existingImageUrls = existingImageUrls.filter { it != uri.toString() }
                        },
                        onImageClick = { index ->
                            startPage = index
                            showFullScreen = true
                        }
                    )
                }
            }

            item {
                if (imageUris.isNotEmpty()) {
                    Text("Ảnh mới:", style = MaterialTheme.typography.labelMedium)
                    ImageGalleryPreview(
                        imageUris = imageUris,
                        onRemoveImage = { uri ->
                            imageUris = imageUris.filter { it != uri }
                        },
                        onImageClick = { index ->
                            startPage = existingImageUrls.size + index
                            showFullScreen = true
                        }
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

            // Adoption Status Toggle (chỉ hiển thị khi edit mode)
            if (isEditMode) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (adopted) Color(0xFFE8F5E9) else Color(0xFFFFFFFF)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Adopted",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (adopted) Color(0xFF2E7D32) else Color.Gray
                            )

                            Switch(
                                checked = adopted,
                                onCheckedChange = { adopted = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF4CAF50),
                                    checkedTrackColor = Color(0xFFA5D6A7),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color(0xFFE0E0E0)
                                )
                            )
                        }
                    }
                }
            }

            item {
                if (!isFormValid) {
                    Text(
                        text = "Vui lòng điền đầy đủ thông tin và chọn ít nhất một ảnh",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (postError != null) {
                    Text(text = postError, color = Color.Red)
                }

                Button(
                    onClick = {
                        if (isEditMode && postIdToEdit != null) {
                            // Edit mode
                            if (imageUris.isNotEmpty()) {
                                viewModel.uploadMultipleImagesAndUpdatePost(
                                    context = context,
                                    postId = postIdToEdit,
                                    newImageUris = imageUris,
                                    existingImageUrls = existingImageUrls,
                                    petName = petName,
                                    type = type,
                                    gender = gender,
                                    age = age,
                                    location = location,
                                    description = description,
                                    adopted = adopted
                                )
                            } else {
                                viewModel.updatePost(
                                    postId = postIdToEdit,
                                    petName = petName,
                                    type = type,
                                    gender = gender,
                                    age = age,
                                    imageUrls = existingImageUrls,
                                    location = location,
                                    description = description,
                                    adopted = adopted
                                )
                            }
                        } else {
                            // Create mode - mặc định adopted = false
                            if (imageUris.isNotEmpty()) {
                                viewModel.uploadMultipleImagesAndSubmitPost(
                                    context = context,
                                    imageUris = imageUris,
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
                                    imageUrls = emptyList(),
                                    location = location,
                                    description = description
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPosting && isFormValid
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(if (isEditMode) "Update" else "Post")
                    }
                }
            }
        }

        if (showFullScreen) {
            val allImages = existingImageUrls.map { Uri.parse(it) } + imageUris
            FullScreenImageViewer(
                imageUris = allImages,
                startPage = startPage,
                onClose = { showFullScreen = false }
            )
        }
    }
}