package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.models.ZineCell
import com.example.viewmodel.ZineViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: ZineViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFlipbook: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val title by viewModel.zineTitle.collectAsState()
    val description by viewModel.zineDescription.collectAsState()
    val themeColorHex by viewModel.zineThemeColor.collectAsState()
    val isPublic by viewModel.isZinePublic.collectAsState()
    val cells by viewModel.editorCells.collectAsState()
    val selectedCellIndex by viewModel.selectedCellIndex.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var showMetadataDialog by remember { mutableStateOf(false) }

    // Standard media picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val cellIndex = selectedCellIndex ?: return@rememberLauncherForActivityResult
            val active = cells[cellIndex]
            viewModel.updateSelectedCell(
                imagePath = uri.toString(),
                textOverlay = active.textOverlay,
                backgroundColorHex = active.backgroundColorHex,
                textColorHex = active.textColorHex,
                fontSizeSp = active.fontSizeSp,
                imageZoom = active.imageZoom,
                focalX = active.focalX,
                focalY = active.focalY,
                isFlipped = active.isFlipped
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF9E5)
                ),
                title = {
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF1A1A2E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Unfolded Blueprint",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF3B0C3),
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.saveCurrentZine {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Save and Back",
                            tint = Color(0xFF1A1A2E)
                        )
                    }
                },
                actions = {
                    // Edit Metadata Button
                    IconButton(onClick = { showMetadataDialog = true }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Zine Properties",
                            tint = Color(0xFF1A1A2E)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Action Panel - Fold! and Physical A4 Print Layout compilation
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp),
                color = Color(0xFF1A1A2E),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Export Physical Folding Blueprint (PDF)
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                compileHighResolutionA4Pdf(context, title, cells)
                            }
                        },
                        modifier = Modifier
                            .testTag("print_blueprint")
                            .weight(1f)
                            .height(52.dp)
                            .border(2.dp, Color(0xFFFFF9E5), RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFFFFF9E5)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "PRINT A4 PDF",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    // Fold! Button
                    Button(
                        onClick = {
                            viewModel.saveCurrentZine {
                                val currentZineId = viewModel.editedZineId.value
                                if (currentZineId != null) {
                                    onNavigateToFlipbook(currentZineId)
                                }
                            }
                        },
                        modifier = Modifier
                            .testTag("fold_zine")
                            .weight(1f)
                            .height(52.dp)
                            .border(2.dp, Color(0xFF1A1A2E), RoundedCornerShape(16.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3B0C3),
                            contentColor = Color(0xFF1A1A2E)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.CropFree, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "FOLD BOOK!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF9E5))
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECE5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF1A1A2E), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFFFF9F1C))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Top pages fold upside down relative to bottom ones. The blueprint accounts for this rotation automatically!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A2E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // THE 4X2 GRID MATRIX - PAPER BLUEPRINT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A1A2E))
            ) {
                // ROW 0: Page 4, Page 3, Page 2, Page 1 (Flipped 180° CSS rotation)
                Row(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    for (i in 0..3) {
                        val cell = cells[i]
                        val bgColor = Color(android.graphics.Color.parseColor(cell.backgroundColorHex))
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(bgColor)
                                .border(1.dp, Color(0xFF1A1A2E).copy(0.3f))
                                .clickable { viewModel.selectCell(i) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationZ = 180f }, // 180 Rotation Rule!
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                MiniCellContent(cell)
                            }
                        }
                    }
                }

                // THE PHYSICAL PAPER CUT-LINE & SCISSORS INDICATOR (✂ - - - - - - - - - )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(Color(0xFFFFF9E5)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✂ - - - - - - FOLD-CUT BLUEPRINT - - - - - - ✂",
                            color = Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // ROW 1: Page 5, Page 6, Back, Cover (Normal 0° rotation)
                Row(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                    for (i in 4..7) {
                        val cell = cells[i]
                        val bgColor = Color(android.graphics.Color.parseColor(cell.backgroundColorHex))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(bgColor)
                                .border(1.dp, Color(0xFF1A1A2E).copy(0.3f))
                                .clickable { viewModel.selectCell(i) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                MiniCellContent(cell)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CELL EDITOR PANEL - EXPOSED DIRECTLY UNDER GRID SECURELY!
            if (selectedCellIndex != null) {
                val index = selectedCellIndex!!
                val cell = cells[index]

                // Brutalist dual layer panel card wrapper
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, end = 8.dp)
                ) {
                    // Shadow
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 8.dp, y = 8.dp)
                            .background(Color(0xFF1A1A2E), RoundedCornerShape(24.dp))
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Editing ${cell.pageName.uppercase()}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color(0xFF1A1A2E)
                                )
                                Text(
                                    "Location in physical fold: Cell #${cell.positionIndex + 1}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1A1A2E).copy(0.6f)
                                )
                            }
                            IconButton(onClick = { viewModel.selectCell(null) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close Panel", tint = Color(0xFF1A1A2E))
                            }
                        }

                        Divider(color = Color(0xFF1A1A2E).copy(0.15f), modifier = Modifier.padding(vertical = 12.dp))

                        // BACKGROUND Palette Picker
                        Text("PAGE CANVAS TONE", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewModel.colorPalette.forEach { hexColor ->
                                val rgb = Color(android.graphics.Color.parseColor(hexColor))
                                Box(
                                    modifier = Modifier
                                        .testTag("color_bubble_$hexColor")
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(rgb)
                                        .border(
                                            width = if (cell.backgroundColorHex == hexColor) 3.dp else 1.dp,
                                            color = if (cell.backgroundColorHex == hexColor) Color(0xFF1A1A2E) else Color(0xFF1A1A2E).copy(0.2f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            viewModel.updateSelectedCell(
                                                imagePath = cell.imagePath,
                                                textOverlay = cell.textOverlay,
                                                backgroundColorHex = hexColor,
                                                textColorHex = if (hexColor == "#1A1A2E") "#FFF9E5" else "#1A1A2E",
                                                fontSizeSp = cell.fontSizeSp,
                                                imageZoom = cell.imageZoom,
                                                focalX = cell.focalX,
                                                focalY = cell.focalY,
                                                isFlipped = cell.isFlipped
                                            )
                                        }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // TEXT OVERLAY
                        Text("PAGE TEXT OVERLAY", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = cell.textOverlay,
                            onValueChange = { newTxt ->
                                viewModel.updateSelectedCell(
                                    imagePath = cell.imagePath,
                                    textOverlay = newTxt,
                                    backgroundColorHex = cell.backgroundColorHex,
                                    textColorHex = cell.textColorHex,
                                    fontSizeSp = cell.fontSizeSp,
                                    imageZoom = cell.imageZoom,
                                    focalX = cell.focalX,
                                    focalY = cell.focalY,
                                    isFlipped = cell.isFlipped
                                )
                            },
                            placeholder = { Text("E.g. Sunrise hiking hike...") },
                            modifier = Modifier
                                .testTag("overlay_text_field")
                                .fillMaxWidth()
                                .border(2.dp, Color(0xFF1A1A2E), RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // MEDIA UPLOADS & PRESETS
                        Text("CHOOSE PHOTO", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Camera roll picker button
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A1A2E),
                                    contentColor = Color(0xFFFFF9E5)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .testTag("pick_camera_roll")
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CAMERA ROLL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Remove Image
                            if (cell.imagePath != null) {
                                Button(
                                    onClick = {
                                        viewModel.updateSelectedCell(
                                            imagePath = null,
                                            textOverlay = cell.textOverlay,
                                            backgroundColorHex = cell.backgroundColorHex,
                                            textColorHex = cell.textColorHex,
                                            fontSizeSp = cell.fontSizeSp,
                                            imageZoom = 1.0f,
                                            focalX = 0.5f,
                                            focalY = 0.5f,
                                            isFlipped = cell.isFlipped
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFF9F1C),
                                        contentColor = Color(0xFF1A1A2E)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CLEAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Presets Row
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("OR CHOOSE AESTHETIC PRESET MOMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E).copy(0.6f))
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("preset_snapshots")
                        ) {
                            items(viewModel.photoPresets) { preset ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(72.dp)
                                        .clickable {
                                            viewModel.updateSelectedCell(
                                                imagePath = preset.imageUrl,
                                                textOverlay = cell.textOverlay,
                                                backgroundColorHex = cell.backgroundColorHex,
                                                textColorHex = cell.textColorHex,
                                                fontSizeSp = cell.fontSizeSp,
                                                imageZoom = cell.imageZoom,
                                                focalX = cell.focalX,
                                                focalY = cell.focalY,
                                                isFlipped = cell.isFlipped
                                            )
                                        }
                                ) {
                                    AsyncImage(
                                        model = preset.imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFF1A1A2E))
                                    )
                                    Text(
                                        preset.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color(0xFF1A1A2E),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }

                        if (cell.imagePath != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            // SMART FRAME CROP SLIDERS
                            Text("SMART FRAMING POSITION", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A2E))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Zoom
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Zoom: ${(cell.imageZoom * 100).toInt()}%", fontSize = 11.sp, color = Color(0xFF1A1A2E), modifier = Modifier.width(84.dp))
                                Slider(
                                    value = cell.imageZoom,
                                    onValueChange = { z ->
                                        viewModel.updateSelectedCell(
                                            imagePath = cell.imagePath,
                                            textOverlay = cell.textOverlay,
                                            backgroundColorHex = cell.backgroundColorHex,
                                            textColorHex = cell.textColorHex,
                                            fontSizeSp = cell.fontSizeSp,
                                            imageZoom = z,
                                            focalX = cell.focalX,
                                            focalY = cell.focalY,
                                            isFlipped = cell.isFlipped
                                        )
                                    },
                                    valueRange = 1.0f..3.0f,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Align focal point X
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Center X", fontSize = 11.sp, color = Color(0xFF1A1A2E), modifier = Modifier.width(84.dp))
                                Slider(
                                    value = cell.focalX,
                                    onValueChange = { fx ->
                                        viewModel.updateSelectedCell(
                                            imagePath = cell.imagePath,
                                            textOverlay = cell.textOverlay,
                                            backgroundColorHex = cell.backgroundColorHex,
                                            textColorHex = cell.textColorHex,
                                            fontSizeSp = cell.fontSizeSp,
                                            imageZoom = cell.imageZoom,
                                            focalX = fx,
                                            focalY = cell.focalY,
                                            isFlipped = cell.isFlipped
                                        )
                                    },
                                    valueRange = 0.0f..1.0f,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                // Friendly tips for selecting cell
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFF1A1A2E).copy(0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap any cell inside the paper matrix above to load pictures or write text loops.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E).copy(0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Properties metadata dialog
    if (showMetadataDialog) {
        var tempTitle by remember { mutableStateOf(title) }
        var tempDescription by remember { mutableStateOf(description) }
        var tempThemeHex by remember { mutableStateOf(themeColorHex) }
        var tempPublic by remember { mutableStateOf(isPublic) }

        AlertDialog(
            onDismissRequest = { showMetadataDialog = false },
            title = {
                Text("Zine Library Properties", fontWeight = FontWeight.Black, color = Color(0xFF1A1A2E))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempTitle,
                        onValueChange = { tempTitle = it },
                        label = { Text("Zine Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tempDescription,
                        onValueChange = { tempDescription = it },
                        label = { Text("Short Story / Event Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Shelf Spine Color Accent", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("#F3B0C3", "#FFE680", "#2EC4B6", "#E0B1CB", "#CBF3F0").forEach { rawHex ->
                            val parsed = Color(android.graphics.Color.parseColor(rawHex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(
                                        width = if (tempThemeHex == rawHex) 3.dp else 1.dp,
                                        color = Color(0xFF1A1A2E),
                                        shape = CircleShape
                                    )
                                    .clickable { tempThemeHex = rawHex }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Discovery Board Hook", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Allow pinning this to the global feed", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = tempPublic,
                            onCheckedChange = { tempPublic = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateZineMetaData(tempTitle, tempDescription, tempThemeHex, tempPublic)
                        showMetadataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3B0C3), contentColor = Color(0xFF1A1A2E))
                ) {
                    Text("Apply Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showMetadataDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black)
                ) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFFFFF9E5),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(24.dp))
        )
    }
}

@Composable
fun MiniCellContent(cell: ZineCell) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (cell.imagePath != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(cell.imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cell.imageZoom
                        scaleY = cell.imageZoom
                    }
            )
        }

        // Overlay text if specified
        if (cell.textOverlay.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.35f))
                    .padding(2.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = cell.textOverlay,
                    color = Color.White,
                    fontSize = 9.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Top Left Label showing Page description
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0xFF1A1A2E), RoundedCornerShape(bottomEnd = 8.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                cell.pageName,
                color = Color(0xFFFFF9E5),
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

// ==========================================
// HIGH-FIDELITY PHYSICAL A4 PDF COMPILER
// ==========================================
private suspend fun compileHighResolutionA4Pdf(
    context: Context,
    title: String,
    cells: List<ZineCell>
) {
    withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            
            // Standard A4 dimensions in PostScript points: 595 x 842 pt
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Paints setup
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                color = android.graphics.Color.parseColor("#1A1A2E")
                style = Paint.Style.FILL
                textAlign = Paint.Align.CENTER
            }

            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 18f
                color = android.graphics.Color.parseColor("#1A1A2E")
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val backgroundPaint = Paint().apply {
                style = Paint.Style.FILL
            }

            val linePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#1A1A2E")
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }

            val dashedPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#F3B0C3")
                strokeWidth = 2f
                style = Paint.Style.STROKE
                pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f)
            }

            // Fill whole document with warm cream
            backgroundPaint.color = android.graphics.Color.parseColor("#FFF9E5")
            canvas.drawRect(0f, 0f, 595f, 842f, backgroundPaint)

            // Header Banner
            canvas.drawText("ZIINLY • PHYSICAL 1-PAGE FOLDING BLUEPRINT", 297.5f, 40f, textPaint)
            canvas.drawText(title.uppercase(), 297.5f, 70f, titlePaint)
            canvas.drawText("Instructions: Fold in half lengthwise. Slice center line. Fold together into modular book.", 297.5f, 95f, textPaint)

            // Define the 4x2 matrix rectangle inside the A4 page (centered)
            val matrixLeft = 47.5f // 595 - 500 = 95 / 2
            val matrixTop = 150f
            val cellWidth = 125f // 500 / 4
            val cellHeight = 180f // 360 / 2

            // Draw each Cell onto PDF
            for (row in 0..1) {
                for (col in 0..3) {
                    val index = row * 4 + col
                    val cell = cells[index]

                    val x = matrixLeft + col * cellWidth
                    val y = matrixTop + row * cellHeight

                    // 1. Draw Cell Background
                    backgroundPaint.color = android.graphics.Color.parseColor(cell.backgroundColorHex)
                    canvas.drawRect(x, y, x + cellWidth, y + cellHeight, backgroundPaint)

                    // 2. Try drawing image if local or loaded
                    if (cell.imagePath != null) {
                        try {
                            val bitmap = retrieveBitmap(context, cell.imagePath)
                            if (bitmap != null) {
                                canvas.save()
                                canvas.clipRect(x, y, x + cellWidth, y + cellHeight)

                                if (row == 0) {
                                    // Upside down top row rotated 180!
                                    canvas.rotate(180f, x + cellWidth/2f, y + cellHeight/2f)
                                }

                                val sourceWidth = bitmap.width
                                val sourceHeight = bitmap.height
                                val scale = Math.max(cellWidth / sourceWidth, cellHeight / sourceHeight) * cell.imageZoom
                                val drawWidth = sourceWidth * scale
                                val drawHeight = sourceHeight * scale
                                val dx = (cellWidth - drawWidth) * cell.focalX
                                val dy = (cellHeight - drawHeight) * cell.focalY

                                val destRect = android.graphics.RectF(x + dx, y + dy, x + dx + drawWidth, y + dy + drawHeight)
                                canvas.drawBitmap(bitmap, null, destRect, null)
                                canvas.restore()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // 3. Draw Page text overlay
                    canvas.save()
                    if (row == 0) {
                        // Rotation Rule physically!
                        canvas.rotate(180f, x + cellWidth/2f, y + cellHeight/2f)
                    }

                    // Draw clean box for text if image exists to make it readable, or just text overlay
                    if (cell.textOverlay.isNotBlank()) {
                        val cardLabelPaint = Paint().apply {
                            color = android.graphics.Color.BLACK
                            alpha = 140
                        }
                        canvas.drawRect(x + 4f, y + cellHeight - 34f, x + cellWidth - 4f, y + cellHeight - 4f, cardLabelPaint)

                        val cellTextPaint = Paint().apply {
                            isAntiAlias = true
                            textSize = 8f
                            color = android.graphics.Color.WHITE
                            isFakeBoldText = true
                            textAlign = Paint.Align.CENTER
                        }
                        
                        // Safety multi-line wrap
                        val overlay = cell.textOverlay
                        if (overlay.length > 20) {
                            canvas.drawText(overlay.substring(0, Math.min(20, overlay.length)), x + cellWidth/2f, y + cellHeight - 20f, cellTextPaint)
                            canvas.drawText(overlay.substring(Math.min(20, overlay.length)), x + cellWidth/2f, y + cellHeight - 8f, cellTextPaint)
                        } else {
                            canvas.drawText(overlay, x + cellWidth/2f, y + cellHeight - 14f, cellTextPaint)
                        }
                    }

                    // Draw Index Badge in corner
                    val badgePaint = Paint().apply {
                        isAntiAlias = true
                        textSize = 7f
                        color = android.graphics.Color.parseColor("#1A1A2E")
                        isFakeBoldText = true
                    }
                    canvas.drawText(cell.pageName, x + 8f, y + 16f, badgePaint)
                    canvas.restore()
                }
            }

            // Draw matrix outer boundary and separating grid borders
            canvas.drawRect(matrixLeft, matrixTop, matrixLeft + 500f, matrixTop + 360f, linePaint)
            
            // Vertical split lines
            for (col in 1..3) {
                val cx = matrixLeft + col * cellWidth
                canvas.drawLine(cx, matrixTop, cx, matrixTop + 360f, linePaint)
            }

            // Central CUT-LINE representing physical zine architecture
            val centerY = matrixTop + cellHeight
            canvas.drawLine(matrixLeft, centerY, matrixLeft + 500f, centerY, dashedPaint)

            // Draw scissor indications in borders
            textPaint.textSize = 16f
            canvas.drawText("✂", matrixLeft - 16f, centerY + 5f, textPaint)
            canvas.drawText("✂", matrixLeft + 516f, centerY + 5f, textPaint)

            // Build footprint metadata footer
            textPaint.textSize = 9f
            canvas.drawText("DESIGNED WITH ZIINLY • CODES ON THE DIGITAL BOOKSHELF", 297.5f, 540f, textPaint)

            pdfDocument.finishPage(page)

            // Save PDF into device's downloads folder or Documents folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "Ziinly_${title.replace(" ", "_")}_blueprint.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Saved folded A4 PDF to downloads folder!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed compiling PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Network or local bitmap retriever safety wrapper
private suspend fun retrieveBitmap(context: Context, path: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                val url = URL(path)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.getInputStream()
                BitmapFactory.decodeStream(input)
            } else {
                val uri = Uri.parse(path)
                val input = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
