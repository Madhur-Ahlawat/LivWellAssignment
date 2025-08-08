package com.example.livwellassignment.ui.composables

import Last6CardDigitsTransformation
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.livwellassignment.R
import com.example.livwellassignment.ui.fonts.MonaSans
import com.example.livwellassignment.ui.fonts.MonaSansBold
import com.example.livwellassignment.ui.fonts.MonaSansExtraBold
import com.example.livwellassignment.util.compose_util.ZeroGrayTransformation
import com.example.livwellassignment.viewmodels.MovieViewModel
import okhttp3.internal.format
import kotlin.math.exp

@Composable
fun MovieGridScreen(
    modifier: Modifier = Modifier,
    viewModel: MovieViewModel
) {
    val search by viewModel.searchText.collectAsState()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        SearchBar(
            onSearch = viewModel.onInputKeywordChanged,
            viewModel = viewModel
        )

        when {
            movies.loadState.refresh is LoadState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            movies.loadState.refresh is LoadState.Error -> {
                val error = movies.loadState.refresh as LoadState.Error
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${error.error.message}")
                }
            }

            movies.itemCount == 0 && search.isNotBlank() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found.")
                }
            }

            search.isBlank() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please enter movie name!")
                }
            }

            else -> {
                MovieGrid(modifier = Modifier.weight(1f), viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyCardScreen(
    viewModel: MovieViewModel = viewModel(),
    modifier: Modifier
) {
    val inputFieldheight = 54.dp
    val cardDigits by viewModel.cardDigits.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()
    val textValue = expiryDate.copy(selection = TextRange(expiryDate.text.length))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        // Top AppBar
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .size(28.dp)
                .clickable { /* Handle back */ }
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Verify debit card details",
            fontSize = 24.sp,
            fontFamily = MonaSans,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Enter details of your ICICI Bank debit card to set UPI PIN",
            fontSize = 14.sp,
            fontFamily = MonaSans,
            color = Color(0xFF7B7B7B)
        )

        Spacer(Modifier.height(24.dp))

        // Bank Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0xFFDEDEDE),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0xDEDEDE)
                    )
            ) {
                Row(
                    Modifier
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountBox, contentDescription = "Bank", tint = Color.Red)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("ICICI Bank", fontWeight = FontWeight.Bold)
                        Text("Savings Account - 9134", color = Color.Gray)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Enter the last 6 digits of card number")
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = cardDigits,
            onValueChange = { viewModel.changeCardDigits(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF909090),
                    shape = RoundedCornerShape(8.dp)
                ),
            shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(
                fontFamily = MonaSansExtraBold,
                fontSize = 22.sp
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF909090),
                unfocusedBorderColor = Color(0xFF909090),
                cursorColor = Color.Black
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            singleLine = true,
            visualTransformation = Last6CardDigitsTransformation()
        )

        Spacer(Modifier.height(16.dp))

        // Expiry Date
        Text("Expiry Date")

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = textValue, onValueChange = { newValue ->
                if (newValue.text.toString().length <= 5) {
                    val oldValue = expiryDate
                    val oldText = oldValue.text
                    val newDigits = newValue.text.filter { it.isDigit() }

                    // Build formatted string
                    val formatted = buildString {
                        for (i in newDigits.indices) {
                            append(newDigits[i])
                            if (i == 1 && newDigits.length > 2) {
                                append("/")
                            }
                        }
                    }

//                    var newCursorPos = newValue.selection.start

//                    val adding = newDigits.length > oldText.filter { it.isDigit() }.length
//                    val deleting = newDigits.length < oldText.filter { it.isDigit() }.length

//                    if (adding) {
//                        // If adding after day (pos==2 before formatting), skip over "/"
//                        if (oldValue.selection.start == 2) {
//                            newCursorPos++
//                        }
//                    } else if (deleting) {
//                        // If deleting right after "/", jump before it
//                        if (oldValue.selection.start == 3 && oldText.getOrNull(2) == '/') {
//                            newCursorPos--
//                        }
//                    }

                    // Special mid-edit case: 1{cursor}1 → insert digit → 11/{cursor}1
//                    if (adding && oldValue.selection.start == 1 && formatted.length >= 3) {
//                        if (formatted.getOrNull(2) == '/') {
//                            newCursorPos = 3
//                        }
//                    }

                    viewModel.changeExpiryDate(
                        TextFieldValue(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF909090),
                    shape = RoundedCornerShape(8.dp)
                ), shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(
                fontFamily = MonaSans,
                fontSize = 22.sp,
                color = Color(0xFF1F1F1F)
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF909090),
                unfocusedBorderColor = Color(0xFF909090),
                cursorColor = Color.Black
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = {
                Text(
                    "DD/MM",
                    style = TextStyle(
                        fontFamily = MonaSans,
                        fontSize = 22.sp,
                        color = Color(0xFFC1C1C1)
                    )
                )
            }
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onSubmitCardInfo() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(color = Color(0xFF1F1F1F), shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Continue", fontSize = 16.sp)
        }
    }
}