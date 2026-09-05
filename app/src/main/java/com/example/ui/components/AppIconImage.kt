package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.launcher.AppItem
import kotlin.math.abs

/**
 * Exibe o ícone de um aplicativo de forma otimizada.
 *
 * O ícone é convertido para um bitmap pequeno, adequado ao tamanho
 * real utilizado pela interface. O resultado é mantido em memória
 * pelo remember enquanto o item permanecer na composição.
 *
 * Em caso de falha ao converter o ícone, é utilizado um fallback
 * com a primeira letra do nome do aplicativo.
 */
@Composable
fun AppIconImage(
    app: AppItem?,
    defaultLabel: String = "",
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    shapeRadius: Dp = 16.dp
) {
    val drawable = app?.icon
    val label = app?.label ?: defaultLabel

    if (drawable != null) {
        /*
         * O código anterior convertia TODOS os ícones para 144x144.
         *
         * Isso é desnecessariamente grande para ícones exibidos em
         * aproximadamente 42-50dp e pode provocar grande pressão
         * de memória durante uma rolagem rápida.
         *
         * Agora utilizamos um tamanho proporcional ao uso real.
         */
        val bitmap = remember(drawable, iconSize) {
            try {
                val targetSize = when {
                    iconSize.value <= 44f -> 64
                    iconSize.value <= 52f -> 72
                    else -> 96
                }

                drawable
                    .toBitmap(
                        width = targetSize,
                        height = targetSize
                    )
                    .asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = label,
                modifier = modifier
                    .size(iconSize)
                    .clip(
                        RoundedCornerShape(shapeRadius)
                    )
            )

            return
        }
    }

    /*
     * Fallback seguro:
     * se algum aplicativo tiver um ícone inválido ou não puder
     * ser convertido, a bandeja continua funcionando normalmente.
     */
    val letter = label
        .firstOrNull()
        ?.uppercaseChar()
        ?: 'A'

    val backgroundColor = remember(label) {
        val palette = listOf(
            Color(0xFF1E88E5),
            Color(0xFF43A047),
            Color(0xFF8E24AA),
            Color(0xFFE64A19),
            Color(0xFF00897B),
            Color(0xFFD81B60),
            Color(0xFF3949AB),
            Color(0xFFFB8C00)
        )

        val hash = abs(label.hashCode())

        palette[hash % palette.size]
    }

    Box(
        modifier = modifier
            .size(iconSize)
            .clip(
                RoundedCornerShape(shapeRadius)
            )
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}
