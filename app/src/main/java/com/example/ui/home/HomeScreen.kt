package com.example.ui.home

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AccountEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.SpendingLimitCalculation
import com.example.data.repository.UpcomingBill
import com.example.launcher.AppItem
import com.example.ui.components.AppIconImage
import com.example.ui.components.BalanceCard
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.TimeDateHeader
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    availableBalanceCentavos: Long,
    monthIncomeCentavos: Long,
    monthExpenseCentavos: Long,
    dailyLimitCentavos: Long,
    spendingLimit: SpendingLimitCalculation? = null,
    upcomingBills: List<UpcomingBill>,
    transactions: List<TransactionEntity> = emptyList(),
    isPrivacyEnabled: Boolean,
    showBattery: Boolean,
    isUltraSimpleMode: Boolean,
    isFocusMode: Boolean,
    favoriteApps: List<AppItem>,
    categories: List<CategoryEntity>,
    accounts: List<AccountEntity>,
    riskWarningDate: String?,
    onTogglePrivacy: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenUpcomingBills: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onConfigureHomeApps: () -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onOpenFullRegister: () -> Unit,
    onOpenRegisterIncome: (() -> Unit)? = null,
    onOpenRegisterExpense: (() -> Unit)? = null,
    onVoiceRequest: () -> Unit,
    onQuickSave: (
        type: TransactionType,
        amountCentavos: Long,
        categoryId: Long,
        accountId: Long,
        description: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dragDistance by remember { mutableStateOf(0f) }

    val handleGoogleSearchClick = {
        try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, "")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                onOpenSearch()
            }
        }
    }

    val handleGoogleLensClick = {
        try {
            val lensIntent = context.packageManager.getLaunchIntentForPackage("com.google.ar.lens")
            if (lensIntent != null) {
                context.startActivity(lensIntent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://lens.google/")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            handleGoogleSearchClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        dragDistance += dragAmount
                    },
                    onDragEnd = {
                        if (dragDistance < -60f) {
                            // Swiped Up -> Open App Drawer
                            onOpenAppDrawer()
                        } else if (dragDistance > 60f) {
                            // Swiped Down -> Open Search
                            handleGoogleSearchClick()
                        }
                        dragDistance = 0f
                    },
                    onDragCancel = { dragDistance = 0f }
                )
            }
            .combinedClickable(
                onClick = {},
                onLongClick = onOpenSettings
            )
            .testTag("home_screen_root")
    ) {
        if (isUltraSimpleMode) {
            // ==========================================
            // MODO MINIMALISTA ULTRA SIMPLES
            // ==========================================
            UltraSimpleHomeLayout(
                availableBalanceCentavos = availableBalanceCentavos,
                isPrivacyEnabled = isPrivacyEnabled,
                showBattery = showBattery,
                isFocusMode = isFocusMode,
                favoriteApps = favoriteApps,
                onTogglePrivacy = onTogglePrivacy,
                onOpenFinance = onOpenFinance,
                onOpenAppDrawer = onOpenAppDrawer,
                onOpenSettings = onOpenSettings,
                onConfigureHomeApps = onConfigureHomeApps,
                onLaunchApp = onLaunchApp,
                onAppLongClick = onAppLongClick,
                onVoiceRequest = onVoiceRequest,
                onGoogleLensClick = handleGoogleLensClick
            )
        } else {
            // ==========================================
            // MODO COMPLETO (CYBER TERMINAL HUD)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section: Time, Date, Telemetry Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TimeDateHeader(
                        showBattery = showBattery,
                        isFocusMode = isFocusMode
                    )
                }

                // Center Section: Hero Financial HUD Terminal
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BalanceCard(
                        availableBalanceCentavos = availableBalanceCentavos,
                        monthIncomeCentavos = monthIncomeCentavos,
                        monthExpenseCentavos = monthExpenseCentavos,
                        dailyLimitCentavos = dailyLimitCentavos,
                        spendingLimit = spendingLimit,
                        upcomingBills = upcomingBills,
                        transactions = transactions,
                        isPrivacyEnabled = isPrivacyEnabled,
                        onTogglePrivacy = onTogglePrivacy,
                        onOpenFinance = onOpenFinance,
                        onOpenUpcomingBills = onOpenUpcomingBills,
                        onOpenRegisterIncome = onOpenRegisterIncome,
                        onOpenRegisterExpense = onOpenRegisterExpense,
                        riskWarningDate = riskWarningDate
                    )
                }

                // Bottom Section: Google Search Bar, App Dock & HUD Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Google Search Bar Capsule
                    GoogleSearchBarCapsule(
                        onVoiceClick = onVoiceRequest,
                        onLensClick = handleGoogleLensClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Swipe Up Telemetry Indicator (Only Arrow)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenAppDrawer() }
                            .padding(horizontal = 24.dp, vertical = 3.dp)
                            .testTag("swipe_up_hint_bar")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Deslize para abrir todos os apps",
                            modifier = Modifier.size(24.dp),
                            tint = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val displayFavorites = if (isFocusMode) {
                        favoriteApps.filter { !it.isHiddenInFocus }.take(4)
                    } else {
                        favoriteApps.take(4)
                    }

                    // Cyber Floating Frosted Dock Container
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF0C131D),
                        border = BorderStroke(1.dp, Color(0x332E4760)),
                        tonalElevation = 4.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (displayFavorites.isNotEmpty()) {
                                displayFavorites.forEach { app ->
                                    AppDockItem(
                                        app = app,
                                        label = app.label,
                                        onClick = { onLaunchApp(app) },
                                        onLongClick = onConfigureHomeApps
                                    )
                                }
                            } else {
                                // Default Fallback shortcuts if no apps selected yet
                                AppDockItem(label = "Telefone", onClick = onOpenAppDrawer, onLongClick = onConfigureHomeApps)
                                AppDockItem(label = "Mensagens", onClick = onOpenAppDrawer, onLongClick = onConfigureHomeApps)
                                AppDockItem(label = "Câmera", onClick = onOpenAppDrawer, onLongClick = onConfigureHomeApps)
                            }

                            // Always present "APPS" Terminal Matrix Trigger
                            AppDockItem(
                                label = "APPS",
                                isAppsButton = true,
                                onClick = onOpenAppDrawer,
                                onLongClick = onConfigureHomeApps
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Futuristic "Personalizar Dock" Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onConfigureHomeApps() }
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                            .testTag("btn_customize_home_apps")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Personalizar apps da home",
                            modifier = Modifier.size(13.dp),
                            tint = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CONFIGURAR DOCK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ultra-Simple Minimalist Mode Layout.
 * Distraction-free, elegant typography, text-based app launcher with Google Search.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UltraSimpleHomeLayout(
    availableBalanceCentavos: Long,
    isPrivacyEnabled: Boolean,
    showBattery: Boolean,
    isFocusMode: Boolean,
    favoriteApps: List<AppItem>,
    onTogglePrivacy: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenAppDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onConfigureHomeApps: () -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onVoiceRequest: () -> Unit,
    onGoogleLensClick: () -> Unit
) {
    val displayApps = if (isFocusMode) {
        favoriteApps.filter { !it.isHiddenInFocus }.take(6)
    } else {
        favoriteApps.take(6)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Clean Clock, Date & Minimal Balance
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                TimeDateHeader(
                    showBattery = showBattery,
                    isFocusMode = isFocusMode
                )

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Ajustes",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Discreet Minimalist Balance Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenFinance() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo: ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatCentavos(availableBalanceCentavos, isPrivacyEnabled),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = if (isPrivacyEnabled) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Ocultar/Exibir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // Center Section: Pure Minimalist Typography App List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (displayApps.isNotEmpty()) {
                displayApps.forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .combinedClickable(
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onAppLongClick(app) }
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.3.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                // Fallback default minimal items
                listOf("Telefone", "Mensagens", "Câmera", "Configurações").forEach { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAppDrawer() }
                            .padding(vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Section: Google Search Bar & App Drawer Access
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GoogleSearchBarCapsule(
                onVoiceClick = onVoiceRequest,
                onLensClick = onGoogleLensClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenAppDrawer() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Apps,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TODOS OS APLICATIVOS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Authentic Google Search Bar Capsule.
 * Displays Google Multi-color G logo, editable search text field with ImeAction.Search,
 * dynamic clear/submit buttons, and Voice and Lens shortcuts.
 */
@Composable
private fun GoogleSearchBarCapsule(
    onVoiceClick: () -> Unit,
    onLensClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val performSearch = {
        val query = searchText.trim()
        keyboardController?.hide()
        focusManager.clearFocus()

        val searchUrl = if (query.isNotEmpty()) {
            "https://www.google.com/search?q=" + Uri.encode(query)
        } else {
            "https://www.google.com"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // Prioritize opening in Google Chrome
        try {
            intent.setPackage("com.android.chrome")
            context.startActivity(intent)
        } catch (e: Exception) {
            intent.setPackage(null)
            try {
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Navegador não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("home_google_search_bar"),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1E2632),
        border = BorderStroke(1.dp, Color(0x334285F4)),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Google Icon + Editable Text Field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                GoogleGIcon(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchText.isEmpty()) {
                        Text(
                            text = "Pesquisar no Google...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = Color(0xFF94A3B8)
                        )
                    }

                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_google_search_input"),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        cursorBrush = SolidColor(Color(0xFF4285F4)),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { performSearch() }
                        )
                    )
                }
            }

            // Quick Math / Evaluation Chip if input is an equation or calculation
            val calculatedResult = remember(searchText) {
                evaluateSimpleMath(searchText.trim())
            }

            if (calculatedResult != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Resultado", calculatedResult)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Resultado $calculatedResult copiado!", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Text(
                        text = "= $calculatedResult",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Action Icons: Clear/Submit when typed, or Voice/Lens when empty
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = { searchText = "" },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Limpar pesquisa",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { performSearch() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Buscar no Google",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onVoiceClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Pesquisa por Voz do Google",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onLensClick,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = "Google Lens",
                            tint = Color(0xFFEA4335),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Authentic 4-color Google "G" Logo vector badge.
 */
@Composable
private fun GoogleGIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = minOf(w, h) * 0.44f
        val strokeWidth = radius * 0.38f

        val blue = Color(0xFF4285F4)
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)

        // Arc Blue
        drawArc(
            color = blue,
            startAngle = 0f,
            sweepAngle = -65f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Arc Red
        drawArc(
            color = red,
            startAngle = -65f,
            sweepAngle = -110f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Arc Yellow
        drawArc(
            color = yellow,
            startAngle = -175f,
            sweepAngle = -65f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Arc Green
        drawArc(
            color = green,
            startAngle = 120f,
            sweepAngle = -105f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Horizontal bar (Blue)
        drawLine(
            color = blue,
            start = Offset(center.x - radius * 0.1f, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppDockItem(
    app: AppItem? = null,
    label: String,
    isAppsButton: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(66.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp)
            .testTag(if (isAppsButton) "dock_apps_btn" else "dock_item_${label.lowercase()}")
    ) {
        if (isAppsButton) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = NeonCyan.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.45f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = label,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else {
            AppIconImage(
                app = app,
                defaultLabel = label,
                iconSize = 48.dp,
                shapeRadius = 15.dp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isAppsButton) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            color = if (isAppsButton) NeonCyan else Color(0xFFE2E8F0),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Evaluates simple mathematical expressions entered in the search bar.
 * Supports +, -, *, x, /, % and decimal numbers.
 */
private fun evaluateSimpleMath(input: String): String? {
    if (input.isBlank() || input.length < 3) return null
    val clean = input.replace(" ", "").replace("x", "*").replace("X", "*").replace(",", ".")
    
    // Check if it looks like a math formula (numbers with standard operators)
    val mathRegex = Regex("""^(\d+(?:\.\d+)?)([\+\-\*/%])(\d+(?:\.\d+)?)$""")
    val match = mathRegex.find(clean) ?: return null

    val (num1Str, op, num2Str) = match.destructured
    val n1 = num1Str.toDoubleOrNull() ?: return null
    val n2 = num2Str.toDoubleOrNull() ?: return null

    val res = when (op) {
        "+" -> n1 + n2
        "-" -> n1 - n2
        "*" -> n1 * n2
        "/" -> if (n2 != 0.0) n1 / n2 else return null
        "%" -> (n1 * n2) / 100.0
        else -> return null
    }

    return if (res % 1.0 == 0.0) {
        res.toLong().toString()
    } else {
        String.format(java.util.Locale.US, "%.2f", res).trimEnd('0').trimEnd('.')
    }
}

