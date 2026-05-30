package com.mediatracker.presentation.leaderboard

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mediatracker.R
import com.mediatracker.domain.model.Ranking
import com.mediatracker.presentation.components.GlassSurface
import com.mediatracker.presentation.components.ShimmerBox
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.fanAppColors
import kotlinx.coroutines.launch

private val TAB_TITLES = listOf(R.string.lb_tab_alltime, R.string.lb_tab_yearly, R.string.lb_tab_series, R.string.lb_tab_movies, R.string.lb_tab_books)
private val MEDALS = listOf("🥇", "🥈", "🥉")

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fanColors = MaterialTheme.fanAppColors
    val pagerState = rememberPagerState(pageCount = { TAB_TITLES.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Spacer(Modifier.statusBarsPadding())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                text = stringResource(R.string.lb_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.weight(1f),
            )
        }

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = fanColors.gradientAccent.first(),
            edgePadding = 16.dp,
            divider = {},
        ) {
            TAB_TITLES.forEachIndexed { index, titleRes ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                        viewModel.onTabSelected(index)
                    },
                    text = {
                        Text(
                            text = stringResource(titleRes),
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            LaunchedEffect(page) { viewModel.onTabSelected(page) }

            if (state.isLoading) {
                LeaderboardLoadingSkeleton()
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "Error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LeaderboardList(
                    rankings = state.rankings,
                    userRank = state.userRank,
                    userEntry = state.userEntry,
                    currentUserId = state.currentUserId,
                )
            }
        }
    }
}

@Composable
private fun LeaderboardList(
    rankings: List<Ranking>,
    userRank: Int?,
    userEntry: Ranking?,
    currentUserId: String?,
) {
    val fanColors = MaterialTheme.fanAppColors

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp,
        ),
    ) {
        items(rankings, key = { it.id }) { ranking ->
            val isCurrentUser = currentUserId != null && ranking.userId == currentUserId
            RankingRow(ranking = ranking, isHighlighted = isCurrentUser)
        }

        if (userRank != null && userEntry != null && rankings.none { it.userId == currentUserId }) {
            item {
                Spacer(Modifier.height(12.dp))
                RankingRow(
                    ranking = userEntry.copy(rank = userRank),
                    isHighlighted = true,
                )
            }
        }
    }
}

@Composable
private fun RankingRow(
    ranking: Ranking,
    isHighlighted: Boolean,
) {
    val fanColors = MaterialTheme.fanAppColors

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        radius = 14.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isHighlighted) Modifier.background(fanColors.gradientAccent.first().copy(alpha = 0.08f)) else Modifier)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = when {
                    ranking.rank <= 3 -> MEDALS[ranking.rank - 1]
                    else -> "${ranking.rank}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (ranking.rank <= 3) fanColors.gradientAccent.first() else MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(fanColors.gradient1.last().copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                if (!ranking.avatarId.isNullOrBlank()) {
                    Text(text = ranking.avatarId, fontSize = 18.sp)
                } else {
                    Text(
                        text = ranking.displayName.first().uppercaseChar().toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = fanColors.gradientAccent.first(),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ranking.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    text = "Nv.${ranking.level} · ${ranking.totalCompleted} ${stringResource(R.string.lb_completed)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = fanColors.gradientAccent.first().copy(alpha = 0.12f),
            ) {
                Text(
                    text = "${ranking.xp} XP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = fanColors.gradientAccent.first(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun LeaderboardLoadingSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(8) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(14.dp),
            )
        }
    }
}
