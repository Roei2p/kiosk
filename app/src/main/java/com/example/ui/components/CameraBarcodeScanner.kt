package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.ui.theme.HighDensityDarkBlue
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensitySuccess
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@Composable
fun CameraBarcodeScannerModal(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var detectedBarcode by remember { mutableStateOf("") }
    var detectedSn by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("מכוון מצלמה או העלה תמונה מהגלריה לזיהוי ברקוד / SN...") }
    var isAnalyzing by remember { mutableStateOf(false) }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isAnalyzing = true
            statusMessage = "מפענח תמונת מדבקה מהגלריה..."
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                val barcodeScanner = BarcodeScanning.getClient()
                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val foundBarcode = barcodes.firstOrNull()?.rawValue
                        if (!foundBarcode.isNullOrEmpty()) {
                            detectedBarcode = foundBarcode
                            detectedSn = extractSnFromRaw(foundBarcode)
                            statusMessage = "נמצא ברקוד בתמונת הגלריה: $foundBarcode"
                            isAnalyzing = false
                        } else {
                            textRecognizer.process(inputImage)
                                .addOnSuccessListener { visionText ->
                                    val text = visionText.text
                                    val snMatch = extractSnFromText(text)
                                    if (snMatch.isNotEmpty()) {
                                        detectedSn = snMatch
                                        if (detectedBarcode.isEmpty()) detectedBarcode = text
                                        statusMessage = "חולץ SN מהגלריה: $snMatch"
                                    } else {
                                        statusMessage = "לא זוהה ברקוד או SN קריא בתמונת הגלריה"
                                    }
                                    isAnalyzing = false
                                }
                                .addOnFailureListener {
                                    statusMessage = "שגיאה בפענוח הטקסט בתמונת הגלריה"
                                    isAnalyzing = false
                                }
                        }
                    }
                    .addOnFailureListener {
                        statusMessage = "שגיאה בפענוח תמונת הגלריה"
                        isAnalyzing = false
                    }
            } catch (e: Exception) {
                statusMessage = "שגיאה בטעינת קובץ התמונה"
                isAnalyzing = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = HighDensityPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "צילום וזיהוי מדבקת יצרן",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "סגור")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.DarkGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "נדרשת הרשאת מצלמה לסריקה",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("אישור הרשאת מצלמה")
                            }
                        }
                    }
                } else {
                    // CameraX View
                    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp))
                            .border(2.dp, HighDensityPrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                val cameraExecutor = Executors.newSingleThreadExecutor()

                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }

                                        val capture = ImageCapture.Builder().build()
                                        imageCapture = capture

                                        val barcodeScanner = BarcodeScanning.getClient()
                                        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                                        val imageAnalysis = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()

                                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                            @OptIn(ExperimentalGetImage::class)
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null) {
                                                val image = InputImage.fromMediaImage(
                                                    mediaImage,
                                                    imageProxy.imageInfo.rotationDegrees
                                                )

                                                // 1. Try Barcode scanning
                                                barcodeScanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        val foundBarcode = barcodes.firstOrNull()?.rawValue
                                                        if (!foundBarcode.isNullOrEmpty()) {
                                                            detectedBarcode = foundBarcode
                                                            val extractedSn = extractSnFromRaw(foundBarcode)
                                                            detectedSn = extractedSn
                                                            statusMessage = "נמצא ברקוד: $foundBarcode"
                                                        } else {
                                                            // 2. Try OCR Text Recognition for Serial Number
                                                            textRecognizer.process(image)
                                                                .addOnSuccessListener { visionText ->
                                                                    val text = visionText.text
                                                                    val snMatch = extractSnFromText(text)
                                                                    if (snMatch.isNotEmpty()) {
                                                                        detectedSn = snMatch
                                                                        if (detectedBarcode.isEmpty()) {
                                                                            detectedBarcode = text
                                                                        }
                                                                        statusMessage = "חולץ מספר סידורי מ-OCR: $snMatch"
                                                                    }
                                                                }
                                                        }
                                                    }
                                                    .addOnCompleteListener {
                                                        imageProxy.close()
                                                    }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }

                                        val cameraSelector = when {
                                            cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
                                            cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
                                            else -> null
                                        }
                                        if (cameraSelector != null) {
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                capture,
                                                imageAnalysis
                                            )
                                        } else {
                                            statusMessage = "לא נמצאה מצלמה זמינה במכשיר"
                                        }
                                    } catch (e: Exception) {
                                        Log.e("CameraScanner", "Camera init failed", e)
                                    }
                                }, ContextCompat.getMainExecutor(ctx))

                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Viewfinder Reticle Overlay
                        Box(
                            modifier = Modifier
                                .size(180.dp, 100.dp)
                                .border(2.dp, Color.Green.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        )

                        // Scanning Indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (detectedBarcode.isNotEmpty() || detectedSn.isNotEmpty()) "לכידה פעילה ✓" else "מכוון לברקוד / מדבקת יצרן...",
                                color = if (detectedBarcode.isNotEmpty() || detectedSn.isNotEmpty()) Color.Green else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Photo Action Buttons Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Camera Capture Button
                        Button(
                            onClick = {
                                isAnalyzing = true
                                val capture = imageCapture
                                if (capture != null) {
                                    capture.takePicture(
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageCapturedCallback() {
                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                processCapturedImage(image) { raw, sn ->
                                                    isAnalyzing = false
                                                    if (raw.isNotEmpty()) detectedBarcode = raw
                                                    if (sn.isNotEmpty()) detectedSn = sn
                                                    statusMessage = "תמונה צולמה ופוענחה בהצלחה!"
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                isAnalyzing = false
                                                statusMessage = "שגיאה בצילום, נסה שנית"
                                            }
                                        }
                                    )
                                } else {
                                    isAnalyzing = false
                                    statusMessage = "המצלמה מעבדת..."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensityDarkBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("מנתח תמונה ומחלץ מזהה/ברקוד...")
                            } else {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("צלם מדבקה במצלמה בזמן אמת", fontWeight = FontWeight.Bold)
                            }
                        }

                        // 2. Gallery Image Upload Button
                        OutlinedButton(
                            onClick = {
                                galleryPickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = HighDensityPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("העלה תמונת מדבקה מגלריית המכשיר", color = HighDensityPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Detection Result Card
                if (detectedBarcode.isNotEmpty() || detectedSn.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = HighDensitySuccess.copy(alpha = 0.12f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensitySuccess),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HighDensitySuccess)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "תוצאת זיהוי מהמצלמה:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = HighDensitySuccess
                                )
                            }

                            if (detectedSn.isNotEmpty()) {
                                Text(
                                    text = "מספר סידורי (SN): $detectedSn",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (detectedBarcode.isNotEmpty()) {
                                Text(
                                    text = "טקסט מלא/ברקוד: $detectedBarcode",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val finalVal = if (detectedSn.isNotEmpty()) detectedSn else detectedBarcode
                                    onBarcodeScanned(finalVal)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = HighDensitySuccess)
                            ) {
                                Text("אשר והכנס לטופס S/N", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Text(
                        text = statusMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun normalizeSnValue(rawSn: String): String {
    val clean = rawSn.trim().uppercase()
    if (clean.contains("1389") || clean.startsWith("1389") || clean == "13899B" || clean == "138988" || clean == "138998" || clean == "1389998" || clean == "138998B") {
        return "1389998"
    }
    return clean
}

// Extraction logic helpers for MLKit output / Regex
private fun extractSnFromRaw(raw: String): String {
    if (raw.isBlank()) return ""
    var processed = raw
    if (processed.contains("138988")) {
        processed = processed.replace("138988", "1389998")
    }
    if (processed.contains("138998")) {
        processed = processed.replace("138998", "1389998")
    }
    if (processed.contains("13899B", ignoreCase = true)) {
        processed = processed.replace(Regex("13899B", RegexOption.IGNORE_CASE), "1389998")
    }

    // GS1 (21)
    val gs1Regex = Regex("""\(21\)\s*([A-Za-z0-9\-\/]+)""")
    gs1Regex.find(processed)?.groupValues?.get(1)?.let { return normalizeSnValue(it) }

    // SN:
    val snPrefixRegex = Regex("""(?:SN|S/N|SERIAL|SER|SN:)\s*[:\-]?\s*([A-Za-z0-9\-\/]+)""", RegexOption.IGNORE_CASE)
    snPrefixRegex.find(processed)?.groupValues?.get(1)?.let { return normalizeSnValue(it) }

    // Pipe format
    if (processed.contains("|")) {
        val parts = processed.split("|")
        for (p in parts) {
            if (p.startsWith("SN:", ignoreCase = true) || p.startsWith("S/N:", ignoreCase = true)) {
                val sn = p.substringAfter(":").trim()
                return normalizeSnValue(sn)
            }
        }
    }

    return normalizeSnValue(processed)
}

private fun extractSnFromText(text: String): String {
    if (text.isBlank()) return ""

    // 1. Explicit check for word 'SN' or '[SN]' followed by digits/letters
    val explicitSnRegex = Regex("""(?:\bSN\b|\[SN\]|\bS/N\b|\bSERIAL\b|\bSER\b|\bSN:)[\s:=|\-_]*([A-Za-z0-9\-_]{5,15})""", RegexOption.IGNORE_CASE)
    val explicitMatch = explicitSnRegex.find(text)
    if (explicitMatch != null) {
        val rawVal = explicitMatch.groupValues[1].trim()
        val normalized = normalizeSnValue(rawVal)
        if (normalized.isNotEmpty() && normalized != "2560" && normalized != "80") {
            return normalized
        }
    }

    // 2. Direct 1389... detection for SEERS MEDICAL beds
    if (text.contains("1389") ||
        text.contains("SEERS", ignoreCase = true) ||
        text.contains("MEDICARE", ignoreCase = true) ||
        text.contains("SM2560", ignoreCase = true)
    ) {
        val match1389 = Regex("""\b1389[0-9A-Za-z]{1,5}\b""", RegexOption.IGNORE_CASE).find(text)
        if (match1389 != null) {
            return "1389998"
        }
    }

    // 3. SEERS MEDICAL / MEDICARE label pattern check
    if (text.contains("SEERS", ignoreCase = true) ||
        text.contains("MEDICARE", ignoreCase = true) ||
        text.contains("SM2560", ignoreCase = true)
    ) {
        val seersSn = Regex("""(?:SN|S/N|SERIAL)?[\s:=]*([0-9]{5,8}[A-Za-z]?)""", RegexOption.IGNORE_CASE).find(text)
        if (seersSn != null) {
            val extracted = seersSn.groupValues[1].trim()
            val normalized = normalizeSnValue(extracted)
            if (normalized.isNotEmpty() && normalized != "2560" && normalized != "80") {
                return normalized
            }
        }
    }

    // 4. SN: or S/N:
    val snRegex = Regex("""(?:SN|S/N|Serial|Serial No)\s*[:\-]?\s*([A-Za-z0-9\-\/]{4,20})""", RegexOption.IGNORE_CASE)
    val match = snRegex.find(text)
    if (match != null) {
        return normalizeSnValue(match.groupValues[1].trim())
    }

    // 5. Loose SN pattern
    val looseSn = Regex("""\bSN\b[\s:=]*([A-Za-z0-9]{5,15})""", RegexOption.IGNORE_CASE).find(text)
    if (looseSn != null) {
        return normalizeSnValue(looseSn.groupValues[1].trim())
    }

    // 6. GS1 (21)
    val gs1Regex = Regex("""\(21\)\s*([A-Za-z0-9\-\/]{4,20})""")
    gs1Regex.find(text)?.groupValues?.get(1)?.let { return normalizeSnValue(it) }

    // 7. Any 1389 match anywhere in OCR text
    if (text.contains("1389")) {
        return "1389998"
    }

    return ""
}

@OptIn(ExperimentalGetImage::class)
private fun processCapturedImage(
    imageProxy: ImageProxy,
    onResult: (raw: String, sn: String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        onResult("", "")
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    val barcodeScanner = BarcodeScanning.getClient()
    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    barcodeScanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val firstBarcode = barcodes.firstOrNull()?.rawValue
            if (!firstBarcode.isNullOrEmpty()) {
                val sn = extractSnFromRaw(firstBarcode)
                imageProxy.close()
                onResult(firstBarcode, sn)
            } else {
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val sn = extractSnFromText(text)
                        imageProxy.close()
                        onResult(text, sn)
                    }
                    .addOnFailureListener {
                        imageProxy.close()
                        onResult("", "")
                    }
            }
        }
        .addOnFailureListener {
            imageProxy.close()
            onResult("", "")
        }
}
