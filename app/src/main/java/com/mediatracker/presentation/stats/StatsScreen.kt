package com.mediatracker.presentation.stats

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.presentation.components.GlassSurface
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.fanAppColors
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import androidx.compose.runtime.LaunchedEffect

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.statusBarsPadding())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            Text(
                text = stringResource(R.string.stats_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(Modifier.height(8.dp))

        SummaryRow(state)

        Spacer(Modifier.height(16.dp))

        TypeDistributionChart(state)

        Spacer(Modifier.height(16.dp))

        MonthlyActivityChart(state.monthlyActivity)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryRow(state: StatsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SummaryTile(
            modifier = Modifier.weight(1f),
            emoji = "✅",
            value = state.totalCompleted.toString(),
            label = stringResource(R.string.status_completed),
        )
        SummaryTile(
            modifier = Modifier.weight(1f),
            emoji = "⏳",
            value = state.totalInProgress.toString(),
            label = stringResource(R.string.status_in_progress),
        )
        SummaryTile(
            modifier = Modifier.weight(1f),
            emoji = "⏱️",
            value = "${state.estimatedHours}h",
            label = stringResource(R.string.stats_estimated_hours),
        )
    }
}

@Composable
private fun SummaryTile(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
) {
    GlassSurface(modifier = modifier, radius = 18.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = DisplayFontFamily,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TypeDistributionChart(state: StatsUiState) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val columnShape = remember { CorneredShape.rounded(6f) }

    LaunchedEffect(state.seriesCompleted, state.moviesCompleted, state.booksCompleted) {
        if (state.seriesCompleted == 0 && state.moviesCompleted == 0 && state.booksCompleted == 0) {
            modelProducer.runTransaction { columnSeries { series(0, 0, 0) } }
        } else {
            modelProducer.runTransaction {
                columnSeries {
                    series(state.seriesCompleted, state.moviesCompleted, state.booksCompleted)
                }
            }
        }
    }

    val bottomLabels = listOf(
        stringResource(R.string.profile_series),
        stringResource(R.string.profile_movies),
        stringResource(R.string.profile_books),
    )
    val bottomFormatter = CartesianValueFormatter { _, x, _ ->
        val idx = x.toInt()
        if (idx in bottomLabels.indices) bottomLabels[idx] else ""
    }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        radius = 18.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.stats_distribution),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                fill = fill(primaryColor),
                                thickness = 24.dp,
                                shape = columnShape,
                            ),
                        ),
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        label = rememberAxisLabelComponent(),
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = rememberAxisLabelComponent(),
                        valueFormatter = bottomFormatter,
                    ),
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                scrollState = rememberVicoScrollState(
                    initialScroll = Scroll.Absolute.Start,
                ),
            )
        }
    }
}

@Composable
private fun MonthlyActivityChart(monthlyActivity: List<MonthlyActivity>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(monthlyActivity) {
        val counts = monthlyActivity.map { it.count }
        if (counts.isEmpty()) {
            modelProducer.runTransaction { lineSeries { series(List(1) { 0 }) } }
        } else {
            modelProducer.runTransaction {
                lineSeries {
                    series(counts)
                }
            }
        }
    }

    val bottomLabels = monthlyActivity.map { it.monthLabel }
    val bottomFormatter = CartesianValueFormatter { _, x, _ ->
        val idx = x.toInt()
        if (idx in bottomLabels.indices) bottomLabels[idx] else ""
    }

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        radius = 18.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.stats_monthly_activity),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(12.dp))

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.Line(
                                fill = LineCartesianLayer.LineFill.single(fill(primaryColor)),
                                stroke = LineCartesianLayer.LineStroke.Continuous(thicknessDp = 3f),
                            ),
                        ),
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        label = rememberAxisLabelComponent(),
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = rememberAxisLabelComponent(),
                        valueFormatter = bottomFormatter,
                    ),
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                scrollState = rememberVicoScrollState(
                    initialScroll = Scroll.Absolute.End,
                ),
            )
        }
    }
}
