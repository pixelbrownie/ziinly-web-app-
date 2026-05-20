package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ZineEntity
import com.example.viewmodel.ZineViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: ZineViewModel,
    onNavigateToEditor: (Int?) -> Unit,
    onNavigateToFlipbook: (Int) -> Unit,
    onNavigateToExplore: () -> Unit
) {
    val zines by viewModel.allZines.collectAsState()
    var selectedZineForActions by remember { mutableStateOf<ZineEntity?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // High-contrast, Neo-Brutalist Custom Bottom Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp),
                color = Color(0xFF1A1A2E),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bookshelf Tab (Active)
                    Row(
                        modifier = Modifier
                            .testTag("nav_bookshelf")
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF3B0C3))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Book,
                            contentDescription = "Bookshelf",
                            tint = Color(0xFF1A1A2E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Bookshelf",
                            color = Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Discovery Feed Tab (Inactive)
                    IconButton(
                        onClick = { onNavigateToExplore() },
                        modifier = Modifier.testTag("nav_explore")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Explore,
                                contentDescription = "Discovery Feed",
                                tint = Color(0xFFFFF9E5),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            // Retro heavy-border Floating Action Button to Curate a New Zine
            Button(
                onClick = {
                    viewModel.startNewZine()
                    onNavigateToEditor(null)
                },
                modifier = Modifier
                    .testTag("create_zine_button")
                    .padding(bottom = 8.dp)
                    .height(64.dp)
                    .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(24.dp))
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF3B0C3),
                    contentColor = Color(0xFF1A1A2E)
                ),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF1A1A2E)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "CURATE ZINE",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Brand Header with Neo-brutalist styling & physical offset shadow layer + tape ornaments
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, end = 8.dp)
            ) {
                // Flat black offset shadow layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 8.dp, y = 8.dp)
                        .background(Color(0xFF1A1A2E), RoundedCornerShape(24.dp))
                )

                // Front color layer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3B0C3), RoundedCornerShape(24.dp))
                        .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF1A1A2E),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ziinly",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1A2E),
                                letterSpacing = (-1.5).sp
                            )
                        }
                        Text(
                            text = "THE ANTI-PHOTO DUMP ✦",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A2E),
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your elite 8 moments, folded into raw local zines.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A2E),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Tactile craft tape stickers over the corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 10.dp, y = (-12).dp)
                            .rotate(8f)
                            .background(Color(0xDDFFFFAA)) // bright semi-trans washi tape
                            .border(1.dp, Color(0xFF1A1A2E).copy(0.3f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ORIGINAL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1A2E).copy(0.7f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // The main shelf content or empty state
            if (zines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color(0xFF1A1A2E),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your collection is empty",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Select 8 epic photos from a concert, road trip, or late-night hang and fold them into a pocket zine.",
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A2E).copy(0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    "MY DIGITAL BOOKSHELF",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A2E),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Render group of 3 per shelf representing visual wooden boards!
                val chunkedZines = remember(zines) { zines.chunked(3) }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(chunkedZines) { shelfGroup ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Row of vertical books
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                shelfGroup.forEach { zine ->
                                    val zineColor = Color(android.graphics.Color.parseColor(zine.themeColorHex))
                                    
                                    // Custom 3D Book Spine Composable mimicking physical page stack edges
                                    Row(
                                        modifier = Modifier
                                            .testTag("book_spine_${zine.id}")
                                            .width(90.dp)
                                            .height(154.dp)
                                            .shadow(6.dp, RoundedCornerShape(12.dp))
                                            .border(
                                                3.dp,
                                                Color(0xFF1A1A2E),
                                                RoundedCornerShape(12.dp)
                                            )
                                            .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp))
                                            .clickable { selectedZineForActions = zine }
                                    ) {
                                        // Left Spine / Cover Layer
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(zineColor, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                                                .padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            // Top Stripe decoration
                                            Box(
                                                modifier = Modifier
                                                    .width(36.dp)
                                                    .height(6.dp)
                                                    .background(Color(0xFF1A1A2E).copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                            )

                                            // Title written vertically or cropped safely
                                            Text(
                                                text = zine.title.uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF1A1A2E),
                                                textAlign = TextAlign.Center,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                                            )

                                            // Private or Public Flag Badge
                                            Icon(
                                                imageVector = if (zine.isPublic) Icons.Filled.Public else Icons.Filled.Lock,
                                                contentDescription = null,
                                                tint = Color(0xFF1A1A2E),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        // Physical Paper Folds Stripe (Page stack peeking from right side)
                                        Box(
                                            modifier = Modifier
                                                .width(10.dp)
                                                .fillMaxHeight()
                                                .background(Color(0xFFF3F3E8), RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                        ) {
                                            // Left boundary separator line
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .width(1.dp)
                                                    .background(Color(0xFF1A1A2E).copy(0.3f))
                                                    .align(Alignment.CenterStart)
                                            )
                                            // Paper seam lines (simulating layers of pages)
                                            Column(
                                                modifier = Modifier.fillMaxHeight(),
                                                verticalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                repeat(4) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(1.dp)
                                                            .background(Color(0xFF1A1A2E).copy(0.15f))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // The actual physical-style wooden shelf representation!
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(4.dp))
                                    .background(Color(0xFFB77955)) // Shelf Wood Brown
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Retro Bottom Sheet for actions of selected Zine
    val activeZine = selectedZineForActions
    if (activeZine != null) {
        val formattedDate = remember(activeZine.createdAt) {
            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(activeZine.createdAt))
        }

        AlertDialog(
            onDismissRequest = { selectedZineForActions = null },
            title = {
                Text(
                    text = activeZine.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color(0xFF1A1A2E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column {
                    Text(
                        text = "Curated on $formattedDate",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A2E).copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = activeZine.description,
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A2E),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Open Flipbook Button
                    Button(
                        onClick = {
                            selectedZineForActions = null
                            onNavigateToFlipbook(activeZine.id)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).border(2.dp, Color(0xFF1A1A2E), RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3B0C3),
                            contentColor = Color(0xFF1A1A2E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OPEN INTERACTIVE FOLD", fontWeight = FontWeight.Bold)
                    }

                    // Edit Layout Button
                    Button(
                        onClick = {
                            selectedZineForActions = null
                            viewModel.loadZineForEditing(activeZine.id)
                            onNavigateToEditor(activeZine.id)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).border(2.dp, Color(0xFF1A1A2E), RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFCBF3F0),
                            contentColor = Color(0xFF1A1A2E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("EDIT GRID ARCHITECTURE", fontWeight = FontWeight.Bold)
                    }

                    // Delete Curation
                    Button(
                        onClick = {
                            viewModel.deleteZine(activeZine)
                            selectedZineForActions = null
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).border(2.dp, Color(0xFF1A1A2E), RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9F1C),
                            contentColor = Color(0xFF1A1A2E)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REMOVE CURATION", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedZineForActions = null },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF1A1A2E)
                    )
                ) {
                    Text("CLOSE MENU", fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color(0xFFFFF9E5),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(24.dp))
        )
    }
}
