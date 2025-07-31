package com.yourdomain.walletmateeu.ui.feature_dashboard

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.yourdomain.walletmateeu.R
import com.yourdomain.walletmateeu.util.IconHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    viewModel: TransactionDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactionDetails = uiState.transaction

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transaction_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_desc))
                    }
                },
                actions = {
                    IconButton(onClick = { transactionDetails?.transaction?.id?.let { onNavigateToEdit(it) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Transaction")
                    }
                    IconButton(onClick = { /* TODO: Delete action */ }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Transaction")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (transactionDetails != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Image
                item {
                    transactionDetails.transaction.imageUri?.let {
                        Image(
                            painter = rememberAsyncImagePainter(model = Uri.parse(it)),
                            contentDescription = stringResource(R.string.receipt_image_desc),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Title and Amount
                item {
                    Column {
                        Text(transactionDetails.transaction.title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.US, "%.2f %s", transactionDetails.transaction.amount, uiState.currency),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (transactionDetails.transaction.type == "EXPENSE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Details Section (Date, Category, Tags)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Date
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(transactionDetails.transaction.date)),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        // Category
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val category = transactionDetails.category
                            val categoryColor = category?.color?.let { try { Color(android.graphics.Color.parseColor(it)) } catch (e: Exception) { Color.Gray } } ?: Color.Gray
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(categoryColor), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = category?.icon?.let { IconHelper.getIcon(it) } ?: Icons.Default.Category,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(category?.name ?: stringResource(R.string.transaction_uncategorized), style = MaterialTheme.typography.bodyLarge)
                        }
                        // Tags
                        if (transactionDetails.tags.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Label, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                                    transactionDetails.tags.forEach { tag ->
                                        val tagColor = try { Color(android.graphics.Color.parseColor(tag.color)) } catch (e: Exception) { MaterialTheme.colorScheme.secondaryContainer }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = tagColor,
                                            contentColor = Color.White
                                        ) {
                                            Text(text = "#${tag.name}", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}