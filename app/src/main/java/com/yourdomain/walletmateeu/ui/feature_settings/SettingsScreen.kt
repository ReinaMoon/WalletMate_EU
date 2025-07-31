package com.yourdomain.walletmateeu.ui.feature_settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourdomain.walletmateeu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToCategorySettings: () -> Unit,
    onNavigateToTagSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    val supportedCurrencies = listOf("EUR", "USD", "KRW", "JPY")

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(stringResource(R.string.settings_select_currency)) },
            text = {
                LazyColumn {
                    items(supportedCurrencies) { currency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onCurrencySelected(currency)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = currentCurrency == currency, onClick = { viewModel.onCurrencySelected(currency); showCurrencyDialog = false })
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(currency)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCurrencyDialog = false }) { Text(stringResource(R.string.dialog_cancel)) } }
        )
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(stringResource(R.string.settings_clear_data_dialog_title)) },
            text = { Text(stringResource(R.string.settings_clear_data_dialog_message)) },
            confirmButton = { Button(onClick = { viewModel.onClearAllData(); showClearDataDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text(stringResource(R.string.settings_delete_all)) } },
            dismissButton = { TextButton(onClick = { showClearDataDialog = false }) { Text(stringResource(R.string.dialog_cancel)) } }
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.bottom_nav_settings)) }) }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToCategorySettings)) {
                Text(text = stringResource(R.string.settings_manage_categories), modifier = Modifier.padding(16.dp))
            }
            Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToTagSettings)) {
                Text(text = stringResource(R.string.settings_manage_tags), modifier = Modifier.padding(16.dp))
            }
            // --- SettingsScreen 함수 내부 ---
            Card(modifier = Modifier.fillMaxWidth().clickable { showCurrencyDialog = true }) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.settings_currency),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f) // Text가 남은 공간을 모두 차지
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            currentCurrency,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = stringResource(R.string.settings_select_currency)
                        )
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth().clickable { showClearDataDialog = true }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(text = stringResource(R.string.settings_reset_all_data), modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}