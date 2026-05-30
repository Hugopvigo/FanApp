package com.mediatracker.presentation.onboarding

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mediatracker.R
import com.mediatracker.presentation.theme.AppTheme
import com.mediatracker.presentation.theme.DisplayFontFamily
import com.mediatracker.presentation.theme.MediaTrackerTheme
import com.mediatracker.presentation.theme.fanAppColors
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: String,
    val titleRes: Int,
    val descRes: Int,
)

private val pages = listOf(
    OnboardingPage("📚", R.string.onboarding_add_title, R.string.onboarding_add_desc),
    OnboardingPage("📊", R.string.onboarding_track_title, R.string.onboarding_track_desc),
    OnboardingPage("🏆", R.string.onboarding_xp_title, R.string.onboarding_xp_desc),
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
) {
    val fanColors = MaterialTheme.fanAppColors
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Skip button
        if (pagerState.currentPage < pages.lastIndex) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onComplete) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }
        } else {
            Spacer(Modifier.height(56.dp))
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(fanColors.gradientAccent).let {
                                it
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = page.icon, fontSize = 48.sp)
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    text = stringResource(page.titleRes),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = DisplayFontFamily,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(page.descRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Page indicators + button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(pages.size) { i ->
                    Box(
                        modifier = Modifier
                            .size(if (i == pagerState.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) fanColors.gradientAccent.first()
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            ),
                    )
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = fanColors.gradientAccent.first(),
                ),
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.lastIndex) stringResource(R.string.onboarding_next)
                           else stringResource(R.string.onboarding_start),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
