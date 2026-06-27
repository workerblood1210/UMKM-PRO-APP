package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.Product
import com.example.ui.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailProductScreen(
    product: Product,
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onEditBahanBaku: () -> Unit
) {
    var showEditMenu by remember { mutableStateOf(false) }
    
    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                        tint = Color.White
                    )
                }

                Text(
                    text = "DETAIL PRODUK",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Box {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0x15FFFFFF), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                            .clickable { showEditMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF00F0FF)
                        )
                    }

                    DropdownMenu(
                        expanded = showEditMenu,
                        onDismissRequest = { showEditMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0x3300F0FF))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Produk", color = Color.White) },
                            onClick = {
                                showEditMenu = false
                                onEditProduct(product)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Bahan Baku", color = Color.White) },
                            onClick = {
                                showEditMenu = false
                                onEditBahanBaku()
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                // Images
                if (product.imageUris.isNotEmpty()) {
                    val images = product.imageUris.split(",")
                    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { images.size })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f/9f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(20.dp))
                    ) {
                        androidx.compose.foundation.pager.HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = images[page],
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        if (images.size > 1) {
                            Row(
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(images.size) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                    Box(
                                        modifier = Modifier
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detail Box
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(
                                text = product.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = product.category,
                                    color = Color(0xFF00F0FF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "|",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = product.type,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            
                            if (product.type == "Paket") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = product.packageName,
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        if (product.type == "Paket") {
                            DetailItem("Deskripsi Paket", product.packageDesc)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bahan Baku:",
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        val bahanBakuList by viewModel.bahanBakuState.collectAsStateWithLifecycle()
                        val selectedIds = product.bahanBakuIds.split(",").filter { it.isNotBlank() }.map { it.toInt() }
                        val relatedBahanBaku = bahanBakuList.filter { it.id in selectedIds }

                        if (relatedBahanBaku.isEmpty()) {
                            Text("Tidak ada bahan baku terkait.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        } else {
                            relatedBahanBaku.forEach { bb ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(bb.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(bb.category, color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Rp ${bb.price.toLong()}", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${bb.amount} ${bb.unit}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun KalkulasiScreen(
    products: List<Product>,
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    LiquidChromeBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
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
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "KALKULASI BIAYA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                var grandTotal = 0.0
                val bahanBakuList by viewModel.bahanBakuState.collectAsStateWithLifecycle()

                products.forEach { product ->
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Produk: ${product.name}",
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val selectedIds = product.bahanBakuIds.split(",").filter { it.isNotBlank() }.map { it.toInt() }
                            val relatedBahanBaku = bahanBakuList.filter { it.id in selectedIds }
                            
                            var productTotalCost = 0.0
                            
                            if (relatedBahanBaku.isEmpty()) {
                                Text("Tidak ada bahan baku yang dikalkulasi.", color = Color(0xFF94A3B8), fontSize = 14.sp)
                            } else {
                                relatedBahanBaku.forEach { bb ->
                                    productTotalCost += bb.price
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(bb.name, color = Color.White, fontSize = 14.sp)
                                        Text("Rp ${bb.price.toLong()}", color = Color(0xFF94A3B8), fontSize = 14.sp)
                                    }
                                }
                                
                                HorizontalDivider(color = Color(0x33FFFFFF), modifier = Modifier.padding(vertical = 12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal", color = Color(0xFF00F0FF), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("Rp ${productTotalCost.toLong()}", color = Color(0xFF00F0FF), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                            grandTotal += productTotalCost
                        }
                    }
                }

                if (products.isNotEmpty()) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("TOTAL KESELURUHAN", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Rp ${grandTotal.toLong()}", color = Color(0xFF00F0FF), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
            }
        }
    }
}
