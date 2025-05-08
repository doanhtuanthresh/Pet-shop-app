package com.example.adopt_pet_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.adopt_pet_app.ui.navigation.Routes.PROFILE
import com.example.adopt_pet_app.ui.screen.login.LoginScreen
import com.example.adopt_pet_app.ui.screen.signup.SignUpScreen
import com.example.adopt_pet_app.ui.screen.onboarding.OnboardingScreen
import com.example.adopt_pet_app.ui.screen.home.HomeScreen
import com.example.adopt_pet_app.ui.screen.post.PostDetailScreen
import com.example.adopt_pet_app.ui.screen.profile.ProfileScreen
import com.example.adopt_pet_app.ui.screen.post.PostScreen
import com.example.adopt_pet_app.ui.screen.profile.EditProfileScreen

// TODO: import SearchScreen, CategoryScreen, PostDetailScreen nếu đã có

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SEARCH = "search"
    const val CATEGORY = "category/{type}"
    const val POST_DETAIL = "post/{id}"
    const val PROFILE = "profile/{userId}"
    const val POST_CREATE = "post_create"


    fun getProfileRoute(userId: String) = "profile/$userId"
    fun getCategoryRoute(type: String) = "category/$type"
    fun getPostDetailRoute(id: String) = "post/$id"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Routes.SIGN_UP) },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(onGetStartedClick = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(Routes.SEARCH) {
            // TODO: Replace with real screen
            // SearchScreen(navController)
        }

        composable(
            route = Routes.CATEGORY,
            arguments = listOf(navArgument("type") { defaultValue = "All" })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "All"
            // TODO: Replace with real screen
            // CategoryScreen(type, navController)
        }

        composable(
            route = Routes.POST_DETAIL,
            arguments = listOf(navArgument("id") { defaultValue = "" })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("id") ?: ""
            // TODO: Replace with real screen
            // PostDetailScreen(postId, navController)
        }

        composable(
            route = PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(userId = userId, navController = navController)
        }

        composable(Routes.POST_CREATE) {
            PostScreen(onPostSuccess = {
                navController.popBackStack() // quay lại sau khi đăng bài thành công
            })
        }

        composable("postDetail/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(postId)
        }

        composable("editProfile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            EditProfileScreen(userId = userId, navController = navController)
        }

    }
}
