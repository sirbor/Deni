package com.loki.deni.ui.screens

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.model.PaymentReceiptData
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTextLogo
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.components.DeniBottomNav
import com.loki.deni.ui.viewmodel.AccountDataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun PaymentReceiptScreen(
    navController: NavController,
    txId: Int,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.load() }
    val tx by viewModel.transactions.collectAsStateWithLifecycle()
    val target = tx.firstOrNull { it.transId == txId }
    val remaining = tx.filter { it.loanId == target?.loanId }.sumOf { if (it.type.equals("Debit", true)) -it.amount else it.amount }
    val receipt = PaymentReceiptData(
        txId = txId,
        amount = target?.amount?.toInt() ?: 0,
        method = target?.type ?: "M-Pesa",
        reference = "RCPT-${txId.toString().padStart(6, '0')}",
        timestamp = target?.timestamp?.let { SimpleDateFormat("MMM d yyyy, HH:mm", Locale.ENGLISH).format(Date(it)) } ?: "N/A",
        remainingBalance = remaining.toInt().coerceAtLeast(0),
    )
    val qrPayload = "DENI|TX:${receipt.txId}|REF:${receipt.reference}|AMT:${receipt.amount}|TIME:${receipt.timestamp}"
    val shareText = stringResource(R.string.payment_receipt_share_text, txId)
    val qrBitmap = remember(qrPayload) { generateQrBitmap(qrPayload, 360) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { DeniBottomNav(navController = navController) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
            DeniTopBar(
                title = stringResource(R.string.payment_receipt_title),
                showBackArrow = true,
                onBack = { navController.navigateUp() },
                trailingContent = {
                    TextButton(onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) { Icon(Icons.Outlined.Share, contentDescription = null) }
                },
            )
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.28f else 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        if (isDark) {
                                            listOf(Color(0xFF0D2C30), Color(0xFF11383D), Color(0xFF0A2225))
                                        } else {
                                            listOf(Color(0xFF015B61), Color(0xFF014D52), Color(0xFF012E31))
                                        },
                                    ),
                                )
                                .padding(14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                DeniTextLogo(color = Color.White)
                                Row(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.92f),
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text("Official receipt", color = Color.White.copy(alpha = 0.92f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), CircleShape)
                                    .padding(10.dp),
                            ) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("KES %,d".format(receipt.amount), fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                            Text(stringResource(R.string.payment_successful), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Reference ${receipt.reference}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Detail(stringResource(R.string.reference_no), receipt.reference)
                            Detail(stringResource(R.string.date_time), receipt.timestamp)
                            Detail(stringResource(R.string.payment_method), receipt.method)
                            Detail(stringResource(R.string.remaining_balance), "KES %,d".format(receipt.remainingBalance))
                        }
                        qrBitmap?.let { bmp ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    "Scan receipt QR",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                )
                                androidx.compose.foundation.Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Receipt QR code",
                                    modifier = Modifier.size(132.dp),
                                )
                            }
                        }
                    }
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.24f else 0.16f)),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Receipt ID", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("#${receipt.txId.toString().padStart(6, '0')}", fontWeight = FontWeight.Bold)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DeniButton(
                        text = stringResource(R.string.share),
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        },
                        modifier = Modifier.weight(1f),
                    )
                    GradientPrimaryButton(
                        text = stringResource(R.string.download_pdf),
                        onClick = {
                            val fileName = "Deni-Receipt-${receipt.txId.toString().padStart(6, '0')}.pdf"
                            val saved = saveReceiptPdf(context, receipt, fileName, qrPayload)
                            if (saved) {
                                Toast.makeText(context, "Receipt downloaded to Downloads", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to download receipt PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                GradientPrimaryButton(
                    text = stringResource(R.string.back_to_home),
                    onClick = { navController.navigate(DeniRoutes.Home.route) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f), fontSize = 13.sp)
        Text(
            value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

private fun saveReceiptPdf(
    context: android.content.Context,
    receipt: PaymentReceiptData,
    fileName: String,
    qrPayload: String,
): Boolean {
    return runCatching {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = AndroidColor.parseColor("#0A2225")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint().apply {
            color = AndroidColor.parseColor("#3E4C59")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val amountPaint = Paint().apply {
            color = AndroidColor.parseColor("#014D52")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val rowLabelPaint = Paint().apply {
            color = AndroidColor.parseColor("#52606D")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val rowValuePaint = Paint().apply {
            color = AndroidColor.parseColor("#102A43")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val dividerPaint = Paint().apply {
            color = AndroidColor.parseColor("#D9E2EC")
            strokeWidth = 1f
        }

        canvas.drawText("Deni Official Receipt", 48f, 72f, titlePaint)
        canvas.drawText("Transaction ID #${receipt.txId.toString().padStart(6, '0')}", 48f, 96f, subtitlePaint)
        canvas.drawText("Payment successful", 48f, 138f, subtitlePaint)
        canvas.drawText("KES %,d".format(receipt.amount), 48f, 174f, amountPaint)
        canvas.drawLine(48f, 198f, 547f, 198f, dividerPaint)

        var y = 236f
        val rows = listOf(
            "Reference No" to receipt.reference,
            "Date & Time" to receipt.timestamp,
            "Payment Method" to receipt.method,
            "Remaining Balance" to "KES %,d".format(receipt.remainingBalance),
        )
        rows.forEach { (label, value) ->
            canvas.drawText(label, 48f, y, rowLabelPaint)
            canvas.drawText(value, 270f, y, rowValuePaint)
            y += 34f
        }

        val matrix = QRCodeWriter().encode(qrPayload, BarcodeFormat.QR_CODE, 140, 140)
        val cell = 1.0f
        val qrStartX = 48f
        val qrStartY = y + 20f
        val qrPaint = Paint().apply { color = AndroidColor.BLACK; style = Paint.Style.FILL }
        for (x in 0 until matrix.width) {
            for (yy in 0 until matrix.height) {
                if (matrix.get(x, yy)) {
                    canvas.drawRect(
                        qrStartX + x * cell,
                        qrStartY + yy * cell,
                        qrStartX + (x + 1) * cell,
                        qrStartY + (yy + 1) * cell,
                        qrPaint,
                    )
                }
            }
        }

        canvas.drawLine(48f, y + 176f, 547f, y + 176f, dividerPaint)
        canvas.drawText("Generated by Deni App", 48f, y + 206f, subtitlePaint)
        canvas.drawText(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ENGLISH).format(Date()), 48f, y + 226f, subtitlePaint)

        document.finishPage(page)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Failed to create download entry")
            resolver.openOutputStream(uri)?.use { output ->
                document.writeTo(output)
            } ?: error("Failed to open output stream")
        } else {
            val file = java.io.File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName,
            )
            file.outputStream().use { output -> document.writeTo(output) }
        }

        document.close()
        true
    }.getOrDefault(false)
}

private fun generateQrBitmap(payload: String, size: Int): Bitmap? {
    return runCatching {
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bmp
    }.getOrNull()
}
