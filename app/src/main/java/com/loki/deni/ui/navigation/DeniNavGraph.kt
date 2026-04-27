package com.loki.deni.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.loki.deni.ui.screens.AuthPinNewScreen
import com.loki.deni.ui.screens.AuthProfileSetupScreen
import com.loki.deni.ui.screens.AuthSuccessScreen
import com.loki.deni.ui.screens.AuthPhoneScreen
import com.loki.deni.ui.screens.AppLockScreen
import com.loki.deni.ui.screens.BorrowAmountScreen
import com.loki.deni.ui.screens.BorrowReviewScreen
import com.loki.deni.ui.screens.BorrowSummaryScreen
import com.loki.deni.ui.screens.BorrowTenureScreen
import com.loki.deni.ui.screens.HistoryDetailScreen
import com.loki.deni.ui.screens.HistoryScreen
import com.loki.deni.ui.screens.HomeScreen
import com.loki.deni.ui.screens.InsightsScreen
import com.loki.deni.ui.screens.KycStatusScreen
import com.loki.deni.ui.screens.KycScreen
import com.loki.deni.ui.screens.LinkedAccountsScreen
import com.loki.deni.ui.screens.LoanDetailScreen
import com.loki.deni.ui.screens.LoanScheduleScreen
import com.loki.deni.ui.screens.LoanTopupScreen
import com.loki.deni.ui.screens.LoginActivityScreen
import com.loki.deni.ui.screens.LoansScreen
import com.loki.deni.ui.screens.NotificationsScreen
import com.loki.deni.ui.screens.OnboardingScreen
import com.loki.deni.ui.screens.PaymentReceiptScreen
import com.loki.deni.ui.screens.ProfileScreen
import com.loki.deni.ui.screens.ReferralScreen
import com.loki.deni.ui.screens.RepayAmountScreen
import com.loki.deni.ui.screens.RepayConfirmScreen
import com.loki.deni.ui.screens.RepayScreen
import com.loki.deni.ui.screens.RepaySuccessScreen
import com.loki.deni.ui.screens.SettingsScreen
import com.loki.deni.ui.screens.SplashScreen
import com.loki.deni.ui.screens.SupportScreen
import com.loki.deni.ui.screens.TransactionsScreen
import com.loki.deni.ui.screens.TransactionsFeedScreen
import com.loki.deni.ui.screens.LoanTopupReviewScreen
import com.loki.deni.ui.screens.SuccessScreen
import com.loki.deni.ui.screens.SummaryScreen
import com.loki.deni.ui.screens.WelcomeScreen
import com.loki.deni.ui.screens.ChangePinScreen
import com.loki.deni.ui.screens.CloseAccountScreen
import com.loki.deni.ui.screens.EditProfileScreen
import com.loki.deni.ui.screens.LoanSuccessScreen
import com.loki.deni.ui.screens.PaymentMethodsScreen
import com.loki.deni.ui.screens.PersonalInfoScreen
import com.loki.deni.ui.screens.PricingScreen
import com.loki.deni.ui.screens.SecurityScreen
import com.loki.deni.ui.screens.SupportChatScreen
import com.loki.deni.ui.screens.SupportTicketScreen
import com.loki.deni.ui.screens.apply.ApplyScreen
import androidx.navigation.navDeepLink

@Composable
fun DeniNavGraph(
    isDarkTheme: Boolean,
    navController: NavHostController,
    onThemeToggle: (Boolean) -> Unit,
    startDestination: String = "splash",
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable(Routes.WELCOME) {
            WelcomeScreen(navController = navController)
        }
        composable("onboarding") {
            OnboardingScreen(navController = navController)
        }
        composable(Routes.AUTH_PHONE) {
            AuthPhoneScreen(navController = navController)
        }
        composable(
            route = Routes.AUTH_PIN_NEW,
            arguments = listOf(navArgument("phone") { type = NavType.StringType }),
        ) { backStackEntry ->
            AuthPinNewScreen(
                navController = navController,
                phone = backStackEntry.arguments?.getString("phone").orEmpty(),
            )
        }
        composable(Routes.AUTH_PROFILE_SETUP) {
            AuthProfileSetupScreen(navController = navController)
        }
        composable(Routes.AUTH_SUCCESS) {
            AuthSuccessScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(
                navController = navController,
            )
        }
        composable(
            route = "transactions?focus={focus}",
            arguments = listOf(
                navArgument("focus") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
            ),
        ) { backStackEntry ->
            TransactionsScreen(
                navController = navController,
                focus = backStackEntry.arguments?.getString("focus"),
            )
        }
        composable("transactions_feed") {
            TransactionsFeedScreen(navController = navController)
        }
        composable(
            route = "loans",
            deepLinks = listOf(navDeepLink { uriPattern = "deni://loans/apply" }),
        ) { LoansScreen(navController = navController) }
        composable("apply") {
            ApplyScreen(
                navController = navController,
            )
        }
        composable("borrow_amount") { BorrowAmountScreen(navController = navController) }
        composable(
            route = "borrow_tenure/{amount}",
            arguments = listOf(navArgument("amount") { type = NavType.IntType }),
        ) { backStackEntry ->
            BorrowTenureScreen(
                navController = navController,
                amount = backStackEntry.arguments?.getInt("amount") ?: 15000,
            )
        }
        composable(
            route = "borrow_summary/{amount}/{tenure}",
            arguments = listOf(
                navArgument("amount") { type = NavType.IntType },
                navArgument("tenure") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            BorrowSummaryScreen(
                navController = navController,
                amount = backStackEntry.arguments?.getInt("amount") ?: 15000,
                tenure = backStackEntry.arguments?.getInt("tenure") ?: 3,
            )
        }
        composable(
            route = "borrow_review/{amount}/{tenure}/{loanType}",
            arguments = listOf(
                navArgument("amount") { type = NavType.IntType },
                navArgument("tenure") { type = NavType.IntType },
                navArgument("loanType") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            BorrowReviewScreen(
                navController = navController,
                amount = backStackEntry.arguments?.getInt("amount") ?: 15000,
                tenure = backStackEntry.arguments?.getInt("tenure") ?: 3,
                loanType = backStackEntry.arguments?.getString("loanType") ?: "Personal",
            )
        }
        composable(
            route = "borrow_success/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType }),
        ) { backStackEntry ->
            LoanSuccessScreen(
                navController = navController,
                loanId = backStackEntry.arguments?.getInt("loanId") ?: 1,
            )
        }
        composable("borrow_success") {
            LoanSuccessScreen(navController = navController, loanId = 1)
        }
        composable(
            route = "summary/{amount}/{tenure}",
            arguments = listOf(
                navArgument("amount") { type = NavType.IntType },
                navArgument("tenure") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getInt("amount") ?: 0
            val tenure = backStackEntry.arguments?.getInt("tenure") ?: 3
            SummaryScreen(
                navController = navController,
                amount = amount,
                tenure = tenure,
            )
        }
        composable(
            route = "success/{amount}",
            arguments = listOf(navArgument("amount") { type = NavType.IntType }),
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getInt("amount") ?: 0
            SuccessScreen(
                navController = navController,
                amount = amount,
            )
        }
        composable("repay") {
            RepayScreen(navController = navController, loanId = 0)
        }
        composable(
            route = "repay/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = "deni://repay/{loanId}" }),
        ) { backStackEntry ->
            RepayScreen(navController = navController, loanId = backStackEntry.arguments?.getInt("loanId") ?: 0)
        }
        composable(
            route = "repay_amount/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType }),
        ) { backStackEntry ->
            RepayAmountScreen(
                navController = navController,
                loanId = backStackEntry.arguments?.getInt("loanId") ?: 0,
            )
        }
        composable(
            route = "repay_confirm/{loanId}/{amount}",
            arguments = listOf(
                navArgument("loanId") { type = NavType.IntType },
                navArgument("amount") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            RepayConfirmScreen(
                navController = navController,
                loanId = backStackEntry.arguments?.getInt("loanId") ?: 0,
                amount = backStackEntry.arguments?.getInt("amount") ?: 0,
            )
        }
        composable(
            route = "repay_success/{loanId}/{receiptRef}",
            arguments = listOf(
                navArgument("loanId") { type = NavType.IntType },
                navArgument("receiptRef") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            RepaySuccessScreen(
                navController = navController,
                loanId = backStackEntry.arguments?.getInt("loanId") ?: 1,
                receiptRef = backStackEntry.arguments?.getString("receiptRef").orEmpty(),
            )
        }
        composable(
            route = "payment_receipt/{txId}",
            arguments = listOf(navArgument("txId") { type = NavType.IntType }),
        ) { backStackEntry ->
            PaymentReceiptScreen(
                navController = navController,
                txId = backStackEntry.arguments?.getInt("txId") ?: 0,
            )
        }
        composable(
            route = "receipt/{paymentId}",
            arguments = listOf(navArgument("paymentId") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = "deni://receipt/{paymentId}" }),
        ) { backStackEntry ->
            PaymentReceiptScreen(
                navController = navController,
                txId = backStackEntry.arguments?.getInt("paymentId") ?: 0,
            )
        }
        composable("history") {
            HistoryScreen(navController = navController)
        }
        composable(
            route = "history_detail/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType }),
        ) { backStackEntry ->
            HistoryDetailScreen(navController = navController, loanId = backStackEntry.arguments?.getInt("loanId") ?: 1)
        }
        composable(
            route = "loan_detail/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType }),
        ) { backStackEntry ->
            LoanDetailScreen(
                navController = navController,
                loanId = backStackEntry.arguments?.getInt("loanId") ?: 1,
            )
        }
        composable(
            route = "loan_schedule/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType }),
        ) { backStackEntry ->
            LoanScheduleScreen(
                navController = navController,
                loanId = backStackEntry.arguments?.getInt("loanId") ?: 1,
            )
        }
        composable(
            route = "loan_topup/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.IntType }),
        ) { backStackEntry ->
            LoanTopupScreen(
                navController = navController,
                loanId = backStackEntry.arguments?.getInt("loanId") ?: 1,
            )
        }
        composable(
            route = "loan_topup_review/{loanId}/{amount}/{days}/{purpose}",
            arguments = listOf(
                navArgument("loanId") { type = NavType.IntType },
                navArgument("amount") { type = NavType.IntType },
                navArgument("days") { type = NavType.IntType },
                navArgument("purpose") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            LoanTopupReviewScreen(
                navController = navController,
                loanId = backStackEntry.arguments?.getInt("loanId") ?: 0,
                amount = backStackEntry.arguments?.getInt("amount") ?: 0,
                days = backStackEntry.arguments?.getInt("days") ?: 14,
                purpose = android.net.Uri.decode(backStackEntry.arguments?.getString("purpose").orEmpty()),
            )
        }
        composable("insights") {
            InsightsScreen(navController = navController)
        }
        composable("notifications") {
            NotificationsScreen(navController = navController)
        }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                onThemeToggle = onThemeToggle,
            )
        }
        composable("kyc_status") { KycStatusScreen(navController = navController) }
        composable("kyc") { KycScreen(navController = navController) }
        composable("referral") { ReferralScreen(navController = navController) }
        composable("linked_accounts") { LinkedAccountsScreen(navController = navController) }
        composable("change_pin") { ChangePinScreen(navController = navController) }
        composable("login_activity") { LoginActivityScreen(navController = navController) }
        composable("app_lock") { AppLockScreen(onUnlock = { navController.navigateUp() }) }
        composable("support") { SupportScreen(navController = navController) }
        composable("support_chat") { SupportChatScreen(navController = navController) }
        composable("support_ticket") { SupportTicketScreen(navController = navController) }
        composable("settings") { SettingsScreen(navController = navController) }
        composable("edit_profile") { EditProfileScreen(navController = navController) }
        composable("personal_info") { PersonalInfoScreen(navController = navController) }
        composable("payment_methods") { PaymentMethodsScreen(navController = navController) }
        composable("pricing") { PricingScreen(navController = navController) }
        composable("security") { SecurityScreen(navController = navController) }
        composable("close_account") { CloseAccountScreen(navController = navController) }
    }
}
