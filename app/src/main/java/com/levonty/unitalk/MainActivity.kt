package com.levonty.unitalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.levonty.unitalk.ui.UniTalkNavGraph
import com.levonty.unitalk.ui.Routes
import com.levonty.unitalk.ui.theme.UniTalkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniTalkTheme {
                UniTalkNavGraph(startDestination = Routes.WELCOME)
            }
        }
    }
}