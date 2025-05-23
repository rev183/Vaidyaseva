package com.mrknti.vaidyaseva.ui.onboarding

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.UserDocumentType
import com.mrknti.vaidyaseva.filehandling.getCameraOutputUri
import com.mrknti.vaidyaseva.filehandling.getMediaData
import com.mrknti.vaidyaseva.ui.components.DatePickerDialog
import com.mrknti.vaidyaseva.ui.components.ProgressIndicatorMedium
import com.mrknti.vaidyaseva.util.DateFormat
import com.mrknti.vaidyaseva.util.formatDate
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Date

@Composable
fun DocumentUpload(onFinishClick: () -> Unit, navigateToFullScreenImage: (String) -> Unit) {

    val viewModel: DocumentUploadViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    var openAlertDialog by remember { mutableIntStateOf(-1) }
    var fileSelectedFor: Int? by remember { mutableStateOf(null) }
    var cameraOutputUri: Uri? by remember { mutableStateOf(null) }
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = {
            if (fileSelectedFor == UserDocumentType.PASSPORT) {
                viewModel.setImageUri(it, UserDocumentType.PASSPORT)
            } else {
                viewModel.setImageUri(it, UserDocumentType.VISA)
            }
        })
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = {
            if (!it && cameraOutputUri == null) {
                return@rememberLauncherForActivityResult
            }
            if (fileSelectedFor == UserDocumentType.PASSPORT) {
                viewModel.setImageUri(listOf(cameraOutputUri!!), UserDocumentType.PASSPORT)
            } else {
                viewModel.setImageUri(listOf(cameraOutputUri!!), UserDocumentType.VISA)
            }
        }
    )
    val localContext = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        when (openAlertDialog) {
            UserDocumentType.PASSPORT -> {
                FileDestinationAlertDialog(
                    onDismissRequest = { openAlertDialog = -1 },
                    onConfirmation = { sourceType ->
                        openAlertDialog = -1
                        fileSelectedFor = UserDocumentType.PASSPORT
                        when (sourceType) {
                            FileSourceType.CAMERA -> {
                                cameraOutputUri = getCameraOutputUri(localContext)
                                cameraLauncher.launch(cameraOutputUri)
                            }
                            FileSourceType.GALLERY -> {
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                    }
                )
            }
            UserDocumentType.VISA -> {
                FileDestinationAlertDialog(
                    onDismissRequest = { openAlertDialog = -1 },
                    onConfirmation = { sourceType ->
                        openAlertDialog = -1
                        fileSelectedFor = UserDocumentType.VISA
                        when (sourceType) {
                            FileSourceType.CAMERA -> {
                                cameraOutputUri = getCameraOutputUri(localContext)
                                cameraLauncher.launch(cameraOutputUri)
                            }
                            FileSourceType.GALLERY -> {
                                imagePickerLauncher.launch("image/*")
                            }
                        }
                    }
                )
            }
            else -> {}
        }

        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp)) {
            Text(
                text = "Upload Documents",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                style = TextStyle(fontSize = 32.sp, fontFamily = FontFamily.Cursive)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Upload ${viewModel.user.displayName}'s documents",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            UploadDocument(
                docType = UserDocumentType.PASSPORT,
                imageData = viewState.passportData,
                expiry = viewState.passportExpiry,
                token = viewModel.authToken,
                uploadStatus = viewState.passportStatus,
                modifier = Modifier.fillMaxWidth(),
                onSelectDocumentClick = { type ->
                    openAlertDialog = type
                },
                onUploadClick = { type, expiry ->
                    val mediaData = viewState.passportData.filterIsInstance<Uri>().mapNotNull {
                        it.getMediaData(localContext)
                    }
                    if (mediaData.isNotEmpty()) {
                        viewModel.uploadDocument(type, expiry, mediaData)
                    } else {
                        Toast.makeText(localContext, "No image to upload", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                onImageClick = {
                    val imageData = viewState.passportData[it]
                    val url = if (imageData is Uri) {
                        imageData.toString()
                    } else {
                        imageData as String
                    }
                    val urlE = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navigateToFullScreenImage(urlE)
                },
                onClearDocument = viewModel::clearDocument
            )
            Spacer(modifier = Modifier.height(16.dp))
            UploadDocument(
                docType = UserDocumentType.VISA,
                imageData = viewState.visaData,
                expiry = viewState.visaExpiry,
                token = viewModel.authToken,
                uploadStatus = viewState.visaStatus,
                modifier = Modifier.fillMaxWidth(),
                onSelectDocumentClick = { type ->
                    openAlertDialog = type
                },
                onUploadClick = { type, expiry ->
                    val mediaData = viewState.visaData.filterIsInstance<Uri>().mapNotNull {
                        it.getMediaData(localContext)
                    }
                    if (mediaData.isNotEmpty()) {
                        viewModel.uploadDocument(type, expiry, mediaData)
                    } else {
                        Toast.makeText(localContext, "No image to upload", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                onImageClick = {
                    val imageData = viewState.visaData[it]
                    val url = if (imageData is Uri) {
                        imageData.toString()
                    } else {
                        imageData as String
                    }
                    val urlE = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
                    navigateToFullScreenImage(urlE)
                },
                onClearDocument = viewModel::clearDocument
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        onFinishClick()
                    }
                ) {
                    Text(text = "Done")
                }
            }
        }
    }

}

@Composable
fun UploadDocument(
    docType: Int,
    imageData: List<Any>,
    expiry: Date?,
    token: String,
    modifier: Modifier = Modifier,
    uploadStatus: UploadStatus,
    onSelectDocumentClick: (Int) -> Unit,
    onUploadClick: (Int, Date) -> Unit,
    onImageClick: (Int) -> Unit,
    onClearDocument: (Int, Int) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var expireDate: Date? by remember { mutableStateOf(expiry) }
    val context = LocalContext.current

    if (showDatePicker) {
        DatePickerDialog(onDismiss = { showDatePicker = false }, onDateSelected = {
            showDatePicker = false
            expireDate = it
        })
    }

    Column(modifier = modifier.fillMaxWidth()) {
        IconButton(onClick = { onSelectDocumentClick(docType) }, modifier = Modifier.fillMaxWidth()) {
            Row {
                Text(
                    text = "Select ${if (docType == UserDocumentType.PASSPORT) "Passport" else "Visa"} Image",
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(painter = painterResource(id = R.drawable.attach_file_24), contentDescription = null)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (imageData.isNotEmpty()) {

            ImageGallery(
                imageData = imageData,
                token = token,
                onImageClick = onImageClick,
                onClearDocument = {
                    onClearDocument(docType, it)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            val displayExpiry = expireDate ?: expiry
            val expiryText = if (displayExpiry != null) {
                "Expiry: ${displayExpiry.formatDate(DateFormat.DAY_MONTH_YEAR)}"
            } else {
                "Expiry: Not Set"
            }
            TextButton(onClick = { showDatePicker = true }) {
                Text(text = expiryText)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = {
                    if (expireDate != null) {
                        onUploadClick(docType, expireDate!!)
                    } else {
                        Toast.makeText(context, "Please select expiry date", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.upload_24),
                            contentDescription = null
                        )
                        Text(
                            text = "Upload",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                when(uploadStatus) {
                    UploadStatus.UPLOADING -> {
                        ProgressIndicatorMedium()
                    }
                    UploadStatus.UPLOADED -> {
                        Icon(
                            painter = painterResource(id = R.drawable.check_circle_24),
                            contentDescription = "Check done",
                            tint = Color.Green
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ImageGallery(
    imageData: List<Any>,
    token: String,
    showClose: Boolean = true,
    onImageClick: (Int) -> Unit,
    onClearDocument: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val imageModels by remember(imageData) { derivedStateOf {
        imageData.map {
            if (it is Uri) {
                it
            } else {
                ImageRequest.Builder(context)
                    .data(it)
                    .addHeader("token", token)
                    .build()
            }

        }
    } }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(imageModels.size) { index ->
            ImageUploadItem(
                imageModels[index],
                showClose,
                onClearDocument = { onClearDocument(index) },
                onImageClick = { onImageClick(index) })
        }
    }
}

@Composable
fun ImageUploadItem(
    imageModel: Any,
    showClose: Boolean,
    onClearDocument: () -> Unit,
    onImageClick: () -> Unit = {}
) {
    Box(modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .clickable { onImageClick() }) {
        AsyncImage(
            model = imageModel,
            contentDescription = null,
            modifier = Modifier
                .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
        )
        if (showClose) {
            IconButton(
                onClick = onClearDocument,
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.close_24),
                    contentDescription = "close icon",
                    tint = Color.Red
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDestinationAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (Int) -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .height(275.dp)
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Image Source",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleSmall
                )
                IconButton(
                    onClick = { onConfirmation(FileSourceType.CAMERA) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add_photo_24),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Take Picture", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                IconButton(
                    onClick = { onConfirmation(FileSourceType.GALLERY) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.gallery_thumbnail_24),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Select from Gallery",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                TextButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

object FileSourceType {
    const val CAMERA = 1
    const val GALLERY = 2
}