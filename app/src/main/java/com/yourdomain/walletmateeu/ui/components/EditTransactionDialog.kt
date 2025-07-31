package com.yourdomain.walletmateeu.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.yourdomain.walletmateeu.R
import com.yourdomain.walletmateeu.data.local.model.CategoryEntity
import com.yourdomain.walletmateeu.data.local.model.TagEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionEntity
import com.yourdomain.walletmateeu.data.local.model.TransactionWithCategoryAndTags
import com.yourdomain.walletmateeu.util.FileUtil
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transactionWithCategoryAndTags: TransactionWithCategoryAndTags,
    allCategories: List<CategoryEntity>,
    allTags: List<TagEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity, List<TagEntity>) -> Unit,
    onDelete: (String) -> Unit
) {
    val transactionEntity = transactionWithCategoryAndTags.transaction
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var title by remember { mutableStateOf(transactionEntity.title) }
    var amount by remember { mutableStateOf(String.format(Locale.US, "%.2f", transactionEntity.amount)) }
    var selectedCategory by remember { mutableStateOf(transactionWithCategoryAndTags.category) }
    val selectedTags = remember { mutableStateListOf<TagEntity>().also { it.addAll(transactionWithCategoryAndTags.tags) } }
    var date by remember { mutableStateOf(transactionEntity.date) }
    var imageUri by remember { mutableStateOf(transactionEntity.imageUri?.let { Uri.parse(it) }) }
    var isDatePickerVisible by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> imageUri = uri }
    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) { success -> if (success) { imageUri = tempCameraUri } }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted -> if (isGranted) { val uri = FileUtil.getTmpFileUri(context); tempCameraUri = uri; cameraLauncher.launch(uri) } }

    if (isDatePickerVisible) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val correctedDate = it + TimeZone.getDefault().getOffset(it)
                        date = correctedDate
                    }
                    isDatePickerVisible = false
                }) { Text(stringResource(R.string.dialog_ok)) }
            },
            dismissButton = { TextButton(onClick = { isDatePickerVisible = false }) { Text(stringResource(R.string.dialog_cancel)) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_transaction_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.transaction_title_label)) })
                OutlinedTextField(value = amount, onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*(\\.\\d{0,2})?\$"))) { amount = it } }, label = { Text(stringResource(R.string.transaction_amount_label)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                ClickableFakeTextField(value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date)), label = stringResource(R.string.transaction_date_label), onClick = { isDatePickerVisible = true }, trailingIcon = Icons.Default.DateRange)
                CategoryDropDown(categories = allCategories.filter { it.type == transactionEntity.type }, selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })

                Text(stringResource(R.string.transaction_tags_label), style = MaterialTheme.typography.titleSmall)
                FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSpacing = 8.dp, crossAxisSpacing = 4.dp) {
                    allTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(selected = isSelected, onClick = { if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag) }, label = { Text(tag.name) })
                    }
                }

                Text(stringResource(R.string.receipt_label), style = MaterialTheme.typography.titleSmall)
                if (imageUri != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = stringResource(R.string.receipt_image_desc), modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        IconButton(onClick = { imageUri = null }, modifier = Modifier.align(Alignment.TopEnd)) {
                            Icon(Icons.Default.Close, stringResource(R.string.remove_image_desc), tint = Color.White, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape))
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.gallery_button))
                        }
                        OutlinedButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val uri = FileUtil.getTmpFileUri(context)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.camera_button))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalImageUri = imageUri?.let { uri ->
                        if (uri.scheme != "content" || !uri.path.orEmpty().contains(context.packageName)) {
                            FileUtil.copyUriToInternalStorage(context, uri, "receipts")
                        } else {
                            uri
                        }
                    }
                    val updatedTransaction = transactionEntity.copy(
                        title = title,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        categoryId = selectedCategory?.id,
                        date = date,
                        imageUri = finalImageUri?.toString(),
                        lastModified = System.currentTimeMillis()
                    )
                    onConfirm(updatedTransaction, selectedTags.toList())
                },
                enabled = title.isNotBlank() && amount.isNotBlank()
            ) { Text(stringResource(R.string.transaction_save)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onDelete(transactionEntity.id) }) {
                    Text(stringResource(R.string.dialog_delete), color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
            }
        }
    )
}