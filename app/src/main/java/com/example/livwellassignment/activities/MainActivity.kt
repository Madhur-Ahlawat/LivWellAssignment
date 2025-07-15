package com.example.livwellassignment.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.livwellassignment.ui.composables.MovieGridScreen
import com.example.livwellassignment.ui.theme.LivWellAssignmentTheme
import com.example.livwellassignment.viewmodels.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    var viewModel: MovieViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LivWellAssignmentTheme {
                viewModel = hiltViewModel()
                val defaultDarkTheme = isSystemInDarkTheme()
                var isDarkTheme by rememberSaveable { mutableStateOf(defaultDarkTheme) }
                val backgroundColor = if (isDarkTheme) {
                    MaterialTheme.colorScheme.background
                } else {
                    MaterialTheme.colorScheme.background
                }
                LaunchedEffect(Unit) {
                    viewModel?.setDarkTheme(isDarkTheme)
                }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                ) { innerPadding ->
                    MovieGridScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LivWellAssignmentTheme {
        MovieGridScreen(modifier = Modifier.fillMaxSize())
    }
}