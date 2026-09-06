package com.example.ui.drawer

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.R
import com.example.launcher.AppItem
import com.example.ui.components.AppIconImage
import com.example.ui.theme.CyberBorderGlowing
import com.example.ui.theme.NeonCyan

import java.text.Collator
import java.util.Locale

private enum class DrawerFilter {
    ALL,
    FAVORITES
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    apps: List<AppItem>,
    frequentlyUsedApps: List<AppItem> = emptyList(),
    isFocusMode: Boolean,
    onLaunchApp: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    var searchQuery by remember {
        mutableStateOf("")
    }

    var isGridView by remember {
        mutableStateOf(false)
    }

    var selectedFilter by remember {
        mutableStateOf(DrawerFilter.ALL)
    }

    /*
     * Filtra os aplicativos.
     *
     * O resultado é recalculado somente quando uma das dependências
     * realmente muda.
     */
    val filteredApps = remember(
        apps,
        searchQuery,
        isFocusMode,
        selectedFilter
    ) {
        var baseList = if (isFocusMode) {
            apps.filter { !it.isHiddenInFocus }
        } else {
            apps
        }

        if (selectedFilter == DrawerFilter.FAVORITES) {
            baseList = baseList.filter {
                it.isFavorite
            }
        }

        if (searchQuery.isBlank()) {
            baseList
        } else {
            val query = searchQuery.trim().lowercase()

            baseList.filter {
                it.label.lowercase().contains(query)
            }
        }
    }

    /*
     * Agrupamento da lista por primeira letra.
     */
    val groupedApps = remember(filteredApps) {
        val collator = Collator.getInstance(
            Locale("pt", "BR")
        )

        filteredApps
            .groupBy {
                val firstChar =
                    it.label.firstOrNull()?.uppercaseChar() ?: '#'

                if (firstChar.isLetter()) {
                    firstChar.toString()
                } else {
                    "#"
                }
            }
            .toSortedMap { a, b ->
                collator.compare(a, b)
            }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_drawer_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 16.dp,
                    start = 12.dp,
                    end = 12.dp
                )
        ) {

            /*
             * =========================================================
             * CABEÇALHO
             * =========================================================
             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Voltar para Home",
                        tint = NeonCyan
                    )
                }

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                    },
                    placeholder = {
                        Text(
                            text = "Buscar aplicativo...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Limpar",
                                    tint = Color(0xFF869AB0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("app_search_input"),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor =
                            CyberBorderGlowing.copy(alpha = 0.4f),
                        focusedContainerColor =
                            Color(0xFF0C1420),
                        unfocusedContainerColor =
                            Color(0xFF080D15)
                    )
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                /*
                 * Alternar entre lista e grade.
                 */
                IconButton(
                    onClick = {
                        isGridView = !isGridView
                    },
                    modifier = Modifier.clip(
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector =
                            if (isGridView) {
                                Icons.Filled.List
                            } else {
                                Icons.Filled.GridView
                            },
                        contentDescription =
                            "Alternar Modo de Exibição",
                        tint = NeonCyan
                    )
                }
            }

            /*
             * =========================================================
             * FILTROS
             * =========================================================
             */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 4.dp,
                        vertical = 4.dp
                    ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color =
                        if (selectedFilter == DrawerFilter.ALL) {
                            NeonCyan.copy(alpha = 0.15f)
                        } else {
                            Color(0xFF0F1722)
                        },
                    border = BorderStroke(
                        1.dp,
                        if (selectedFilter == DrawerFilter.ALL) {
                            NeonCyan
                        } else {
                            Color(0x332E4760)
                        }
                    ),
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptics.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            selectedFilter =
                                DrawerFilter.ALL
                        }
                ) {
                    Text(
                        text = "TODOS (${apps.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 10.sp
                        ),
                        color =
                            if (selectedFilter == DrawerFilter.ALL) {
                                NeonCyan
                            } else {
                                Color(0xFF869AB0)
                            },
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color =
                        if (selectedFilter ==
                            DrawerFilter.FAVORITES
                        ) {
                            NeonCyan.copy(alpha = 0.15f)
                        } else {
                            Color(0xFF0F1722)
                        },
                    border = BorderStroke(
                        1.dp,
                        if (selectedFilter ==
                            DrawerFilter.FAVORITES
                        ) {
                            NeonCyan
                        } else {
                            Color(0x332E4760)
                        }
                    ),
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptics.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )

                            selectedFilter =
                                DrawerFilter.FAVORITES
                        }
                ) {
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint =
                                if (selectedFilter ==
                                    DrawerFilter.FAVORITES
                                ) {
                                    NeonCyan
                                } else {
                                    Color(0xFF869AB0)
                                },
                            modifier = Modifier.size(12.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text(
                            text = "FAVORITOS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.sp
                            ),
                            color =
                                if (selectedFilter ==
                                    DrawerFilter.FAVORITES
                                ) {
                                    NeonCyan
                                } else {
                                    Color(0xFF869AB0)
                                }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            /*
             * =========================================================
             * AÇÕES RÁPIDAS
             * =========================================================
             */

            if (searchQuery.isNotBlank()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    val query = searchQuery.trim()

                    Button(
                        onClick = {
                            haptics.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )

                            val waIntent =
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    setPackage(
                                        "com.whatsapp"
                                    )
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        ""
                                    )
                                }

                            try {
                                context.startActivity(
                                    waIntent
                                )
                            } catch (
                                _: Exception
                            ) {
                                Toast.makeText(
                                    context,
                                    "WhatsApp não instalado",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF25D366)
                            ),
                        shape =
                            RoundedCornerShape(12.dp),
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Filled.Chat,
                            contentDescription = null,
                            modifier =
                                Modifier.size(16.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text(
                            "WhatsApp",
                            style =
                                MaterialTheme.typography.labelSmall
                        )
                    }

                    Button(
                        onClick = {
                            haptics.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )

                            val isNumber =
                                query.all {
                                    it.isDigit() ||
                                        it == '+' ||
                                        it == '-' ||
                                        it == ' '
                                }

                            if (
                                isNumber &&
                                query.isNotEmpty()
                            ) {
                                val intent =
                                    Intent(
                                        Intent.ACTION_DIAL,
                                        Uri.parse(
                                            "tel:$query"
                                        )
                                    )

                                try {
                                    context.startActivity(
                                        intent
                                    )
                                } catch (
                                    _: Exception
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Aplicativo de telefone não encontrado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            } else {

                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW
                                    ).apply {
                                        data =
                                            Uri.withAppendedPath(
                                                ContactsContract
                                                    .Contacts
                                                    .CONTENT_FILTER_URI,
                                                Uri.encode(query)
                                            )
                                    }

                                try {
                                    context.startActivity(
                                        intent
                                    )
                                } catch (
                                    _: Exception
                                ) {
                                    Toast.makeText(
                                        context,
                                        "App de Contatos não encontrado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    NeonCyan
                            ),
                        shape =
                            RoundedCornerShape(12.dp),
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector =
                                Icons.Filled.Call,
                            contentDescription = null,
                            modifier =
                                Modifier.size(16.dp),
                            tint = Color.Black
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text(
                            "Ligar / Buscar",
                            style =
                                MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                    }
                }
            }

            /*
             * =========================================================
             * MAIS UTILIZADOS
             * =========================================================
             */

            if (
                searchQuery.isBlank() &&
                selectedFilter == DrawerFilter.ALL &&
                frequentlyUsedApps.isNotEmpty()
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    color = Color(0xFF0A121C),
                    shape =
                        RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        Color(0x2A00F5D4)
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        )
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                start = 2.dp,
                                bottom = 6.dp
                            )
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier =
                                    Modifier.size(13.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(4.dp)
                            )

                            Text(
                                text =
                                    "MAIS UTILIZADOS",
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontWeight =
                                            FontWeight.Bold,
                                        letterSpacing =
                                            1.1.sp,
                                        fontSize = 10.sp
                                    ),
                                color = NeonCyan
                            )
                        }

                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp),
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            itemsIndexed(
                                items =
                                    frequentlyUsedApps.take(
                                        7
                                    ),
                                key = { index, app ->
                                    "frequent_${app.packageName}_$index"
                                }
                            ) { _, app ->
FrequentAppItem(
    app = app,
    onClick = {
        onLaunchApp(
            app
        )
    },
    onLongClick = {
        onAppLongClick(
            app
        )
    }
)
                            }
                        }
                    }
                }
            }

            /*
             * =========================================================
             * RESULTADO
             * =========================================================
             */

            if (filteredApps.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp),
                    contentAlignment =
                        Alignment.TopCenter
                ) {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = stringResource(
                                R.string.no_apps_found
                            ),
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight =
                                        FontWeight.SemiBold,
                                    letterSpacing =
                                        0.5.sp
                                ),
                            color =
                                Color(0xFFCBD5E1)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                "Nenhum aplicativo corresponde aos filtros.",
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                Color(0xFF64748B)
                        )
                    }
                }

            } else if (isGridView) {

                /*
                 * =====================================================
                 * GRADE
                 * =====================================================
                 */

                LazyVerticalGrid(
                    columns =
                        GridCells.Fixed(4),
                    modifier =
                        Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            vertical = 8.dp
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    /*
                     * A chave agora combina packageName + índice.
                     *
                     * Isso evita conflito caso dois itens tenham
                     * o mesmo packageName.
                     */
                    itemsIndexed(
                        items = filteredApps,
                        key = { index, app ->
                            "grid_${app.packageName}_$index"
                        }
                    ) { _, app ->

                        AppGridItem(
                            app = app,
                            onClick = {
                                onLaunchApp(app)
                            },
                            onLongClick = {
                                onAppLongClick(app)
                            }
                        )
                    }
                }

            } else {

                /*
                 * =====================================================
                 * LISTA
                 * =====================================================
                 */

                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            vertical = 8.dp
                        )
                ) {

                    groupedApps.forEach { (header, appList) ->

                        /*
                         * Chave explícita para cada sticky header.
                         *
                         * O header também passa a ter uma identidade
                         * estável e única.
                         */
                        stickyHeader(
                            key = "header_$header"
                        ) {

                            Surface(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .background
                            ) {

                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically,
                                    modifier =
                                        Modifier.padding(
                                            start = 12.dp,
                                            top = 12.dp,
                                            bottom = 4.dp
                                        )
                                ) {

                                    Text(
                                        text =
                                            "// $header",
                                        style =
                                            MaterialTheme
                                                .typography
                                                .labelLarge
                                                .copy(
                                                    fontWeight =
                                                        FontWeight.Bold,
                                                    letterSpacing =
                                                        1.2.sp
                                                ),
                                        color =
                                            NeonCyan
                                    )
                                }
                            }
                        }

                        /*
                         * =================================================
                         * CORREÇÃO PRINCIPAL
                         * =================================================
                         *
                         * Não usamos mais somente packageName como key.
                         *
                         * packageName + índice garante identidade única
                         * dentro da lista.
                         */
                        itemsIndexed(
                            items = appList,
                            key = { index, app ->
                                "row_${header}_${app.packageName}_$index"
                            }
                        ) { _, app ->

                            AppRowItem(
                                app = app,
                                onClick = {
                                    onLaunchApp(
                                        app
                                    )
                                },
                                onLongClick = {
                                    onAppLongClick(
                                        app
                                    )
                                }
                            )
                        }
                    }

                    /*
                     * Espaço final.
                     *
                     * A chave explícita impede qualquer possibilidade
                     * de conflito com outros itens.
                     */
                    item(
                        key = "drawer_bottom_spacer"
                    ) {
                        Spacer(
                            modifier =
                                Modifier.height(32.dp)
                        )
                    }
                }
            }
        }
    }
}

/*
 * =============================================================
 * ITEM DA GRADE
 * =============================================================
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppGridItem(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(
                RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                vertical = 6.dp,
                horizontal = 4.dp
            )
            .testTag(
                "app_grid_${app.packageName}"
            )
    ) {

        Box(
            contentAlignment =
                Alignment.TopEnd
        ) {

            AppIconImage(
                app = app,
                defaultLabel = app.label,
                iconSize = 50.dp,
                shapeRadius = 16.dp
            )

            if (app.isFavorite) {

                Surface(
                    shape = CircleShape,
                    color = NeonCyan,
                    modifier =
                        Modifier.size(14.dp)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Filled.Star,
                            contentDescription =
                                "Favorito",
                            tint = Color.Black,
                            modifier =
                                Modifier.size(9.dp)
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(6.dp)
        )

        Text(
            text = app.label,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight =
                        FontWeight.Medium,
                    fontSize = 11.sp
                ),
            color =
                Color(0xFFCBD5E1),
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis,
            textAlign =
                TextAlign.Center,
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

/*
 * =============================================================
 * ITEM DA LISTA
 * =============================================================
 */
 @OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRowItem(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(
                RoundedCornerShape(14.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag(
                "app_row_${app.packageName}"
            ),
        color =
            Color(0xFF091018),
        shape =
            RoundedCornerShape(14.dp),
        border =
            BorderStroke(
                1.dp,
                Color(0x221E3349)
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                modifier =
                    Modifier.weight(1f)
            ) {

                AppIconImage(
                    app = app,
                    defaultLabel = app.label,
                    iconSize = 42.dp,
                    shapeRadius = 13.dp
                )

                Spacer(
                    modifier =
                        Modifier.width(14.dp)
                )

                Column {

                    Text(
                        text = app.label,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium
                                .copy(
                                    fontWeight =
                                        FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                        color =
                            Color(0xFFE2E8F0)
                    )

                    Text(
                        text =
                            app.packageName,
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall
                                .copy(
                                    fontSize = 10.sp
                                ),
                        color =
                            Color(0xFF64748B),
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }

            if (app.isFavorite) {

                Icon(
                    imageVector =
                        Icons.Filled.Star,
                    contentDescription =
                        "Favorito",
                    tint =
                        NeonCyan,
                    modifier =
                        Modifier.size(16.dp)
                )
            }
        }
    }
}

/*
 * =============================================================
 * ITEM DE MAIS UTILIZADOS
 * =============================================================
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FrequentAppItem(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally,
        modifier = Modifier
            .width(62.dp)
            .clip(
                RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                vertical = 4.dp,
                horizontal = 2.dp
            )
            .testTag(
                "app_frequent_${app.packageName}"
            )
    ) {

        AppIconImage(
            app = app,
            defaultLabel = app.label,
            iconSize = 42.dp,
            shapeRadius = 13.dp
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = app.label,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight =
                        FontWeight.Medium
                ),
            color =
                Color(0xFFCBD5E1),
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis,
            textAlign =
                TextAlign.Center
        )
    }
}
 
