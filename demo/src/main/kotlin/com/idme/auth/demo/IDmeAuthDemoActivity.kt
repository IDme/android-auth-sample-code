package com.idme.auth.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.idme.auth.demo.ui.ContentScreen
import com.idme.auth.demo.ui.IDmeAuthDemoTheme

class IDmeAuthDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IDmeAuthDemoTheme {
                ContentScreen()
            }
        }
    }
}
