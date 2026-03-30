package com.example.myapplication.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.myapplication.data.model.UserLocation


class LocationViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<UserLocation>>(emptyList())
    val users: StateFlow<List<UserLocation>> = _users.asStateFlow()

    init {
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            delay(1000)

            _users.value = listOf(
                UserLocation(1, "Anvar", 41.3111, 69.2401),
                UserLocation(2, "Bobur", 41.2995, 69.2401),
                UserLocation(3, "G'ayrat", 41.3250, 69.2600),
                UserLocation(4, "O'ktam", 41.3000, 69.2000)
            )
        }
    }
}