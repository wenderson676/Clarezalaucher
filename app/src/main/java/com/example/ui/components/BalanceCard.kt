package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.SpendingLimitCalculation
import com.example.data.repository.UpcomingBill
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.FinanceWarningAmber
import com.example.ui.theme.NeoExpenseDark
import com.example.ui.theme.NeoExpenseRed
import com.example.ui.theme.NeoIncomeDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BalanceCard(
    availableBalanceCentavos: Long,
    monthIncomeCentavos: Long,
    monthExpenseCentavos: Long,
    dailyLimitCentavos: Long,
    spendingLimit: SpendingLimitCalculation? = null,
    upcomingBills: List<UpcomingBill>,
    transactions: List<TransactionEntity> = emptyList(),
    isPrivacyEnabled: Boolean,
    onTogglePrivacy: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenUpcomingBills: () -> Unit,
    onOpenRegisterIncome: (() -> Unit)? = null,
    onOpenRegisterExpense: (() -> Unit)? = null,
    riskWarningDate: String? = null,
    modifier: Modifier = Modifier
) {
    var showLimitExplanationDialog by remember { mutableStateOf(false) }

    val totalUpcomingBills = upcomingBills.sumOf { it.amount }
    val nextBill = upcomingBills.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Cyber HUD Terminal Surface
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .clickable { onOpenFinance() }
                .testTag("home_balance_card"),
            color = Color(0xFF0C131D),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.55f),
                        Color(0x3300F5D4),
                        NeonCyan.copy(alpha = 0.35f)
                    )
                )
            ),
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF131D2A), Color(0xFF090F16))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // HUD Terminal Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonCyan.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "VIDA.FIN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.3.sp,
                                            fontSize = 11.sp
                                        ),
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (riskWarningDate != null || (nextBill != null && nextBill.daysUntilDue <= 3)) FinanceWarningAmber
                                                else NeonCyan
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (riskWarningDate != null || (nextBill != null && nextBill.daysUntilDue <= 3)) "ALERTA" else "ONLINE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 1.sp
                                        ),
                                        color = if (riskWarningDate != null || (nextBill != null && nextBill.daysUntilDue <= 3)) FinanceWarningAmber else NeonCyan
                                    )
                                }
                                Text(
                                    text = "Terminal Financeiro",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color(0xFF869AB0)
                                )
                            }
                        }

                        // Privacy Button
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            modifier = Modifier.size(30.dp)
                        ) {
                            IconButton(
                                onClick = onTogglePrivacy,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("toggle_privacy_btn")
                            ) {
                                Icon(
                                    imageVector = if (isPrivacyEnabled) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Ocultar ou exibir saldo",
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isPrivacyEnabled) NeonCyan else Color(0xFF9EA5AD)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Balance Display & Sparkline Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "// SALDO DISPONÍVEL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp,
                                    fontSize = 10.sp
                                ),
                                color = Color(0xFF869AB0)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatCentavos(availableBalanceCentavos, isPrivacyEnabled),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = NeonCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Real Monthly Telemetry Inflow & Outflow Badges
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(start = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = NeonCyan.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowUpward,
                                        contentDescription = "Entradas no mês",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+ ${CurrencyFormatter.formatCentavos(monthIncomeCentavos, isPrivacyEnabled)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = NeonCyan
                                    )
                                }
                            }

                            Surface(
                                color = NeoExpenseRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, NeoExpenseRed.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDownward,
                                        contentDescription = "Saídas no mês",
                                        tint = NeoExpenseRed,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "- ${CurrencyFormatter.formatCentavos(monthExpenseCentavos, isPrivacyEnabled)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = NeoExpenseRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Functional 7-Day Interactive Spending & Cashflow Chart
                    FunctionalFinancialChart(
                        transactions = transactions,
                        isPrivacyEnabled = isPrivacyEnabled,
                        onOpenFinance = onOpenFinance,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Safe Daily Spend Quota HUD
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLimitExplanationDialog = true },
                        color = Color(0xFF101923),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0x2E00F5D4))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = NeonCyan
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val limitText = if (spendingLimit != null && spendingLimit.todaySpent > 0) {
                                    "Resta Hoje: ${CurrencyFormatter.formatCentavos(spendingLimit.todayRemainingLimit, isPrivacyEnabled)}"
                                } else {
                                    "Limite Hoje: ${CurrencyFormatter.formatCentavos(dailyLimitCentavos, isPrivacyEnabled)}"
                                }
                                Text(
                                    text = limitText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (spendingLimit != null && spendingLimit.todaySpent > spendingLimit.dailyLimit) FinanceExpenseRed else Color(0xFFE2E8F0)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "[ D-${spendingLimit?.daysRemainingInMonth ?: 1} ]",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = NeonCyan.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Entenda o cálculo",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFF869AB0)
                                )
                            }
                        }
                    }

                    // Adaptive Notification Pill
                    if (!isPrivacyEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))

                        when {
                            // Scenario 1: Bill due soon
                            nextBill != null && nextBill.daysUntilDue <= 3 -> {
                                val daysText = when (nextBill.daysUntilDue) {
                                    0 -> "vence hoje"
                                    1 -> "vence amanhã"
                                    else -> "vence em ${nextBill.daysUntilDue}d"
                                }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onOpenUpcomingBills() },
                                    color = FinanceWarningAmber.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FinanceWarningAmber.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = FinanceWarningAmber
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "⚠️ ${nextBill.name} (${CurrencyFormatter.formatCentavos(nextBill.amount)}) $daysText",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = FinanceWarningAmber,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = "Ver contas",
                                            modifier = Modifier.size(13.dp),
                                            tint = FinanceWarningAmber
                                        )
                                    }
                                }
                            }

                            // Scenario 2: Tight budget / Deficit
                            dailyLimitCentavos <= 0 || availableBalanceCentavos < totalUpcomingBills -> {
                                val deficit = (totalUpcomingBills - availableBalanceCentavos).coerceAtLeast(0L)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onOpenFinance() },
                                    color = FinanceExpenseRed.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FinanceExpenseRed.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Warning,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = FinanceExpenseRed
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (deficit > 0) "⚠️ Faltam ${CurrencyFormatter.formatCentavos(deficit)} no mês"
                                                else "⚠️ Limite diário ultrapassado",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                                color = FinanceExpenseRed,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = "Acessar finanças",
                                            modifier = Modifier.size(13.dp),
                                            tint = FinanceExpenseRed
                                        )
                                    }
                                }
                            }

                            // Scenario 3: All bills covered
                            totalUpcomingBills > 0 && availableBalanceCentavos >= totalUpcomingBills -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onOpenUpcomingBills() },
                                    color = FinanceIncomeGreen.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, FinanceIncomeGreen.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = FinanceIncomeGreen
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "✓ Contas do mês cobertas (${spendingLimit?.daysRemainingInMonth ?: 1}d restantes)",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
                                                color = FinanceIncomeGreen,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = "Ver contas",
                                            modifier = Modifier.size(13.dp),
                                            tint = FinanceIncomeGreen
                                        )
                                    }
                                }
                            }

                            // Scenario 4: Standard next bill
                            nextBill != null -> {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onOpenUpcomingBills() },
                                    color = Color(0xFF131D28),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0x22FFFFFF))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CalendarToday,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = NeonCyan
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${nextBill.name} em ${nextBill.daysUntilDue}d: ${CurrencyFormatter.formatCentavos(nextBill.amount)}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp),
                                                color = Color(0xFFCBD5E1),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Filled.ChevronRight,
                                            contentDescription = "Ver contas",
                                            modifier = Modifier.size(13.dp),
                                            tint = Color(0xFF869AB0)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cyber Tactical Quick Register Triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left: -R$ NOVA DESPESA (Laser Coral / Red)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenRegisterExpense?.invoke() ?: onOpenFinance() }
                                .testTag("btn_quick_register_expense"),
                            color = NeoExpenseDark,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, NeoExpenseRed.copy(alpha = 0.55f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "-R$",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = NeoExpenseRed,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "NOVA DESPESA",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        // Right: +R$ ADICIONAR RECEITA (Electric Cyan)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenRegisterIncome?.invoke() ?: onOpenFinance() }
                                .testTag("btn_quick_register_income"),
                            color = NeoIncomeDark,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.55f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "+R$",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    ),
                                    color = NeonCyan,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "ADICIONAR RECEITA",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom Cyber Prompt
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "// Toque para extrato, metas e cartões",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF7E92A6)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = Color(0xFF7E92A6)
                        )
                    }
                }
            }
        }
    }

    // Limit Explanation Dialog Modal
    if (showLimitExplanationDialog) {
        val calc = spendingLimit ?: SpendingLimitCalculation(
            currentBalance = availableBalanceCentavos,
            upcomingBillsMonth = totalUpcomingBills,
            reservedForGoals = 0L,
            netAvailable = (availableBalanceCentavos - totalUpcomingBills).coerceAtLeast(0L),
            daysRemainingInMonth = 1,
            dailyLimit = dailyLimitCentavos,
            formulaExplanation = ""
        )

        AlertDialog(
            onDismissRequest = { showLimitExplanationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cálculo do Limite Diário",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Valor que você pode gastar livremente hoje preservando os compromissos do mês:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Saldo em Conta:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    CurrencyFormatter.formatCentavos(calc.currentBalance),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            if (calc.upcomingIncomesMonth > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("(+) Rendas a receber no mês:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "+ ${CurrencyFormatter.formatCentavos(calc.upcomingIncomesMonth)}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = FinanceIncomeGreen, fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("(-) Contas a pagar no mês:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "- ${CurrencyFormatter.formatCentavos(calc.upcomingBillsMonth)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = FinanceExpenseRed, fontWeight = FontWeight.SemiBold)
                                )
                            }
                            if (calc.reservedForGoals > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("(-) Reservas para Metas:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "- ${CurrencyFormatter.formatCentavos(calc.reservedForGoals)}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = FinanceWarningAmber, fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("(=) Saldo Realmente Livre:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    CurrencyFormatter.formatCentavos(calc.netAvailable),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = FinanceIncomeGreen)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("(÷) Dias restantes no mês:", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${calc.daysRemainingInMonth} dias",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Divider(modifier = Modifier.padding(vertical = 6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("(=) Teto Diário Planejado:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    CurrencyFormatter.formatCentavos(calc.dailyLimit),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NeonCyan)
                                )
                            }
                            if (calc.todaySpent > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("(-) Gasto Hoje:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "- ${CurrencyFormatter.formatCentavos(calc.todaySpent)}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = FinanceExpenseRed)
                                    )
                                }
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("(=) Restante Hoje:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        CurrencyFormatter.formatCentavos(calc.todayRemainingLimit),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (calc.todayRemainingLimit > 0) NeonCyan else FinanceExpenseRed
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Gastando até esse teto por dia, todas as contas do mês serão pagas pontualmente.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLimitExplanationDialog = false }) {
                    Text("OK, Entendido", color = NeonCyan)
                }
            }
        )
    }
}

/**
 * High-performance minimalist cyber vector sparkline.
 */
@Composable
fun MiniSparkline(
    modifier: Modifier = Modifier,
    lineColor: Color = NeonCyan
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val path = Path().apply {
            moveTo(0f, h * 0.85f)
            cubicTo(
                w * 0.25f, h * 0.90f,
                w * 0.40f, h * 0.20f,
                w * 0.65f, h * 0.45f
            )
            cubicTo(
                w * 0.80f, h * 0.60f,
                w * 0.92f, h * 0.10f,
                w, h * 0.15f
            )
        }

        val fillPath = Path().apply {
            addPath(path)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Truly functional, interactive 7-day telemetry chart for Home Screen.
 * Visualizes daily cashflow and spending with touch selection.
 */
private data class DayFlowData(
    val dateMillis: Long,
    val dayLabel: String,
    val dayOfMonth: String,
    val isToday: Boolean,
    val expenseCentavos: Long,
    val incomeCentavos: Long,
    val txCount: Int
)

@Composable
fun FunctionalFinancialChart(
    transactions: List<TransactionEntity>,
    isPrivacyEnabled: Boolean,
    onOpenFinance: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    val daysData = remember(transactions) {
        val list = mutableListOf<DayFlowData>()
        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val dayNames = arrayOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dYear = dayCal.get(Calendar.YEAR)
            val dDayOfYear = dayCal.get(Calendar.DAY_OF_YEAR)
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed
            val dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH).toString()
            val isToday = (dYear == todayYear && dDayOfYear == todayDayOfYear)

            // Filter transactions that occurred on this day
            val dayTxs = transactions.filter { tx ->
                val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                txCal.get(Calendar.YEAR) == dYear && txCal.get(Calendar.DAY_OF_YEAR) == dDayOfYear
            }

            val dayExpense = dayTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val dayIncome = dayTxs.filter { it.type == "INCOME" }.sumOf { it.amount }

            list.add(
                DayFlowData(
                    dateMillis = dayCal.timeInMillis,
                    dayLabel = dayNames.getOrElse(dayOfWeek) { "Dia" },
                    dayOfMonth = dayOfMonth,
                    isToday = isToday,
                    expenseCentavos = dayExpense,
                    incomeCentavos = dayIncome,
                    txCount = dayTxs.size
                )
            )
        }
        list
    }

    val total7dExpense = remember(daysData) { daysData.sumOf { it.expenseCentavos } }
    val maxDayExpense = remember(daysData) {
        val m = daysData.maxOfOrNull { it.expenseCentavos } ?: 0L
        if (m <= 0L) 10000L else m // baseline if 0
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        color = Color(0xFF0D1520),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0x2B00F5D4))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Chart Telemetry Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "// FLUXO 7 DIAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                }

                Text(
                    text = "Gasto 7d: ${CurrencyFormatter.formatCentavos(total7dExpense, isPrivacyEnabled)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    ),
                    color = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7 Glowing Visual Bars Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                daysData.forEachIndexed { index, day ->
                    val isSelected = (selectedDayIndex == index)
                    val heightRatio = if (day.expenseCentavos > 0) {
                        (day.expenseCentavos.toFloat() / maxDayExpense.toFloat()).coerceIn(0.12f, 1.0f)
                    } else 0.05f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                selectedDayIndex = if (selectedDayIndex == index) null else index
                            }
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        // Dot indicator if day had income
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day.incomeCentavos > 0) NeonCyan else Color.Transparent
                                )
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // Dynamic Cyber Bar Tower
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 18.dp else 14.dp)
                                .height((32 * heightRatio).coerceAtLeast(5f).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                .background(
                                    when {
                                        isSelected -> Brush.verticalGradient(listOf(NeonCyan, Color(0xFF0077B6)))
                                        day.isToday -> Brush.verticalGradient(listOf(Color(0xFF00F5D4), Color(0xFF0B525B)))
                                        day.expenseCentavos > 0 -> Brush.verticalGradient(listOf(NeoExpenseRed.copy(alpha = 0.85f), Color(0xFF4A1525)))
                                        else -> Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                    }
                                )
                                .border(
                                    width = if (isSelected || day.isToday) 1.dp else 0.dp,
                                    color = if (isSelected) NeonCyan else if (day.isToday) NeonCyan.copy(alpha = 0.6f) else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Day label & number
                        Text(
                            text = if (day.isToday) "Hoje" else day.dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = when {
                                isSelected -> NeonCyan
                                day.isToday -> Color.White
                                else -> Color(0xFF64748B)
                            },
                            maxLines = 1
                        )
                    }
                }
            }

            // Interactive Day Readout Pill
            AnimatedVisibility(
                visible = selectedDayIndex != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedDayIndex?.let { idx ->
                    val day = daysData.getOrNull(idx)
                    if (day != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF131D28),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${day.dayLabel} (${day.dayOfMonth}):",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (day.expenseCentavos > 0) "- ${CurrencyFormatter.formatCentavos(day.expenseCentavos, isPrivacyEnabled)}" else "Sem gastos",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (day.expenseCentavos > 0) NeoExpenseRed else Color(0xFF94A3B8)
                                    )
                                    if (day.incomeCentavos > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "+ ${CurrencyFormatter.formatCentavos(day.incomeCentavos, isPrivacyEnabled)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 10.sp
                                            ),
                                            color = NeonCyan
                                        )
                                    }
                                }

                                Text(
                                    text = "${day.txCount} reg.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = Color(0xFF869AB0)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


