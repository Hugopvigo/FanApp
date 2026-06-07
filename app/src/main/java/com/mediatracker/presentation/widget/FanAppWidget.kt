package com.mediatracker.presentation.widget

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import androidx.glance.text.TextAlign
import androidx.glance.unit.ColorProvider
import com.mediatracker.MainActivity
import com.mediatracker.R
import com.mediatracker.domain.model.MediaType

class FanAppWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        val items = WidgetDataHelper.getInProgressItems(context)
        provideContent {
            WidgetContent(items = items)
        }
    }
}

@OptIn(ExperimentalGlanceApi::class)
@androidx.compose.runtime.Composable
private fun WidgetContent(items: List<WidgetItem>) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background)),
        contentAlignment = Alignment.Center,
    ) {
        if (items.isEmpty()) {
            EmptyState()
        } else {
            ItemContent(item = items.first())
        }
    }
}

@OptIn(ExperimentalGlanceApi::class)
@androidx.compose.runtime.Composable
private fun EmptyState() {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "\uD83D\uDCDA",
            style = TextDefaults.defaultTextStyle.copy(
                fontSize = TextUnit(32f, TextUnitType.Sp),
            ),
        )
        Spacer(modifier = GlanceModifier.height(8))
        Text(
            text = "No items in progress",
            style = TextDefaults.defaultTextStyle.copy(
                textAlign = TextAlign.Center,
                color = ColorProvider(android.graphics.Color.WHITE),
            ),
        )
    }
}

@OptIn(ExperimentalGlanceApi::class)
@androidx.compose.runtime.Composable
private fun ItemContent(item: WidgetItem) {
    val deepLinkUri = Uri.parse("mediatracker://detail/${item.mediaType.name.lowercase(java.util.Locale.US)}/${Uri.encode(item.apiId)}")
    val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
        setPackage("com.mediatracker")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12)
            .clickable(actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.mediaType.emoji,
            style = TextDefaults.defaultTextStyle.copy(
                fontSize = TextUnit(28f, TextUnitType.Sp),
            ),
        )
        Spacer(modifier = GlanceModifier.height(6))
        Text(
            text = item.title,
            style = TextDefaults.defaultTextStyle.copy(
                textAlign = TextAlign.Center,
                color = ColorProvider(android.graphics.Color.WHITE),
                fontSize = TextUnit(13f, TextUnitType.Sp),
            ),
            maxLines = 2,
        )
        if (item.progressLabel != null) {
            Spacer(modifier = GlanceModifier.height(4))
            Text(
                text = item.progressLabel,
                style = TextDefaults.defaultTextStyle.copy(
                    textAlign = TextAlign.Center,
                    color = ColorProvider(0xB3FFFFFF.toInt()),
                    fontSize = TextUnit(11f, TextUnitType.Sp),
                ),
                maxLines = 1,
            )
        }
    }
}

private val MediaType.emoji: String
    get() = when (this) {
        MediaType.SERIES -> "\uD83D\uDCFA"
        MediaType.MOVIE -> "\uD83C\uDFAC"
        MediaType.BOOK -> "\uD83D\uDCD6"
    }
