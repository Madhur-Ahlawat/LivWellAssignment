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

    var onInputKeywordChanged: (String) -> Unit = {
        _searchText.value = it
    }

    private var _cardDigits = MutableStateFlow("")
    val cardDigits: StateFlow<String> = _cardDigits

    private var _expiryDate = MutableStateFlow(TextFieldValue(""))
    val expiryDate: StateFlow<TextFieldValue> = _expiryDate


    fun changeCardDigits(input: String) {
        if (input.length <= 6 && input.all { it.isDigit() }) {
            _cardDigits.value = input
        }
    }

    fun changeExpiryDate(newValue: TextFieldValue) {
        _expiryDate.value = newValue
    }

    fun formatExpiryDate(value: TextFieldValue): TextFieldValue {
        var text = value.text.filter { it.isDigit() } // remove non-digits
        var selectionIndex = value.selection.end

        if (text.length > 2) {
            text = text.substring(0, 2) + "/" + text.substring(2)
            if (selectionIndex == 3 && !value.text.contains("/")) {
                selectionIndex++ // move cursor past "/"
            }
        }

        // Limit to MM/YY format (5 chars total)
        if (text.length > 5) {
            text = text.substring(0, 5)
        }

        // Keep cursor within text bounds
        selectionIndex = selectionIndex.coerceIn(0, text.length)

        return TextFieldValue(
            text = text,
            selection = TextRange(selectionIndex)
        )
    }

    fun onSubmitCardInfo() {
        if (_cardDigits.value.length == 6 && _expiryDate.value.text.matches(Regex("\\d{2}/\\d{2}"))) {
            println("Card: ${_cardDigits.value}, Expiry: ${_expiryDate.value}")
        }
    }

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