package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.viewmodel.ZineViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExploreScreen(
    viewModel: ZineViewModel,
    onNavigateToBookshelf: () -> Unit,
    onNavigateToFlipbook: (Int) -> Unit
) {
    val publicZines by viewModel.publicZines.collectAsState()

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
                    // Bookshelf Tab (Inactive)
                    IconButton(
                        onClick = { onNavigateToBookshelf() },
                        modifier = Modifier.testTag("nav_bookshelf")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Book,
                                contentDescription = "Bookshelf",
                                tint = Color(0xFFFFF9E5),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Discovery Feed Tab (Active)
                    Row(
                        modifier = Modifier
                            .testTag("nav_explore")
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF3B0C3))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Explore,
                            contentDescription = "Discovery Feed",
                            tint = Color(0xFF1A1A2E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Discovery",
                            color = Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
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

            // Community discovery board header with neo-brutalist offset shadow layer + tape sticker
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

                // Front color layer (Sage Green)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFCBF3F0), RoundedCornerShape(24.dp))
                        .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Explore,
                                contentDescription = null,
                                tint = Color(0xFF1A1A2E),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Discovery Feed",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1A2E),
                                letterSpacing = (-1).sp
                            )
                        }
                        Text(
                            text = "PUBLIC CURATIONS ✦",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A1A2E),
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Swipe, explore, and open physical-format layouts made by other creators internationally.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A2E),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Tape decoration on top end
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 10.dp, y = (-12).dp)
                            .rotate(-6f)
                            .background(Color(0xCCEBD5C4)) // washi card tape
                            .border(1.dp, Color(0xFF1A1A2E).copy(0.3f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "COMMUNITY",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1A2E).copy(0.7f),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Board items
            if (publicZines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No public curations pinned yet.", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Be the first to share one!", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            } else {
                Text(
                    "TRENDING MINI ZINES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A2E),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(publicZines) { zine ->
                        val zineColor = Color(android.graphics.Color.parseColor(zine.themeColorHex))
                        val formattedDate = remember(zine.createdAt) {
                            SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(zine.createdAt))
                        }

                        // Discovery Card in brutalist design style with physical dual layer offset shadow
                        Box(
                            modifier = Modifier
                                .testTag("public_discovery_card_${zine.id}")
                                .padding(bottom = 6.dp, end = 6.dp)
                        ) {
                            // Bottom Shadow Layer
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 6.dp, y = 6.dp)
                                    .background(Color(0xFF1A1A2E), RoundedCornerShape(16.dp))
                            )

                            // Top Front Card Content Layer
                            Column(
                                modifier = Modifier
                                    .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFFF9E5))
                                    .clickable { onNavigateToFlipbook(zine.id) }
                            ) {
                                // Cover Preview blocks
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .background(zineColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                                        Icon(
                                            Icons.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = Color(0xFF1A1A2E),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = zine.title.uppercase(),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = Color(0xFF1A1A2E),
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                HorizontalDivider(
                                    thickness = 2.dp,
                                    color = Color(0xFF1A1A2E)
                                )

                            // Card text space
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = zine.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1A1A2E),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    minLines = 3
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Posted $formattedDate",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A2E).copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}
