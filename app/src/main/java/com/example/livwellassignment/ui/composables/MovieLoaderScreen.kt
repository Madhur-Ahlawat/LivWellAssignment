package com.example.livwellassignment.ui.composables

import Last6CardDigitsTransformation
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.livwellassignment.R
import com.example.livwellassignment.viewmodels.MovieViewModel

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

//@Composable
//fun SetmPinUI(modifier: Modifier,
//             viewModel: MovieViewModel ) {
//    val pin by viewModel.pin.collectAsState()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(24.dp),
//        verticalArrangement = Arrangement.SpaceBetween
//    ) {
//
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            // Header
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text("State Bank of India", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//                Spacer(modifier = Modifier.weight(1f))
//                Image(
//                    painter = painterResource(id = R.drawable.upi_logo),
//                    contentDescription = "UPI Logo",
//                    modifier = Modifier.size(48.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text("SET 6 digit UPI PIN", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // PIN Indicator
//            Row(
//                horizontalArrangement = Arrangement.Center,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                repeat(6) { index ->
//                    val isFilled = index < pin.length
//                    Box(
//                        modifier = Modifier
//                            .size(20.dp)
//                            .padding(4.dp)
//                            .clip(CircleShape)
//                            .background(if (isFilled) Color.Black else Color.LightGray)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Text(
//                text = "UPI PIN will keep your account secure from unauthorized access. Do not share this PIN with anyone.",
//                fontSize = 14.sp,
//                color = Color.Gray,
//                textAlign = TextAlign.Center
//            )
//        }
//
//        // Number Pad + Submit
//        Column {
//            NumberPad(
//                onNumberClick = {
//                    if (pin.length < 6) viewModel.addPINDigit(it)
//                },
//                onDelete = {
//                    if (pin.isNotEmpty()) viewModel.deletePINDigit()
//                },
//                onSubmit = {
//                    if (pin.length == 6) {
//                        viewModel.submitPin()
//                    }
//                }
//            )
//        }
//    }
//}
//


@Composable
fun VerifyCardScreen(
    viewModel: MovieViewModel = viewModel(),
    modifier: Modifier
) {
    val cardDigits by viewModel.cardDigits.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()

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

        Text("Verify debit card details", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Enter details of your ICICI Bank debit card to set UPI PIN", color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        // Bank Info Card
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier.padding(16.dp),
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

        Spacer(Modifier.height(24.dp))

        Text("Enter the last 6 digits of card number")
        OutlinedTextField(
            value = cardDigits,
            onValueChange = { viewModel.changeCardDigits(it) },
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text("•••• •••• ••00 0000") }, keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),            visualTransformation = Last6CardDigitsTransformation(),

            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        // Expiry Date
        Text("Expiry Date")
        OutlinedTextField(
            value = expiryDate,
            onValueChange = { viewModel.changeExpiryDate(it) },
            modifier = Modifier
                .fillMaxWidth(),
            label = { Text("Expiry Date") }, keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            placeholder = { Text("MM/YY") },
            singleLine = true
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onSubmitCardInfo() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Continue", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Powered by UPI
        Text(
            "Powered by",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontSize = 12.sp,
            color = Color.Gray
        )
        Image(
            painter = painterResource(R.drawable.upi_logo), // add UPI logo in drawable
            contentDescription = "UPI Logo",
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}