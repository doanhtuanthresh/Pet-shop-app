package com.example.adopt_pet_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.adopt_pet_app.ui.screen.authgate.AuthGateScreen
import com.example.adopt_pet_app.ui.screen.camera.CameraScreen
import com.example.adopt_pet_app.ui.screen.chat.ChatScreen
import com.example.adopt_pet_app.ui.screen.chat.ChatViewModel
import com.example.adopt_pet_app.ui.screen.chat.MessengerScreen
import com.example.adopt_pet_app.ui.screen.home.HomeScreen
import com.example.adopt_pet_app.ui.screen.login.LoginScreen
import com.example.adopt_pet_app.ui.screen.onboarding.OnboardingScreen
import com.example.adopt_pet_app.ui.screen.post.PostDetailScreen
import com.example.adopt_pet_app.ui.screen.post.PostScreen
import com.example.adopt_pet_app.ui.screen.post.PostViewModel
import com.example.adopt_pet_app.ui.screen.profile.EditProfileScreen
import com.example.adopt_pet_app.ui.screen.profile.ProfileScreen
import com.example.adopt_pet_app.ui.screen.signup.SignUpScreen

object Routes {
    const val AUTH_GATE = "auth_gate"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SEARCH = "search"
    const val CAMERA = "camera"
    const val CATEGORY = "category/{type}"
    const val POST_DETAIL = "post/{id}"
    const val PROFILE = "profile/{userId}"
    const val POST_CREATE = "post_create"
    const val POST_EDIT = "post_edit/{postId}"
    const val POST_DETAIL_NEW = "postDetail/{postId}"
    const val EDIT_PROFILE = "editProfile/{userId}"

    // Chat routes
    const val MESSENGER = "messenger"
    const val CHAT = "chat/{userId}"
    const val NEW_CHAT = "new_chat"

    // Helper functions
    fun getProfileRoute(userId: String) = "profile/$userId"
    fun getCategoryRoute(type: String) = "category/$type"
    fun getPostDetailRoute(id: String) = "post/$id"
    fun getPostDetailNewRoute(postId: String) = "postDetail/$postId"
    fun getPostEditRoute(postId: String) = "post_edit/$postId"
    fun getEditProfileRoute(userId: String) = "editProfile/$userId"
    fun getChatRoute(userId: String) = "chat/$userId"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.AUTH_GATE) {

        composable(Routes.AUTH_GATE) {
            AuthGateScreen(navController)
        }


        // ============ AUTH SCREENS ============

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
            OnboardingScreen(
                onGetStartedClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ============ MAIN SCREENS ============

        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(Routes.CAMERA) {
            CameraScreen(navController = navController)
        }

        composable(
            route = Routes.CATEGORY,
            arguments = listOf(navArgument("type") { defaultValue = "All" })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "All"
            // CategoryScreen(type, navController)
        }

        // ============ PROFILE SCREENS ============

        composable(
            route = Routes.PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileScreen(userId = userId, navController = navController)
        }

        composable(
            route = Routes.EDIT_PROFILE
        ) {
            EditProfileScreen(navController = navController)
        }

        // ============ POST SCREENS ============

        composable(Routes.POST_CREATE) {
            PostScreen(
                navController = navController,
                onPostSuccess = {
                    navController.popBackStack()
                },
                postIdToEdit = null
            )
        }

        composable(
            route = Routes.POST_EDIT,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostScreen(
                navController = navController,
                onPostSuccess = {
                    navController.popBackStack()
                },
                postIdToEdit = postId
            )
        }

        composable(
            route = Routes.POST_DETAIL_NEW,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            val postViewModel: PostViewModel = viewModel()

            PostDetailScreen(
                postId = postId,
                postViewModel = postViewModel,
                onNavigateToEdit = { id ->
                    navController.navigate(Routes.getPostEditRoute(id))
                },
                onNavigateToChat = { userId ->
                    // Navigate thẳng đến chat với user đó
                    navController.navigate(Routes.getChatRoute(userId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ============ CHAT SCREENS ============

        composable(Routes.MESSENGER) {
            val chatViewModel: ChatViewModel = viewModel()
            MessengerScreen(
                navController = navController,
                chatViewModel = chatViewModel
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val chatViewModel: ChatViewModel = viewModel()

            ChatScreen(
                userId = userId,
                navController = navController,
                chatViewModel = chatViewModel
            )
        }

        composable(Routes.NEW_CHAT) {
            // NewChatScreen - danh sách users để bắt đầu chat mới
            // Có thể implement sau
        }
    }
}