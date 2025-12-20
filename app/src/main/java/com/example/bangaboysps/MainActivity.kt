package com.example.bangaboysps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bangaboysps.ui.theme.BangaBoysPSTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔔 Notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        setContent {
            BangaBoysPSTheme {
                MenuScreen { section ->
                    val intent = Intent(this, SectionActivity::class.java)
                    intent.putExtra("sectionType", section)
                    startActivity(intent)
                }
            }
        }
    }
}

/* ======================= UI BELOW (UNCHANGED) ======================= */

@Composable
fun MenuScreen(onOpen: (String) -> Unit) {

    val skyBlue = Color(0xFFB3E5FC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(skyBlue)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        MenuCategoryTitle("Announcement")
        MenuCard("View Announcement") { onOpen("announcement") }

        Spacer(Modifier.height(20.dp))

        MenuCategoryTitle("School Gallery")
        MenuCard("Images") { onOpen("images") }
        MenuCard("Videos") { onOpen("videos") }

        MenuCard("Digital Magazine (Banga Bithi)") { onOpen("magazine") }
        MenuCard("Holiday List") { onOpen("holiday") }

        Spacer(Modifier.height(20.dp))

        MenuCategoryTitle("Homework")
        MenuCard("PP") { onOpen("homework_pp") }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { MenuCardSmall("Class 1") { onOpen("homework_class1") } }
            Box(Modifier.weight(1f)) { MenuCardSmall("Class 2") { onOpen("homework_class2") } }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { MenuCardSmall("Class 3") { onOpen("homework_class3") } }
            Box(Modifier.weight(1f)) { MenuCardSmall("Class 4") { onOpen("homework_class4") } }
        }

        MenuCard("Class 5") { onOpen("homework_class5") }

        Spacer(Modifier.height(20.dp))

        MenuCategoryTitle("Marksheet")
        MenuCard("PP") { onOpen("marksheet_pp") }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { MenuCardSmall("Class 1") { onOpen("marksheet_class1") } }
            Box(Modifier.weight(1f)) { MenuCardSmall("Class 2") { onOpen("marksheet_class2") } }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { MenuCardSmall("Class 3") { onOpen("marksheet_class3") } }
            Box(Modifier.weight(1f)) { MenuCardSmall("Class 4") { onOpen("marksheet_class4") } }
        }

        MenuCard("Class 5") { onOpen("marksheet_class5") }
    }
}

@Composable
fun MenuCategoryTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun MenuCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun MenuCardSmall(title: String, onClick: () -> Unit) {
    MenuCard(title, onClick)
}
