package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToCategorySettings: () -> Unit,
    onNavigateToTagSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
    // <<--- onNavigateBack 파라미터 제거 ---
) {
    var showClearDataDialog by remember { mutableStateOf(false) }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("This will permanently delete all transactions, categories, and tags. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onClearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }, // <<--- 제목 수정
                navigationIcon = { } // <<--- 뒤로가기 버튼 제거 (빈 값으로 둠)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToCategorySettings)
            ) {
                Text(text = "Manage Categories", modifier = Modifier.padding(16.dp))
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToTagSettings)
            ) {
                Text(text = "Manage Tags", modifier = Modifier.padding(16.dp))
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearDataDialog = true },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = "Reset All Data",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}