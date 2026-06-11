package com.levonty.unitalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.levonty.unitalk.ui.NavGraph
import com.levonty.unitalk.ui.Routes
import com.levonty.unitalk.ui.UniTalkNavGraph
import com.levonty.unitalk.ui.theme.UniTalkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniTalkTheme {
                // For MVP, always start at Register.
                // In production, check session and route to Search if logged in.
                UniTalkNavGraph(startDestination = Routes.REGISTER)
            }
        }
    }
}