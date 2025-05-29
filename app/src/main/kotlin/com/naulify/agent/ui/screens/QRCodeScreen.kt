package com.naulify.agent.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naulify.agent.ui.components.*
import com.naulify.agent.viewmodel.ProfileViewModel
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScreen(
    onNavigateBack: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val vehicles by profileViewModel.vehicles.collectAsState()
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(vehicles) {
        vehicles.firstOrNull()?.let { vehicle ->
            val qrContent = "https://naulify.com/pay/${vehicle.id}"
            withContext(Dispatchers.Default) {
                try {
                    val qrCodeWriter = QRCodeWriter()
                    val bitMatrix = qrCodeWriter.encode(
                        qrContent,
                        BarcodeFormat.QR_CODE,
                        512,
                        512
                    )
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                        }
                    }
                    qrCodeBitmap = bitmap
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }

    Scaffold(
        topBar = {
            NaulifyTopBar(
                title = "QR Code",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        // TODO: Implement sharing functionality
                    }) {
                        Icon(Icons.Default.Share, "Share QR Code")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .size(300.dp)
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    qrCodeBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(250.dp)
                                .padding(16.dp)
                        )
                    } ?: CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Scan to Pay",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ask commuters to scan this QR code to pay their fare",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            vehicles.firstOrNull()?.let { vehicle ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Vehicle: ${vehicle.registration}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "M-Pesa Short Code: ${vehicle.mpesaShortCode}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
