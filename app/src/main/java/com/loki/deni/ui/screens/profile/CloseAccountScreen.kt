package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CloseAccountScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    var input by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = "Close Account", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "This action is irreversible. Type CLOSE to confirm.",
                modifier = Modifier.fillMaxWidth().background(Color(0xFFFDEAEA), RoundedCornerShape(12.dp)).padding(12.dp),
                color = Color(0xFFB00020),
            )
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("Type CLOSE") }, modifier = Modifier.fillMaxWidth())
            DeniButton(
                text = "Submit closure request",
                enabled = input == "CLOSE",
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        authViewModel.requestClosure()
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            )
        }
    }
}
