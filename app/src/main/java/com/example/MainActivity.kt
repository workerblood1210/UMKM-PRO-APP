package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.Product
import com.example.data.ProductRepository
import com.example.ui.ProductViewModel
import com.example.ui.ProductViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigator()
            }
        }
    }
}

enum class AppState {
    Splash, Dashboard, AddProduct, AddBahanBaku
}

@Composable
fun AppNavigator() {
    var currentState by remember { mutableStateOf(AppState.Splash) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ProductRepository(database.productDao(), database.bahanBakuDao()) }
    val viewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(repository))

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = currentState,
            animationSpec = tween(800, easing = FastOutSlowInEasing),
            label = "AppTransition"
        ) { state ->
            when (state) {
                AppState.Splash -> SplashScreen {
                    currentState = AppState.Dashboard
                }
                AppState.Dashboard -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddProduct = { currentState = AppState.AddProduct },
                    onNavigateToAddBahanBaku = { currentState = AppState.AddBahanBaku }
                )
                AppState.AddProduct -> AddProductScreen(
                    viewModel = viewModel,
                    onBack = { currentState = AppState.Dashboard },
                    onSuccess = {
                        successMessage = "data tersimpan"
                        currentState = AppState.Dashboard
                    }
                )
                AppState.AddBahanBaku -> BahanBakuDashboardScreen(
                    viewModel = viewModel,
                    onBack = { currentState = AppState.Dashboard }
                )
            }
        }

        successMessage?.let {
            SuccessNotification(message = it) {
                successMessage = null
            }
        }
    }
}

@Composable
fun SuccessNotification(message: String, onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(bottom = 120.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .wrapContentSize()
                .animateContentSize(),
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(50.dp),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Konfirmasi Keluar", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "Anda yakin ingin keluar tanpa menyimpan data?",
                color = Color(0xFF94A3B8),
                fontSize = 15.sp
            )
        },
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(28.dp),
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Ya, Keluar", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.White)
            }
        }
    )
}

@Composable
fun DuplicateAlertDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFFB74D))
                Text("Data Sudah Ada", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = { 
            Text(
                "Sistem mendeteksi adanya kesamaan data pada kolom Nama dan Kategori. Silakan periksa kembali input Anda agar tidak terjadi duplikasi.", 
                color = Color(0xFF94A3B8),
                fontSize = 15.sp,
                lineHeight = 22.sp
            ) 
        },
        containerColor = Color(0xFF1E293B),
        shape = RoundedCornerShape(28.dp),
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF))
            ) {
                Text("Perbaiki Data", color = Color(0xFF020E26), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun BahanBakuSlideMenu(
    bahanBakuList: List<com.example.data.BahanBaku>,
    selectedIds: Set<Int>,
    onDismiss: () -> Unit,
    onSelectionChanged: (Set<Int>) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        androidx.compose.animation.AnimatedVisibility(
            visible = true,
            enter = androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }),
            exit = androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {},
                color = Color(0xFF0B1528),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, Color(0x1AFFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Header of Slide Menu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0x0CFFFFFF), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                "Daftar Bahan Baku",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Pilih item untuk komposisi produk",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0x1AFFFFFF), thickness = 1.dp)

                    if (bahanBakuList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada data bahan baku", color = Color(0xFF94A3B8))
                        }
                    } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(1),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 24.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(bahanBakuList.size) { index ->
                                val item = bahanBakuList[index]
                                val isSelected = selectedIds.contains(item.id)
                                
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val newSelection = if (isSelected) {
                                                    selectedIds - item.id
                                                } else {
                                                    selectedIds + item.id
                                                }
                                                onSelectionChanged(newSelection)
                                            }
                                            .padding(vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        androidx.compose.material3.Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                val newSelection = if (isSelected) {
                                                    selectedIds - item.id
                                                } else {
                                                    selectedIds + item.id
                                                }
                                                onSelectionChanged(newSelection)
                                            },
                                            colors = androidx.compose.material3.CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF00F0FF),
                                                uncheckedColor = Color(0x66FFFFFF),
                                                checkmarkColor = Color(0xFF020E26)
                                            )
                                        )
                                        Column {
                                            Text(
                                                item.name,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "${item.amount} ${item.unit} • ${item.category}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    if (index < bahanBakuList.size - 1) {
                                        HorizontalDivider(color = Color(0x0DFFFFFF), thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Simpan Komposisi", color = Color(0xFF020E26), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidChromeBackground(content: @Composable BoxScope.() -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundWaves")
    
    val waveOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val waveOffset2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF020E26), // Deep Blue Top
                        Color(0xFF041235), // Dark Chrome Mid
                        Color(0xFF010718)  // Deep Dark Blue Bottom
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val angle1Rad = Math.toRadians(waveOffset1.toDouble())
            val blob1X = width / 2f + (width / 3f) * Math.cos(angle1Rad).toFloat()
            val blob1Y = height / 3f + (height / 6f) * Math.sin(angle1Rad).toFloat()

            val angle2Rad = Math.toRadians(waveOffset2.toDouble())
            val blob2X = width / 2f + (width / 4f) * Math.cos(angle2Rad).toFloat()
            val blob2Y = height * 0.7f + (height / 8f) * Math.sin(angle2Rad).toFloat()

            // Metallic Cyan flare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x4400F0FF),
                        Color(0x1500D2FF),
                        Color.Transparent
                    ),
                    center = Offset(blob1X, blob1Y),
                    radius = width * 0.85f
                ),
                radius = width * 0.85f,
                center = Offset(blob1X, blob1Y)
            )

            // Deep Cobalt flare
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3A38BDF8),
                        Color(0x0C091E4A),
                        Color.Transparent
                    ),
                    center = Offset(blob2X, blob2Y),
                    radius = width * 1.0f
                ),
                radius = width * 1.0f,
                center = Offset(blob2X, blob2Y)
            )

            // Silver-white ambient flare ("gradations-light")
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x1A94A3B8),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.8f, height * 0.15f),
                    radius = width * 0.7f
                ),
                radius = width * 0.7f,
                center = Offset(width * 0.8f, height * 0.15f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0x06FFFFFF),
                                Color.Transparent,
                                Color(0x0300F0FF)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                }
        ) {
            content()
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                clip = false,
                ambientColor = Color(0x1000F0FF),
                spotColor = Color(0x3B000000)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x15FFFFFF),
                        Color(0x0800F0FF),
                        Color(0x05FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x4DFFFFFF), // Semi-transparent glass white top border
                        Color(0x1A00F0FF), // Soft cyan bottom glow edge
                        Color(0x05FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnim by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1.0f else 0.5f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1200,
            easing = FastOutSlowInEasing
        ),
        label = "LogoAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "SheenSweep")
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SheenProgress"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(3000)
        onAnimationFinished()
    }

    LiquidChromeBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                modifier = Modifier
                    .size(190.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha
                    )
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(44.dp),
                        clip = false,
                        spotColor = Color(0xFF00F0FF)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x2EFFFFFF),
                                Color(0x0A00F0FF)
                            )
                        ),
                        shape = RoundedCornerShape(44.dp)
                    )
                    .border(
                        width = 1.8.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x8CFFFFFF),
                                Color(0x1F00F0FF),
                                Color(0x0AFFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(44.dp)
                    )
                    .clip(RoundedCornerShape(44.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_logo),
                    contentDescription = "UMKM PRO Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            val progress = sheenProgress
                            val xOffset = size.width * progress
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x10FFFFFF),
                                        Color(0x40FFFFFF),
                                        Color(0x10FFFFFF),
                                        Color.Transparent
                                    ),
                                    start = Offset(xOffset, 0f),
                                    end = Offset(xOffset + size.width * 0.4f, size.height)
                                )
                            )
                        }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "UMKM PRO",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 5.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer(alpha = alpha)
                    .testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "L I Q U I D   G L A S S M O R P H I S M",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = Color(0xB300F0FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(3.dp)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(10.dp))
                    .align(Alignment.CenterHorizontally)
            ) {
                val progressWidth = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    progressWidth.animateTo(
                        targetValue = 130f,
                        animationSpec = tween(2800, easing = LinearOutSlowInEasing)
                    )
                }
                Box(
                    modifier = Modifier
                        .width(progressWidth.value.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00F0FF), Color(0xFF7DF9FF))
                            ),
                            RoundedCornerShape(10.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "iOS 27 Engine",
                fontSize = 10.sp,
                color = Color(0x3DFFFFFF),
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp
            )
        }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: ProductViewModel, onNavigateToAddProduct: () -> Unit, onNavigateToAddBahanBaku: () -> Unit) {
    val products by viewModel.uiState.collectAsStateWithLifecycle()
    
    val infiniteTransition = rememberInfiniteTransition(label = "DashboardPulse")
    val cardAlpha by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(
                        color = Color(0x0CFFFFFF),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0x20FFFFFF), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x2600F0FF), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(
                            1.dp, Color(0x26FFFFFF), RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color(0xFF00F0FF), RoundedCornerShape(6.dp))
                    )
                }

                Text(
                    text = "UMKM PRO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    modifier = Modifier.testTag("dashboard_title")
                )

                Box(
                    modifier = Modifier
                        .background(
                            Color(0x1C00F0FF),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp, Color(0x2E00F0FF), RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F0FF),
                        letterSpacing = 1.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                if (products.isEmpty()) {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp)
                            .graphicsLayer(alpha = cardAlpha)
                            .testTag("empty_state_card")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0x15FFFFFF), Color(0x0800F0FF))
                                    ),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .border(
                                    1.dp, Color(0x26FFFFFF), RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty State Icon",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "silahkan buat produk dulu",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Aplikasi UMKM PRO siap dikonfigurasi. Hubungkan database atau tambahkan produk untuk memulai.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xCC94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        products.forEach { product ->
                            ProductCard(product)
                        }
                    }
                }
            }
        }

        GlassmorphicExpandableFab(
            onAddProduct = onNavigateToAddProduct,
            onAddBahanBaku = onNavigateToAddBahanBaku,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp)
                .safeDrawingPadding()
        )
    }
}

@Composable
fun ProductCard(product: Product) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x15FFFFFF),
                        Color(0x0800F0FF)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x4DFFFFFF),
                        Color(0x1A00F0FF)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUris.isNotEmpty()) {
                    val firstImageUri = product.imageUris.split(",").firstOrNull()
                    AsyncImage(
                        model = firstImageUri,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x3300F0FF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.category,
                            color = Color(0xFF00F0FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0x3394A3B8), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = product.type,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                if (product.type == "Paket") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = product.packageName,
                        color = Color(0xCCFFFFFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GlassmorphicExpandableFab(
    onAddProduct: () -> Unit,
    onAddBahanBaku: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(300)),
            exit = fadeOut(tween(200)) + slideOutVertically(targetOffsetY = { 50 }, animationSpec = tween(200))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FabChildButton(icon = Icons.Default.ShoppingCart, label = "Tambah Produk", onClick = {
                    isExpanded = false
                    onAddProduct()
                })
                FabChildButton(icon = Icons.AutoMirrored.Filled.List, label = "Bahan Baku", onClick = {
                    isExpanded = false
                    onAddBahanBaku()
                })
                FabChildButton(icon = Icons.Default.Settings, label = "Pengaturan")
            }
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(22.dp),
                    clip = false,
                    spotColor = Color(0xFF00F0FF)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            Color(0x1A00F0FF)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x1A00F0FF),
                            Color(0x0DFFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .clip(RoundedCornerShape(22.dp))
                .clickable { isExpanded = !isExpanded },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Main Action",
                tint = Color.White,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer(rotationZ = rotation)
            )
        }
    }
}

@Composable
fun FabChildButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .shadow(10.dp, spotColor = Color(0xFF00F0FF))
                .background(Color(0x33000000), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false,
                    spotColor = Color(0xFF00F0FF)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x26FFFFFF),
                            Color(0x0D00F0FF)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x4DFFFFFF),
                            Color(0x1A00F0FF)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(viewModel: ProductViewModel, onBack: () -> Unit, onSuccess: () -> Unit) {
    val products by viewModel.uiState.collectAsStateWithLifecycle()
    val bahanBakuList by viewModel.bahanBakuState.collectAsStateWithLifecycle()
    var selectedBahanBakuIds by remember { mutableStateOf(setOf<Int>()) }
    var showSelectionSheet by remember { mutableStateOf(false) }

    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    
    var showDuplicateAlert by remember { mutableStateOf(false) }
    var showExitAlert by remember { mutableStateOf(false) }
    var productType by remember { mutableStateOf("Satuan") } // Satuan or Paket
    var packageName by remember { mutableStateOf("") }
    var packageDesc by remember { mutableStateOf("") }
    val productTypes = listOf("Satuan", "Paket")
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris ->
            imageUris = imageUris + uris
        }
    )

    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Header (Remains visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color(0x15FFFFFF),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0x26FFFFFF),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { 
                            if (productName.isNotBlank() || category.isNotBlank() || selectedBahanBakuIds.isNotEmpty() || imageUris.isNotEmpty()) {
                                showExitAlert = true
                            } else {
                                onBack()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Tambah Produk",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Form Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                Spacer(modifier = Modifier.height(8.dp))

                GlassmorphicTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = "Nama Produk",
                    placeholder = "Masukkan nama produk"
                )

                GlassmorphicTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = "Kategori Produk",
                    placeholder = "Misal: Minuman, Makanan Ringan"
                )

                // Dropdown Type
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Jenis Produk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                                .clickable { typeDropdownExpanded = true }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = productType,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = typeDropdownExpanded,
                            onDismissRequest = { typeDropdownExpanded = false },
                            modifier = Modifier
                                .background(Color(0xFF0B1528))
                                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(8.dp))
                        ) {
                            productTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, color = Color.White) },
                                    onClick = {
                                        productType = type
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = productType == "Paket") {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        GlassmorphicTextField(
                            value = packageName,
                            onValueChange = { packageName = it },
                            label = "Nama Paket",
                            placeholder = "Misal: Paket Hemat A"
                        )
                        GlassmorphicTextField(
                            value = packageDesc,
                            onValueChange = { packageDesc = it },
                            label = "Keterangan Paket",
                            placeholder = "Isi dari paket ini...",
                            singleLine = false,
                            modifier = Modifier.height(100.dp)
                        )
                    }
                }

                // Image Upload Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Foto Produk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(16.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Photo",
                                        tint = Color(0xFF00F0FF),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Upload",
                                        color = Color(0xFF00F0FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        
                        items(imageUris) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .background(Color(0x99000000), androidx.compose.foundation.shape.CircleShape)
                                        .clickable {
                                            imageUris = imageUris.filter { it != uri }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Bahan Baku Selection (Slide Menu)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Komposisi Produk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .background(Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showSelectionSheet = true }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Pilih Bahan Baku",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${selectedBahanBakuIds.size} item terpilih",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (productName.isNotBlank()) {
                            // Duplicate check
                            val isDuplicate = products.any { 
                                val sameBasic = it.name.trim().equals(productName.trim(), ignoreCase = true) && 
                                               it.category.trim().equals(category.trim(), ignoreCase = true) &&
                                               it.type == productType
                                
                                if (productType == "Paket") {
                                    sameBasic && it.packageName.trim().equals(packageName.trim(), ignoreCase = true)
                                } else {
                                    sameBasic
                                }
                            }
                            
                            if (isDuplicate) {
                                showDuplicateAlert = true
                            } else {
                                viewModel.addProduct(
                                    Product(
                                        name = productName.trim(),
                                        category = category.trim(),
                                        type = productType,
                                        packageName = packageName,
                                        packageDesc = packageDesc,
                                        imageUris = imageUris.joinToString(",") { it.toString() },
                                        bahanBakuIds = selectedBahanBakuIds.joinToString(",")
                                    )
                                )
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(16.dp, spotColor = Color(0xFF00F0FF), shape = RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF),
                        contentColor = Color(0xFF020E26)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Simpan Produk",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }

            if (showSelectionSheet) {
                BahanBakuSlideMenu(
                    bahanBakuList = bahanBakuList,
                    selectedIds = selectedBahanBakuIds,
                    onDismiss = { showSelectionSheet = false },
                    onSelectionChanged = { selectedBahanBakuIds = it }
                )
            }
        }

        if (showDuplicateAlert) {
            DuplicateAlertDialog(onDismiss = { showDuplicateAlert = false })
        }

        if (showExitAlert) {
            ExitConfirmationDialog(
                onConfirm = { 
                    showExitAlert = false
                    onBack()
                },
                onDismiss = { showExitAlert = false }
            )
        }
    }
}
}

@Composable
fun GlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = true,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(start = 4.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color(0x66FFFFFF)) },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00F0FF),
                unfocusedBorderColor = Color(0x26FFFFFF),
                focusedContainerColor = Color(0x0CFFFFFF),
                unfocusedContainerColor = Color(0x0CFFFFFF),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF00F0FF)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BahanBakuDashboardScreen(viewModel: ProductViewModel, onBack: () -> Unit) {
    val bahanBakuList by viewModel.bahanBakuState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var bahanBakuToEdit by remember { mutableStateOf<com.example.data.BahanBaku?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LiquidChromeBackground {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x15FFFFFF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Bahan Baku",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                // List
                if (bahanBakuList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Belum ada bahan baku", color = Color(0xFF94A3B8))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bahanBakuList) { bahanBaku ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = bahanBaku.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "${bahanBaku.amount} ${bahanBaku.unit} - Rp${bahanBaku.price.toLong()}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 12.sp
                                        )
                                    }
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = Color(0xFF00F0FF),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable {
                                                    bahanBakuToEdit = bahanBaku
                                                    showEditDialog = true
                                                }
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFFF4C4C),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable {
                                                    viewModel.deleteBahanBaku(bahanBaku)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // FAB
            androidx.compose.material3.FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF00F0FF),
                contentColor = Color(0xFF020E26),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Bahan Baku")
            }
        }
        
        if (showAddDialog) {
            AddBahanBakuDialog(
                bahanBakuList = bahanBakuList,
                onDismiss = { showAddDialog = false },
                onSave = { newBahanBaku ->
                    viewModel.addBahanBaku(newBahanBaku)
                    showAddDialog = false
                    successMessage = "data tersimpan"
                }
            )
        }
        
        if (showEditDialog && bahanBakuToEdit != null) {
            EditBahanBakuDialog(
                bahanBaku = bahanBakuToEdit!!,
                bahanBakuList = bahanBakuList,
                onDismiss = {
                    showEditDialog = false
                    bahanBakuToEdit = null
                },
                onSave = { updatedBahanBaku ->
                    viewModel.addBahanBaku(updatedBahanBaku)
                    showEditDialog = false
                    bahanBakuToEdit = null
                    successMessage = "data tersimpan"
                }
            )
        }

        successMessage?.let {
            SuccessNotification(message = it) {
                successMessage = null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBahanBakuDialog(
    bahanBakuList: List<com.example.data.BahanBaku>,
    onDismiss: () -> Unit,
    onSave: (com.example.data.BahanBaku) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var satuan by remember { mutableStateOf("Kg") }
    var jumlah by remember { mutableStateOf("") }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDuplicateAlert by remember { mutableStateOf(false) }
    
    val units = listOf("Kg", "Gr", "Ltr", "ML")
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah Bahan Baku",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                GlassmorphicTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = "Nama",
                    placeholder = "Misal: Tepung"
                )

                GlassmorphicTextField(
                    value = kategori,
                    onValueChange = { kategori = it },
                    label = "Kategori",
                    placeholder = "Misal: Sembako"
                )

                GlassmorphicTextField(
                    value = harga,
                    onValueChange = { newValue -> harga = newValue.filter { it.isDigit() } },
                    label = "Harga Modal",
                    placeholder = "Misal: 15000",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Satuan & Jumlah",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.4f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { unitDropdownExpanded = true }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = satuan,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Unit",
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                units.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit, color = Color.White) },
                                        onClick = {
                                            satuan = unit
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = jumlah,
                            onValueChange = { newValue -> jumlah = newValue.filter { it.isDigit() || it == '.' } },
                            placeholder = { Text("Jumlah", color = Color(0x66FFFFFF)) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00F0FF),
                                unfocusedBorderColor = Color(0x26FFFFFF),
                                focusedContainerColor = Color(0x0CFFFFFF),
                                unfocusedContainerColor = Color(0x0CFFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF00F0FF)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(0.6f).height(56.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nama.isNotBlank() && harga.isNotBlank() && jumlah.isNotBlank()) {
                                // Duplicate check
                                val isDuplicate = bahanBakuList.any { 
                                    it.name.equals(nama.trim(), ignoreCase = true) && 
                                    it.category.equals(kategori.trim(), ignoreCase = true) 
                                }
                                
                                if (isDuplicate) {
                                    showDuplicateAlert = true
                                } else {
                                    showConfirmDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF),
                            contentColor = Color(0xFF020E26)
                        )
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
        
        if (showDuplicateAlert) {
            DuplicateAlertDialog(onDismiss = { showDuplicateAlert = false })
        }
        
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Konfirmasi", color = Color.White) },
                text = { Text("Pastikan detail bahan baku sudah benar", color = Color(0xFF94A3B8)) },
                containerColor = Color(0xFF1E293B),
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        onSave(
                            com.example.data.BahanBaku(
                                name = nama,
                                category = kategori,
                                price = harga.toDoubleOrNull() ?: 0.0,
                                unit = satuan,
                                amount = jumlah.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }) {
                        Text("Ya, Simpan", color = Color(0xFF00F0FF))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Batal", color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBahanBakuDialog(
    bahanBaku: com.example.data.BahanBaku,
    bahanBakuList: List<com.example.data.BahanBaku>,
    onDismiss: () -> Unit,
    onSave: (com.example.data.BahanBaku) -> Unit
) {
    var nama by remember { mutableStateOf(bahanBaku.name) }
    var kategori by remember { mutableStateOf(bahanBaku.category) }
    var harga by remember { mutableStateOf(bahanBaku.price.toLong().toString()) }
    var satuan by remember { mutableStateOf(bahanBaku.unit) }
    var jumlah by remember { mutableStateOf(bahanBaku.amount.toString()) }
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDuplicateAlert by remember { mutableStateOf(false) }
    
    val units = listOf("Kg", "Gr", "Ltr", "ML")
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Bahan Baku",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                GlassmorphicTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = "Nama",
                    placeholder = ""
                )

                GlassmorphicTextField(
                    value = kategori,
                    onValueChange = { kategori = it },
                    label = "Kategori",
                    placeholder = ""
                )

                GlassmorphicTextField(
                    value = harga,
                    onValueChange = { newValue -> harga = newValue.filter { it.isDigit() } },
                    label = "Harga Modal",
                    placeholder = "",
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Satuan & Jumlah",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(0.4f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(Color(0x0CFFFFFF), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { unitDropdownExpanded = true }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = satuan,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Unit",
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                units.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit, color = Color.White) },
                                        onClick = {
                                            satuan = unit
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = jumlah,
                            onValueChange = { newValue -> jumlah = newValue.filter { it.isDigit() || it == '.' } },
                            placeholder = { Text("Jumlah", color = Color(0x66FFFFFF)) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00F0FF),
                                unfocusedBorderColor = Color(0x26FFFFFF),
                                focusedContainerColor = Color(0x0CFFFFFF),
                                unfocusedContainerColor = Color(0x0CFFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF00F0FF)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(0.6f).height(56.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nama.isNotBlank() && harga.isNotBlank() && jumlah.isNotBlank()) {
                                // Duplicate check (excluding current item)
                                val isDuplicate = bahanBakuList.any { 
                                    it.id != bahanBaku.id &&
                                    it.name.equals(nama.trim(), ignoreCase = true) && 
                                    it.category.equals(kategori.trim(), ignoreCase = true) 
                                }
                                
                                if (isDuplicate) {
                                    showDuplicateAlert = true
                                } else {
                                    showConfirmDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00F0FF),
                            contentColor = Color(0xFF020E26)
                        )
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }

        if (showDuplicateAlert) {
            DuplicateAlertDialog(onDismiss = { showDuplicateAlert = false })
        }
        
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Konfirmasi", color = Color.White) },
                text = { Text("Pastikan detail bahan baku sudah benar", color = Color(0xFF94A3B8)) },
                containerColor = Color(0xFF1E293B),
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        onSave(
                            bahanBaku.copy(
                                name = nama,
                                category = kategori,
                                price = harga.toDoubleOrNull() ?: 0.0,
                                unit = satuan,
                                amount = jumlah.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }) {
                        Text("Ya, Simpan", color = Color(0xFF00F0FF))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Batal", color = Color(0xFF94A3B8))
                    }
                }
            )
        }
    }
}
