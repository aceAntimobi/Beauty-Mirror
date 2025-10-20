package com.beautymirror

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.beautymirror.ui.theme.BeautyMirrorTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeautyMirrorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BeautyMirrorApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BeautyMirrorApp() {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    var showGuides by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf(MakeupFilter.NaturalGlow) }
    var showTip by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (cameraPermissionState.status is PermissionStatus.Denied) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    when (val status = cameraPermissionState.status) {
        is PermissionStatus.Granted -> {
            MirrorContent(
                showGuides = showGuides,
                onToggleGuides = { showGuides = it },
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                onShowTip = { showTip = true }
            )
        }
        is PermissionStatus.Denied -> {
            PermissionRationale(
                isPermanentlyDenied = status.shouldShowRationale.not(),
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }
    }

    if (showTip) {
        MakeupTipDialog(onDismiss = { showTip = false })
    }
}

@Composable
private fun MirrorContent(
    showGuides: Boolean,
    onToggleGuides: (Boolean) -> Unit,
    selectedFilter: MakeupFilter,
    onFilterSelected: (MakeupFilter) -> Unit,
    onShowTip: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(modifier = Modifier.fillMaxSize(), filter = selectedFilter, showGuides = showGuides)

        ElevatedCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "化妆模式",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilterSelector(selected = selectedFilter, onSelected = onFilterSelected)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onShowTip) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "化妆技巧")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "显示辅助线", modifier = Modifier.padding(end = 8.dp))
                        Switch(
                            checked = showGuides,
                            onCheckedChange = onToggleGuides,
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp),
            onClick = { onFilterSelected(MakeupFilter.NaturalGlow) }
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "重置滤镜")
        }
    }
}

@Composable
private fun CameraPreview(modifier: Modifier, filter: MakeupFilter, showGuides: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
        }
        cameraProviderFuture.addListener(listener, executor)

        onDispose {
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        BeautyFilterOverlay(filter = filter)
        if (showGuides) {
            MakeupGuidanceOverlay()
        }
    }
}

@Composable
private fun BeautyFilterOverlay(filter: MakeupFilter) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(filter.overlayColor.copy(alpha = filter.overlayAlpha))
    )
}

@Composable
private fun MakeupGuidanceOverlay() {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val faceRadius = width.coerceAtMost(height) * 0.35f
            val centerX = size.width / 2
            val centerY = size.height * 0.4f
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = faceRadius,
                center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                style = Stroke(width = 6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f)))
            )

            val eyeRadius = faceRadius * 0.18f
            val eyeOffsetY = centerY - faceRadius * 0.25f
            val eyeSpacing = faceRadius * 0.6f
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = eyeRadius,
                center = androidx.compose.ui.geometry.Offset(centerX - eyeSpacing / 2, eyeOffsetY),
                style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = eyeRadius,
                center = androidx.compose.ui.geometry.Offset(centerX + eyeSpacing / 2, eyeOffsetY),
                style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
            )

            val lipWidth = faceRadius * 0.9f
            val lipHeight = faceRadius * 0.25f
            val lipTop = centerY + faceRadius * 0.3f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = androidx.compose.ui.geometry.Offset(centerX - lipWidth / 2, lipTop),
                size = androidx.compose.ui.geometry.Size(lipWidth, lipHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(x = lipHeight / 2, y = lipHeight / 2),
                style = Stroke(width = 5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 12f)))
            )
        }

        OutlinedCard(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Face, contentDescription = null)
                Column {
                    Text(text = "面部黄金比例提示", fontWeight = FontWeight.SemiBold)
                    Text(text = "对齐眼睛和嘴唇到虚线，打造自然妆感。", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun FilterSelector(selected: MakeupFilter, onSelected: (MakeupFilter) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MakeupFilter.values().forEach { filter ->
            val isSelected = filter == selected
            SuggestionChip(
                onClick = { onSelected(filter) },
                label = { Text(text = filter.displayName) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun MakeupTipDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Default.Info, contentDescription = null) },
        title = { Text(text = "快速化妆技巧") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TipLine(title = "底妆", description = "以面部中心为起点向外轻推，让妆感更服帖。")
                TipLine(title = "腮红", description = "微笑并在笑肌位置轻扫，与辅助线的圆弧保持一致。")
                TipLine(title = "唇妆", description = "沿着唇部虚线范围勾勒轮廓，再进行填充。")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

@Composable
private fun TipLine(title: String, description: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.SemiBold)
        Text(text = description, fontSize = 13.sp)
    }
}

@Composable
private fun PermissionRationale(isPermanentlyDenied: Boolean, onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "需要相机权限",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isPermanentlyDenied) {
                "请在系统设置中启用相机权限，以便实时查看妆效。"
            } else {
                "授权相机权限即可实时查看化妆效果和辅助线。"
            },
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onRequestPermission) {
            Text(text = "重新授权")
        }
    }
}

enum class MakeupFilter(val displayName: String, val overlayColor: Color, val overlayAlpha: Float) {
    NaturalGlow(displayName = "自然光泽", overlayColor = Color(0xFFFFCDD2), overlayAlpha = 0.08f),
    CoolContour(displayName = "冷调修容", overlayColor = Color(0xFFBBDEFB), overlayAlpha = 0.12f),
    WarmSunset(displayName = "暖阳蜜桃", overlayColor = Color(0xFFFFE0B2), overlayAlpha = 0.12f)
}
