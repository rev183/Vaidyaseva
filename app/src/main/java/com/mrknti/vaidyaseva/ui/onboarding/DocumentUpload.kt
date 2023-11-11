package com.mrknti.vaidyaseva.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mrknti.vaidyaseva.R
import com.mrknti.vaidyaseva.data.UserDocumentType
import com.mrknti.vaidyaseva.filehandling.getCameraOutputUri
import com.mrknti.vaidyaseva.filehandling.getMediaData

@Composable
fun DocumentUpload(onFinishClick: () -> Unit) {

    val viewModel: DocumentUploadViewModel = viewModel()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    var openAlertDialog by remember { mutableIntStateOf(-1) }
    var fileSelectedFor: Int? by remember { mutableStateOf(null) }
    var cameraOutputUri: Uri? by remember { mutableStateOf(null) }
    val scrollState = rememberScrollState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
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
            if (fileSelectedFor == UserDocumentType.PASSPORT) {
                viewModel.setImageUri(cameraOutputUri, UserDocumentType.PASSPORT)
            } else {
                viewModel.setImageUri(cameraOutputUri, UserDocumentType.VISA)
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
                imageUri = viewState.passportUri,
                isDocumentUploaded = viewState.isPassportUploaded,
                modifier = Modifier.fillMaxWidth(),
                onSelectDocumentClick = { type ->
                    openAlertDialog = type
                },
                onUploadClick = {
                    val mediaData = viewState.passportUri?.getMediaData(localContext)
                    if (mediaData != null) {
                        viewModel.uploadDocument(it, mediaData)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            UploadDocument(
                docType = UserDocumentType.VISA,
                imageUri = viewState.visaUri,
                isDocumentUploaded = viewState.isVisaUploaded,
                modifier = Modifier.fillMaxWidth(),
                onSelectDocumentClick = { type ->
                    openAlertDialog = type
                },
                onUploadClick = {
                    val mediaData = viewState.visaUri?.getMediaData(localContext)
                    if (mediaData != null) {
                        viewModel.uploadDocument(it, mediaData)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(onClick = { onFinishClick() }) {
                    Text(text = "Done")
                }
            }
        }
    }

}

@Composable
fun UploadDocument(
    docType: Int,
    imageUri: Uri?,
    modifier: Modifier = Modifier,
    isDocumentUploaded: Boolean,
    onSelectDocumentClick: (Int) -> Unit,
    onUploadClick: (Int) -> Unit
) {
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
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = { onUploadClick(docType) }) {
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
                if (isDocumentUploaded) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(id = R.drawable.check_circle_24),
                        contentDescription = "Check done",
                        tint = Color.Green
                    )
                }
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
    AlertDialog(onDismissRequest = onDismissRequest) {
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