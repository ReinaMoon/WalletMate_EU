package com.yourdomain.walletmateeu.ui.feature_timeline

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.yourdomain.walletmateeu.R
import com.yourdomain.walletmateeu.ui.components.CategoryDropDown
import com.yourdomain.walletmateeu.ui.components.ClickableFakeTextField
import com.yourdomain.walletmateeu.util.FileUtil
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    onSaveCompleted: () -> Unit,
    viewModel: AddEditTransactionViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val allTags by viewModel.allTags.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val filteredCategories = remember(categories, uiState.transactionType) {
        categories.filter { it.type == uiState.transactionType }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(AddEditTransactionEvent.OnImagePicked(it)) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { viewModel.onEvent(AddEditTransactionEvent.OnImagePicked(it)) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = FileUtil.getTmpFileUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            // Handle permission denial if needed
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.events.collect { event ->
            if (event is AddEditTransactionViewModel.UiEvent.SaveSuccess) {
                onSaveCompleted()
            }
        }
    }

    if (uiState.isDatePickerDialogVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.date)
        DatePickerDialog(
            onDismissRequest = { viewModel.onEvent(AddEditTransactionEvent.OnDatePickerDismiss) },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val correctedDate = it + TimeZone.getDefault().getOffset(it)
                        viewModel.onEvent(AddEditTransactionEvent.OnDateSelected(correctedDate))
                    }
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = { TextButton(onClick = { viewModel.onEvent(AddEditTransactionEvent.OnDatePickerDismiss) }) { Text(stringResource(R.string.dialog_cancel)) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    if (uiState.isEditMode) stringResource(R.string.edit_transaction_title)
                    else stringResource(R.string.add_transaction_title)
                )
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (!uiState.isSaving) {
                    uiState.imageUri?.let {
                        if (it.scheme != "content" || !it.path.orEmpty().contains(context.packageName)) {
                            val newUri = FileUtil.copyUriToInternalStorage(context, it, "receipts")
                            viewModel.onEvent(AddEditTransactionEvent.OnImagePicked(newUri))
                        }
                    }
                    viewModel.onEvent(AddEditTransactionEvent.OnSaveClick)
                }
            }) {
                if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text(stringResource(R.string.transaction_save))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row {
                FilterChip(selected = uiState.transactionType == "EXPENSE", onClick = { viewModel.onEvent(AddEditTransactionEvent.OnTypeChange("EXPENSE")) }, label = { Text(stringResource(R.string.transaction_expense_type)) })
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(selected = uiState.transactionType == "INCOME", onClick = { viewModel.onEvent(AddEditTransactionEvent.OnTypeChange("INCOME")) }, label = { Text(stringResource(R.string.transaction_income_type)) })
            }
            OutlinedTextField(value = uiState.title, onValueChange = { viewModel.onEvent(AddEditTransactionEvent.OnTitleChange(it)) }, label = { Text(stringResource(R.string.transaction_title_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            OutlinedTextField(value = uiState.amount, onValueChange = { viewModel.onEvent(AddEditTransactionEvent.OnAmountChange(it)) }, label = { Text(stringResource(R.string.transaction_amount_label)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done), modifier = Modifier.fillMaxWidth(), singleLine = true)
            ClickableFakeTextField(value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(uiState.date)), label = stringResource(R.string.transaction_date_label), onClick = { viewModel.onEvent(AddEditTransactionEvent.OnDateClick) }, trailingIcon = Icons.Default.DateRange)
            CategoryDropDown(categories = filteredCategories, selectedCategory = uiState.selectedCategory, onCategorySelected = { category -> viewModel.onEvent(AddEditTransactionEvent.OnCategorySelect(category)) })
            Text(stringResource(R.string.transaction_tags_label), style = MaterialTheme.typography.titleMedium)
            FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSpacing = 8.dp, crossAxisSpacing = 4.dp) {
                allTags.forEach { tag ->
                    val isSelected = uiState.selectedTags.contains(tag)
                    FilterChip(selected = isSelected, onClick = { viewModel.onEvent(AddEditTransactionEvent.OnTagSelected(tag)) }, label = { Text(tag.name) })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.receipt_label), style = MaterialTheme.typography.titleMedium)

            if (uiState.imageUri != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.imageUri),
                        contentDescription = stringResource(R.string.receipt_image_desc),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.onEvent(AddEditTransactionEvent.OnRemoveImage) },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, stringResource(R.string.remove_image_desc), tint = Color.White, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape))
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.gallery_button))
                    }
                    OutlinedButton(
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                    val uri = FileUtil.getTmpFileUri(context)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                                else -> {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.camera_button))
                    }
                }
            }
        }
    }
}