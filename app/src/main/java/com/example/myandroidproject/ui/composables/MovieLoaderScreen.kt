package com.example.myandroidproject.ui.composables

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.myandroidproject.R
import com.example.myandroidproject.ui.fonts.MonaSans
import com.example.myandroidproject.ui.fonts.MonaSansExtraBold
import com.example.myandroidproject.util.compose_util.Last6CardDigitsTransformation
import com.example.myandroidproject.viewmodels.MovieViewModel

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
    val cardDigits by viewModel.cardDigits.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()
    val textValue = expiryDate.copy(selection = TextRange(expiryDate.text.length))
    var errorDay by remember { mutableStateOf<String?>(null) }
    var errorMonth by remember { mutableStateOf<String?>(null) }
    var completeError by remember { mutableStateOf<String?>("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        // Top AppBar
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier
                .size(28.dp)
                .clickable { /* Handle back */ }
        )

        Spacer(Modifier.height(18.dp))

        Text(
            style = TextStyle(
                fontFamily = MonaSansExtraBold,
                fontSize = 24.sp, fontWeight = FontWeight.Bold
            ), text =
                "Verify debit card details"
        )
        Text(
            style = TextStyle(
                fontFamily = MonaSans,
                fontSize = 16.sp,
                color = Color(0xFF7B7B7B)
            ),
            text =
                "Enter details of your ICICI Bank debit card to set UPI PIN"
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
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = Color(0xE9E9E9E9))
                            .padding(1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color = Color.White)
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo_icici_bank), // your drawable
                                contentDescription = "ICICI Bank Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Inside
                            )
                        }

                    }


                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "ICICI Bank", style = TextStyle(
                                fontFamily = MonaSans,
                                fontSize = 14.sp,
                                color = Color.Black, fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            "Savings Account - 9134", style = TextStyle(
                                fontFamily = MonaSans,
                                fontSize = 12.sp,
                                color = Color(0xFF7B7B7B), fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Enter the last 6 digits of card number", style = TextStyle(
                fontFamily = MonaSans,
                fontSize = 14.sp,
                color = Color.Black, fontWeight = FontWeight.SemiBold
            )
        )
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

        Spacer(Modifier.height(24.dp))

        // Expiry Date
        Text(
            text = "Expiry Date", style = TextStyle(
                fontFamily = MonaSans,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(Modifier.height(12.dp))


        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                if (newValue.text.length <= 5) {
                    val digits = newValue.text.filter { it.isDigit() }

                    val formatted = buildString {
                        for (i in digits.indices) {
                            append(digits[i])
                            if (i == 1 && digits.length > 2) {
                                append("/")
                            }
                        }
                    }

                    // Validation
                    val parts = formatted.split("/")
                    when {
                        parts.size == 1 && parts[0].length == 2 -> {
                            val day = parts[0].toIntOrNull()
                            if (day == null || day !in 1..31) {
                                errorDay = "day"
                            } else {
                                errorDay = null
                            }
                        }

                        parts.size == 2 && parts[1].length == 2 -> {
                            val day = parts[0].toIntOrNull()
                            val month = parts[1].toIntOrNull()
                            if (day != null && day in 1..31) {
                                errorDay = null
                            }
                            if (month != null && month in 1..12) {
                                errorMonth = null
                            }
                            if (day == null || day !in 1..31) {
                                errorDay = "day"
                            }
                            if (month == null || month !in 1..12) {
                                errorMonth = "month"
                            }
                        }

                        else -> {
                            if (completeError != null || newValue.toString().isNullOrEmpty()) {
                                errorDay = null
                                errorMonth = null
                                completeError = ""
                            }
                        }
                    }
                    if (errorDay != null && errorMonth != null) {
                        completeError = "Invalid day and month!"
                    } else if (errorDay != null) {
                        completeError = "Invalid " + errorDay
                    } else if (errorMonth != null) {
                        completeError = "Invalid " + errorMonth
                    }
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
                ),
            shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(
                fontFamily = MonaSans,
                fontSize = 20.sp,
                color = Color(0xFF1F1F1F)
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF909090),
                unfocusedBorderColor = Color(0xFF909090),
                cursorColor = Color.Black,
                errorBorderColor = Color.Red
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            placeholder = {
                Text(
                    "DD/MM",
                    style = TextStyle(
                        fontFamily = MonaSans,
                        fontSize = 20.sp,
                        color = Color(0xFFC1C1C1)
                    )
                )
            },
            isError = errorDay != null || errorMonth != null
        )

        if (!completeError!!.trim().isNullOrEmpty()) {
            Text(
                text = completeError!!,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onSubmitCardInfo() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1F1F1F), // Black background
                contentColor = Color.White          // Text color
            )
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp
            )
        }
    }

}