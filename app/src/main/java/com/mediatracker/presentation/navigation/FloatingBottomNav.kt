package com.mediatracker.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.mediatracker.presentation.theme.fanAppColors

internal data class NavItem(
    val labelRes: Int,
    val icon: ImageVector,
    val route: BottomNavRoute,
)

@Composable
internal fun FloatingBottomNav(
    currentDestination: NavDestination?,
    onNavigate: (BottomNavRoute) -> Unit,
    items: List<NavItem>,
    modifier: Modifier = Modifier,
) {
    val fanColors = MaterialTheme.fanAppColors
    val pillShape = RoundedCornerShape(26.dp)

    val shadowColor = if (fanColors.isDark) {
        Color(0x80000000)
    } else {
        Color(0x2E503078)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 20.dp,
                    shape = pillShape,
                    ambientColor = shadowColor,
                    spotColor = shadowColor,
                )
                .clip(pillShape)
                .background(fanColors.navBarBg)
                .border(1.dp, fanColors.borderStrong, pillShape),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                items.forEach { item ->
                    val selected = when (item.route) {
                        BottomNavRoute.Home     -> currentDestination?.hasRoute(BottomNavRoute.Home::class) == true
                        BottomNavRoute.Discover -> currentDestination?.hasRoute(BottomNavRoute.Discover::class) == true
                        BottomNavRoute.Library  -> currentDestination?.hasRoute(BottomNavRoute.Library::class) == true
                        BottomNavRoute.Profile  -> currentDestination?.hasRoute(BottomNavRoute.Profile::class) == true
                    }

                    NavPillItem(
                        item = item,
                        selected = selected,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavPillItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fanColors = MaterialTheme.fanAppColors
    val indicatorShape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier = Modifier
                .clip(indicatorShape)
                .background(
                    if (selected)
                        Brush.linearGradient(fanColors.gradientAccent.map { it.copy(alpha = 0.28f) })
                    else
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                )
                .padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = stringResource(item.labelRes),
                tint = if (selected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = stringResource(item.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal fun makeNavItems(
    labelResHome: Int,
    labelResDiscover: Int,
    labelResLibrary: Int,
    labelResProfile: Int,
    iconHome: ImageVector,
    iconDiscover: ImageVector,
    iconLibrary: ImageVector,
    iconProfile: ImageVector,
): List<NavItem> = listOf(
    NavItem(labelResHome, iconHome, BottomNavRoute.Home),
    NavItem(labelResDiscover, iconDiscover, BottomNavRoute.Discover),
    NavItem(labelResLibrary, iconLibrary, BottomNavRoute.Library),
    NavItem(labelResProfile, iconProfile, BottomNavRoute.Profile),
)
