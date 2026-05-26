package com.mediatracker.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun shimmerBrush(
    widthPx: Float = 900f,
    durationMs: Int = 1200,
): Brush {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val colors = listOf(
        base.copy(alpha = 0.9f),
        base.copy(alpha = 0.3f),
        base.copy(alpha = 0.9f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = widthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
        ),
        label = "shimmer_x",
    )
    return Brush.linearGradient(
        colors = colors,
        start = Offset(x - widthPx / 2f, 0f),
        end = Offset(x + widthPx / 2f, 0f),
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    Box(modifier = modifier.clip(shape).background(shimmerBrush()))
}

@Composable
fun MediaCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(120.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ShimmerBox(modifier = Modifier.size(width = 120.dp, height = 180.dp))
        ShimmerBox(
            modifier = Modifier.fillMaxWidth().height(12.dp),
            shape = RoundedCornerShape(4.dp),
        )
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.6f).height(10.dp),
            shape = RoundedCornerShape(4.dp),
        )
    }
}

@Composable
fun MediaRowSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(4) { MediaCardSkeleton() }
    }
}

@Composable
fun FeaturedCardSkeleton(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(210.dp),
        shape = RoundedCornerShape(20.dp),
    )
}

@Composable
fun DiscoverScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ShimmerBox(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(14.dp),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier.height(32.dp).width(90.dp),
                    shape = RoundedCornerShape(999.dp),
                )
            }
        }
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(0.45f).height(20.dp),
            shape = RoundedCornerShape(4.dp),
        )
        MediaRowSkeleton()
    }
}

@Composable
fun LibraryGridSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ShimmerBox(
                modifier = Modifier.fillMaxWidth(0.35f).height(28.dp),
                shape = RoundedCornerShape(4.dp),
            )
            ShimmerBox(
                modifier = Modifier.width(60.dp).height(16.dp),
                shape = RoundedCornerShape(4.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier.height(32.dp).width(80.dp),
                    shape = RoundedCornerShape(999.dp),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier.height(28.dp).width(90.dp),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(3) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        shape = RoundedCornerShape(4.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            repeat(3) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ShimmerBox(
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        shape = RoundedCornerShape(4.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun DetailScreenSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        ShimmerBox(
            modifier = Modifier.fillMaxWidth().height(380.dp),
            shape = RoundedCornerShape(0.dp),
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShimmerBox(
                modifier = Modifier.width(220.dp).height(28.dp),
                shape = RoundedCornerShape(6.dp),
            )
            ShimmerBox(
                modifier = Modifier.width(140.dp).height(16.dp),
                shape = RoundedCornerShape(4.dp),
            )
            Spacer(Modifier.height(8.dp))
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth().height(14.dp),
                    shape = RoundedCornerShape(4.dp),
                )
            }
        }
    }
}
