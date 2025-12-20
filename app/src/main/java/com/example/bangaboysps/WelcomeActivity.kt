package com.example.bangaboysps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bangaboysps.ui.theme.BangaBoysPSTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class WelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize Firebase (safe even if already initialized)
        FirebaseApp.initializeApp(this)
        Log.d("FCM_DEBUG", "Firebase initialized")

        // ✅ Subscribe to topic (this is what real users will use)
        FirebaseMessaging.getInstance()
            .subscribeToTopic("all_users")
            .addOnSuccessListener {
                Log.d("FCM_DEBUG", "Subscribed to topic: all_users")
            }
            .addOnFailureListener { e ->
                Log.e("FCM_DEBUG", "Topic subscription FAILED", e)
            }

        // 🔑 GET FCM TOKEN (ONLY FOR TESTING IN FIREBASE CONSOLE)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM_TOKEN", token)
                } else {
                    Log.e("FCM_TOKEN", "Failed to get FCM token", task.exception)
                }
            }

        setContent {
            BangaBoysPSTheme {
                WelcomeScreen()
            }
        }
    }
}

@Composable
fun WelcomeScreen() {

    val context = LocalContext.current
    val skyBlue = Color(0xFFB3E5FC)
    val textBlack = Color.Black

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(skyBlue)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "WELCOME",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = textBlack
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "BANGA (BOYS) PRIMARY SCHOOL",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = textBlack
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "[A Model School of Pry. & Pre Pry. Level]",
                fontSize = 16.sp,
                maxLines = 1,
                textAlign = TextAlign.Center,
                color = textBlack
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    context.startActivity(
                        Intent(context, MainActivity::class.java)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("MENU", fontSize = 20.sp)
            }
        }
    }
}
