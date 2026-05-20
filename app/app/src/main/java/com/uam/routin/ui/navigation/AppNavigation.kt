package com.uam.routin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uam.routin.data.model.DeploymentMode
import com.uam.routin.ui.screens.MainDashboardScreen
import com.uam.routin.ui.screens.onboarding.CloudOAuthMockScreen
import com.uam.routin.ui.screens.onboarding.DeploymentSelectionScreen
import com.uam.routin.ui.screens.onboarding.LocalNetworkConfigScreen
import com.uam.routin.ui.screens.onboarding.WelcomeScreen
import com.uam.routin.viewmodel.RoutInViewModel

/** Navigation route constants for UI-testing and deep-link clarity. */
object Routes {
    const val WELCOME = "welcome"
    const val DEPLOYMENT_SELECTION = "deployment_selection"
    const val LOCAL_NET_CONFIG = "local_net_config"
    const val CLOUD_OAUTH = "cloud_oauth"
    const val DASHBOARD = "dashboard"
}

/**
 * Root navigation host for the Rout-In MVP.
 * A single [RoutInViewModel] instance is scoped to the NavHost so all screens
 * share the same in-memory state machine — consistent with SPEC01 §8 constraints.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: RoutInViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onAuthenticate = {
                    viewModel.onAuthenticate()
                    navController.navigate(Routes.DEPLOYMENT_SELECTION)
                }
            )
        }

        composable(Routes.DEPLOYMENT_SELECTION) {
            DeploymentSelectionScreen(
                onSelectSelfHosted = {
                    viewModel.onSelectDeploymentMode(DeploymentMode.OPEN_SOURCE_SELF_HOSTED)
                    navController.navigate(Routes.LOCAL_NET_CONFIG)
                },
                onSelectCloud = {
                    viewModel.onSelectDeploymentMode(DeploymentMode.PREMIUM_CLOUD_SUBSCRIPTION)
                    navController.navigate(Routes.CLOUD_OAUTH)
                }
            )
        }

        composable(Routes.LOCAL_NET_CONFIG) {
            LocalNetworkConfigScreen(
                onSaveUri = { uri ->
                    viewModel.onSaveMcpUri(uri)
                    navController.navigate(Routes.DASHBOARD) {
                        // Clear the onboarding backstack — user cannot go back to welcome
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CLOUD_OAUTH) {
            CloudOAuthMockScreen(
                onContinue = {
                    viewModel.onCloudOAuthContinue()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            MainDashboardScreen(viewModel = viewModel)
        }
    }
}
