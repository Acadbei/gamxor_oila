package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.ui.screen.FamilyCareApp
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                FamilyCareApp()
            }
        }
    }
}
