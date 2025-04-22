package com.example.adopt_pet_app.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingViewModel : ViewModel() {

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted = _onboardingCompleted.asStateFlow()

    fun onGetStartedClicked() {
        _onboardingCompleted.value = true
        // Có thể lưu trạng thái này vào DataStore để lần sau app bỏ qua onboarding
    }
}
