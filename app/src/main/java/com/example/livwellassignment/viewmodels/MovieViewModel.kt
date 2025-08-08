package com.example.livwellassignment.viewmodels

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.network.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun setDarkTheme(value: Boolean) {
        _isDarkTheme.value = value
    }

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText
    var movieResponse: Flow<PagingData<MovieListItem>>? = null

    var onInputKeywordChanged: (String) -> Unit = {
        _searchText.value = it
    }

//    private val _pin = MutableStateFlow("")
//    val pin: StateFlow<String> get() = _pin

    private var _cardDigits = MutableStateFlow("")
    val cardDigits: StateFlow<String> = _cardDigits

    private var _expiryDate = MutableStateFlow(TextFieldValue())
    val expiryDate: StateFlow<TextFieldValue> = _expiryDate
//    private var _focusedField = mutableStateOf(FocusedFieldEnum.NONE)


    fun changeCardDigits(input: String) {
        if (input.length <= 6 && input.all { it.isDigit() }) {
            _cardDigits.value = input
        }
    }

    fun changeExpiryDate(newValue: TextFieldValue) {
        val raw = newValue.text.filter { it.isDigit() }

        val formatted = when {
            raw.length <= 2 -> raw
            else -> raw.substring(0, 2) + "/" + raw.substring(2)
        }

        // Calculate new cursor position after formatting
        val cursorPosition = maxOf(formatted.length, newValue.selection.end)

        _expiryDate.value = TextFieldValue(
            text = formatted,
            selection = TextRange(cursorPosition)
        )
    }

    fun onSubmitCardInfo() {
        if (_cardDigits.value.length == 6 && _expiryDate.value.text.matches(Regex("\\d{2}/\\d{2}"))) {
            println("Card: ${_cardDigits.value}, Expiry: ${_expiryDate.value}")
        }
    }
    fun formatExpiry(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        val trimmed = digits.take(4)
        val month = trimmed.take(2).toIntOrNull()?.coerceIn(1, 12)?.toString()?.padStart(2, '0') ?: ""
        return when {
            trimmed.length <= 2 -> month
            else -> month + "/" + trimmed.drop(2)
        }
    }

//    fun addPINDigit(digit: String) {
//        if (_pin.value.length < 6) {
//            _pin.value += digit
//        }
//    }

//    fun deletePINDigit() {
//        if (_pin.value.isNotEmpty()) {
//            _pin.value = _pin.value.dropLast(1)
//        }
//    }


//    fun resetPin() {
//        _pin.value = ""
//    }

//    val uiState: StateFlow<MovieUiState> = _searchText
//        .debounce(300)
//        .distinctUntilChanged()
//        .flatMapLatest { input ->
//            if (input.isBlank()) {
//                flow {
//                    emit(MovieUiState.Error("Please enter movie name!"))
//                }
//            } else {
//                repository.getMovies(input)
//                    .map { pagingData ->
//                        MovieUiState.Success(pagingData) as MovieUiState
//                    }
//                    .onStart {
//                        emit(MovieUiState.Loading)
//                    }
//                    .catch { e ->
//                        emit(MovieUiState.Error(e.message ?: "Unknown error"))
//                    }
//            }
//        }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            MovieUiState.Loading
//        )
val movies: Flow<PagingData<MovieListItem>> = _searchText
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest { input ->
        if (input.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            repository.getMovies(input)
        }.cachedIn(viewModelScope)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(300), PagingData.empty())
}