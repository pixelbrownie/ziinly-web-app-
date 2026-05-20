package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
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
import com.example.data.models.ZineCell
import com.example.data.models.ZineEntity
import com.example.viewmodel.ZineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlipbookScreen(
    viewModel: ZineViewModel,
    zineId: Int,
    onNavigateBack: () -> Unit
) {
    val zines by viewModel.allZines.collectAsState()
    val zine = remember(zines, zineId) { zines.find { it.id == zineId } }

    if (zine == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Zine not found.")
        }
        return
    }

    val cells = remember(zine) { zine.getCells() }
    var currentSpreadIndex by remember { mutableStateOf(0) } // 0 to 4 (Cover, Spread 1, 2, 3, Back)

    // Layout configuration for spreads
    // Index mapping from 4x2 grid:
    // [0]=Pg 4, [1]=Pg 3, [2]=Pg 2, [3]=Pg 1 (Row 0, unfolded flipped)
    // [4]=Pg 5, [5]=Pg 6, [6]=Back, [7]=Cover (Row 1, unfolded upright)
    //
    // Book closed showing Cover on right: SpreadIndex = 0
    // Pages 1 & 2 open: SpreadIndex = 1
    // Pages 3 & 4 open: SpreadIndex = 2
    // Pages 5 & 6 open: SpreadIndex = 3
    // Book closed showing Back on left: SpreadIndex = 4

    val leftCell = when (currentSpreadIndex) {
        0 -> null
        1 -> cells[3] // Page 1
        2 -> cells[1] // Page 3
        3 -> cells[4] // Page 5
        4 -> cells[6] // Back
        else -> null
    }

    val rightCell = when (currentSpreadIndex) {
        0 -> cells[7] // Cover
        1 -> cells[2] // Page 2
        2 -> cells[0] // Page 4
        3 -> cells[5] // Page 6
        4 -> null
        else -> null
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
                            text = zine.title,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF1A1A2E)
                        )
                        Text(
                            text = "3D Flipped Book Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF3B0C3),
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1A1A2E)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFFF9E5))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Instructions block
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFCBF3F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF1A1A2E), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFF2EC4B6))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Your Elite 8 moments have been reorientated, scaled, and compiled into an interactive flipping mini booklet.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                }
            }

            // BOOK VIEWPORT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Shadow Surface simulating physical depths
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(260.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .background(Color(0xFF1A1A2E).copy(alpha = 0.05f))
                )

                // The Spread Layout Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(240.dp)
                        .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1A1A2E)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEFT PAGE CARD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                leftCell?.let { Color(android.graphics.Color.parseColor(it.backgroundColorHex)) }
                                    ?: Color(0xFF1D1B22) // Back spine shadow if closed
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (leftCell != null) {
                            BookPageCard(cell = leftCell)
                        } else {
                            // Empty inside front flap
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color(0xFFFFF9E5).copy(0.15f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("ZIINLY", color = Color(0xFFFFF9E5).copy(0.15f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // CENTRAL REALISTIC SEWING SEAM SPLIT / SPINE BOUNDARY COZY STITCHES
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF1A1A2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Vertical dashed stitch sequence simulating real thread bound booklets
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            repeat(18) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(6.dp)
                                        .background(Color(0xFFFFF9E5).copy(alpha = 0.5f))
                                )
                            }
                        }
                    }

                    // RIGHT PAGE CARD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                rightCell?.let { Color(android.graphics.Color.parseColor(it.backgroundColorHex)) }
                                    ?: Color(0xFF1D1B22) // Back spine shadow if closed
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rightCell != null) {
                            BookPageCard(cell = rightCell)
                        } else {
                            // Empty inside back flap
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Color(0xFFFFF9E5).copy(0.15f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("THE END", color = Color(0xFFFFF9E5).copy(0.15f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // CONTROLLER NAV BUTTONS & PROGRESS
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Bullets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    repeat(5) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (currentSpreadIndex == i) 14.dp else 10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (currentSpreadIndex == i) Color(0xFFF3B0C3) else Color(0xFF1A1A2E).copy(0.2f))
                                .border(1.dp, Color(0xFF1A1A2E), RoundedCornerShape(5.dp))
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prev Button
                    IconButton(
                        onClick = { if (currentSpreadIndex > 0) currentSpreadIndex-- },
                        enabled = currentSpreadIndex > 0,
                        modifier = Modifier
                            .testTag("flip_prev")
                            .size(52.dp)
                            .border(
                                2.dp,
                                if (currentSpreadIndex > 0) Color(0xFF1A1A2E) else Color(0xFF1A1A2E).copy(0.2f),
                                CircleShape
                            )
                            .background(if (currentSpreadIndex > 0) Color(0xFFF3B0C3) else Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Page",
                            tint = if (currentSpreadIndex > 0) Color(0xFF1A1A2E) else Color(0xFF1A1A2E).copy(0.2f)
                        )
                    }

                    // Spread Indicator Description text
                    Text(
                        text = when (currentSpreadIndex) {
                            0 -> "FRONT COVER (OUTSIDE)"
                            1 -> "SPREAD: SIZES 1 & 2"
                            2 -> "SPREAD: SIZES 3 & 4"
                            3 -> "SPREAD: SIZES 5 & 6"
                            4 -> "BACK COVER (OUTSIDE)"
                            else -> ""
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A2E)
                    )

                    // Next Button
                    IconButton(
                        onClick = { if (currentSpreadIndex < 4) currentSpreadIndex++ },
                        enabled = currentSpreadIndex < 4,
                        modifier = Modifier
                            .testTag("flip_next")
                            .size(52.dp)
                            .border(
                                2.dp,
                                if (currentSpreadIndex < 4) Color(0xFF1A1A2E) else Color(0xFF1A1A2E).copy(0.2f),
                                CircleShape
                            )
                            .background(if (currentSpreadIndex < 4) Color(0xFFF3B0C3) else Color.Transparent, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Page",
                            tint = if (currentSpreadIndex < 4) Color(0xFF1A1A2E) else Color(0xFF1A1A2E).copy(0.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Public Community Toggles
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9E5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFF1A1A2E), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "PIN TO DISCOVERY FEED?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1A2E)
                            )
                            Text(
                                "Allow other curators to find and read this in physical flipbook mode.",
                                fontSize = 10.sp,
                                color = Color(0xFF1A1A2E).copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = zine.isPublic,
                            onCheckedChange = { isPub ->
                                viewModel.updateZineMetaData(zine.title, zine.description, zine.themeColorHex, isPub)
                                viewModel.saveCurrentZine()
                            },
                            modifier = Modifier.testTag("pin_feed_switch")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookPageCard(cell: ZineCell) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (cell.imagePath != null) {
            AsyncImage(
                model = cell.imagePath,
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

        // Dark Gradient Vignette for aesthetic contrast overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(0.25f))
        )

        // Page text Overlay
        if (cell.textOverlay.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = cell.textOverlay,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Page descriptor label inside book pages e.g. "Cover", "Page 2" in neon tabs
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .background(Color(0xFF1A1A2E), RoundedCornerShape(bottomEnd = 12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = cell.pageName,
                color = Color(0xFFFFF9E5),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
