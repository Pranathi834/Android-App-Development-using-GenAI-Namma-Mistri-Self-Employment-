package com.example.nammamistri.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.nammamistri.ui.theme.BgLight
import com.example.nammamistri.ui.theme.BorderGray
import com.example.nammamistri.ui.theme.OrangePrimary
import com.example.nammamistri.ui.theme.OrangeSelected
import com.example.nammamistri.ui.theme.TextDark
import com.example.nammamistri.ui.theme.TextGray

@Composable
fun PhotosScreen() {
    val context = LocalContext.current
    val isKannada = com.example.nammamistri.LocalIsKannada.current
    val categoriesEn = listOf("All", "Foundation", "Wall", "Roof", "Flooring", "Finishing")
    val categoriesKn = listOf("ಎಲ್ಲ", "ಅಡಿಪಾಯ", "ಗೋಡೆ", "ಮಾಡು", "ನೆಲ", "ಮುಗಿಸುವಿಕೆ")
    val categories = categoriesEn  // internal keys always English
    var selectedCategory by remember { mutableStateOf("All") }
    var showCategoryPicker by remember { mutableStateOf(false) }

    val savedPhotos = DataStore.loadPhotos(context)
    var photosByCategory by remember {
        mutableStateOf(
            savedPhotos.mapValues { (_, uriStrings) ->
                uriStrings.map { Uri.parse(it) }
            }.toMutableMap() as Map<String, List<Uri>>
        )
    }

    var photoToDelete by remember { mutableStateOf<Pair<String, Uri>?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val targetCategory = if (selectedCategory == "All") "Foundation" else selectedCategory
            val updated = photosByCategory.toMutableMap()
            val list = (updated[targetCategory] ?: emptyList()).toMutableList()
            list.addAll(uris)
            updated[targetCategory] = list
            photosByCategory = updated
            DataStore.savePhotos(
                context,
                updated.mapValues { (_, uriList) -> uriList.map { it.toString() } }
            )
        }
    }

    val displayPhotos = if (selectedCategory == "All") {
        photosByCategory.values.flatten()
    } else {
        photosByCategory[selectedCategory] ?: emptyList()
    }

    val totalCount = photosByCategory.values.sumOf { it.size }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    val count = if (category == "All") totalCount
                    else (photosByCategory[category]?.size ?: 0)
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) OrangePrimary else Color.White)
                            .border(
                                1.dp,
                                if (isSelected) OrangePrimary else BorderGray,
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${if (isKannada) categoriesKn[categoriesEn.indexOf(category)] else category} ($count)",
                            color = if (isSelected) Color.White else TextGray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (displayPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(BorderGray.copy(alpha = 0.4f))
                                .clickable { showCategoryPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Photo",
                                tint = TextGray,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (isKannada) "ಇನ್ನೂ ಫೋಟೋಗಳಿಲ್ಲ" else "No photos yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (isKannada) "+ ಒತ್ತಿ ಸೈಟ್ ಫೋಟೋ ಸೇರಿಸಿ" else "Tap + to add site progress photos",
                            fontSize = 13.sp,
                            color = TextGray
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(displayPhotos) { uri ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { photoToDelete = selectedCategory to uri },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showCategoryPicker = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = OrangePrimary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Photo",
                tint = Color.White
            )
        }
    }

    // Category Picker Dialog
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = {
                Text(
                    if (isKannada) "ವಿಭಾಗ ಆಯ್ಕೆ ಮಾಡಿ" else "Select Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    listOf(
                        Triple("Foundation", if (isKannada) "ಅಡಿಪಾಯ" else "Foundation", "🏗️"),
                        Triple("Wall",       if (isKannada) "ಗೋಡೆ"    else "Wall",       "🧱"),
                        Triple("Roof",       if (isKannada) "ಮಾಡು"    else "Roof",       "🏠"),
                        Triple("Flooring",   if (isKannada) "ನೆಲ"     else "Flooring",   "🪨"),
                        Triple("Finishing",  if (isKannada) "ಮುಗಿಸುವಿಕೆ" else "Finishing","🎨")
                    ).forEach { (catKey, catLabel, emoji) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedCategory = catKey
                                    showCategoryPicker = false
                                    launcher.launch("image/*")
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCategory == catKey) OrangeSelected else BgLight
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(emoji, fontSize = 20.sp)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    catLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedCategory == catKey) OrangePrimary else TextDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text(if (isKannada) "ರದ್ದು" else "CANCEL", color = TextGray)
                }
            }
        )
    }

    // Delete confirmation dialog
    if (photoToDelete != null) {
        val (category, uri) = photoToDelete!!
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text(if (isKannada) "ಫೋಟೋ ಅಳಿಸಿ" else "Delete Photo", fontWeight = FontWeight.Bold) },
            text = { Text("Remove this photo from $category?") },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = photosByCategory.toMutableMap()
                        val catKey = if (selectedCategory == "All") {
                            photosByCategory.entries
                                .find { it.value.contains(uri) }?.key ?: category
                        } else category
                        updated[catKey] = (updated[catKey] ?: emptyList())
                            .filter { it != uri }
                        photosByCategory = updated
                        DataStore.savePhotos(
                            context,
                            updated.mapValues { (_, uriList) ->
                                uriList.map { it.toString() }
                            }
                        )
                        photoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("DELETE", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text(if (isKannada) "ರದ್ದು" else "CANCEL", color = TextGray)
                }
            }
        )
    }
}