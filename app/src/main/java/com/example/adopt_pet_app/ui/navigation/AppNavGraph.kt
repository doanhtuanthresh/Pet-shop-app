package com.example.adopt_pet_app.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.adopt_pet_app.ui.screen.login.LoginScreen
import com.example.adopt_pet_app.ui.screen.signup.SignUpScreen
import com.example.adopt_pet_app.ui.screen.onboarding.OnboardingScreen

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val ONBOARDING = "onboarding"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.SIGN_UP) }
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onGetStartedClick = {
                navController.navigate(Routes.LOGIN) // 👉 chuyển sang màn login sau khi nhấn "Get Started"
            })
        }
    }
}