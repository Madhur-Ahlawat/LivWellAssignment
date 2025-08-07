package com.example.livwellassignment.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.models.enums.FocusedFieldEnum
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

    private var _expiryDate = MutableStateFlow("")
    val expiryDate: StateFlow<String> = _expiryDate
//    private var _focusedField = mutableStateOf(FocusedFieldEnum.NONE)

    private var _focusedField = MutableStateFlow(FocusedFieldEnum.NONE)
    val focusedField : StateFlow<FocusedFieldEnum> = _focusedField

    fun setFocusedField(focusedFieldEnum: FocusedFieldEnum){
        _focusedField.value = focusedFieldEnum
    }
    fun getFocusedField(): MutableStateFlow<FocusedFieldEnum> {
        return _focusedField
    }

    fun onCardDigitChange(input: String) {
        if (input.length <= 6 && input.all { it.isDigit() }) {
            _cardDigits.value = input
        }
    }
    fun onExpiryChange(newVal: String) {
        if (newVal.length <= 5 && newVal.all { it.isDigit() || it == '/' }) {
            _expiryDate.value = formatExpiry(newVal)
        }
    }

    fun addCardDigit(digit: String) {
        onCardDigitChange(_cardDigits.value + digit)
    }

    fun addExpiryDigit(digit: String) {
        onExpiryChange(_expiryDate.value + digit)
    }

    fun deleteCardDigit() {
        _cardDigits.value = _cardDigits.value.dropLast(1)
    }

    fun onSubmitCardInfo() {
        if (_cardDigits.value.length == 6 && _expiryDate.value.matches(Regex("\\d{2}/\\d{2}"))) {
            println("Card: ${_cardDigits.value}, Expiry: ${_expiryDate.value}")
        }
    }
    fun formatExpiry(raw: String): String {
        return when {
            raw.length <= 2 -> raw
            else -> raw.substring(0, 2) + "/" + raw.substring(2)
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

    fun deleteExpiryDigit() {
        if (_expiryDate.value.isNotEmpty()) {
            val raw = _expiryDate.value.replace("/", "")
            val newRaw = raw.dropLast(1)
            _expiryDate.value = formatExpiry(newRaw)
        }
    }


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