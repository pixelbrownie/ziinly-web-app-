package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun branding_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9E5))
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .fillMaxWidth()
              .border(3.dp, Color(0xFF1A1A2E), RoundedCornerShape(24.dp))
              .background(Color(0xFFF3B0C3))
              .padding(32.dp)
          ) {
            Text(
              text = "Ziinly",
              fontSize = 36.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "THE ANTI-PHOTO DUMP",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1A1A2E),
              letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Your elite 8 moments, folded into raw local zines.",
              fontSize = 14.sp,
              color = Color(0xFF1A1A2E),
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
