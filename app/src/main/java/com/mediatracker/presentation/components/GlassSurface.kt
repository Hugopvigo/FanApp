package com.mediatracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mediatracker.presentation.theme.fanAppColors

/**
 * Glass-morphism surface used throughout the iOS26-style redesign.
 * Light themes get a translucent white panel; dark themes get a subtle
 * white-over-dark overlay — all with a hairline inner top highlight.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    radius: Dp = 18.dp,
    elevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    val shape = RoundedCornerShape(radius)
    val bg = if (elevated) fanColors.surfaceElev else fanColors.surfaceGlass

    Box(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, fanColors.borderColor, shape),
    ) {
        // Hairline top-edge highlight (inset 1px from top, full-width, fades to edges)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            fanColors.hairlineColor.copy(alpha = 0.7f),
                            fanColors.hairlineColor.copy(alpha = 0.7f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        content()
    }
}
