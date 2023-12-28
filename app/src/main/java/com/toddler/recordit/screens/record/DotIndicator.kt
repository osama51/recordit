package com.toddler.recordit.screens.record

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.toddler.recordit.ui.theme.selectedColor
import com.toddler.recordit.ui.theme.unselectedColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DotIndicators(
    pageCount: Int,
    pagerState: PagerState,
    modifier: Modifier
) {
    // TODO: Add animation
    // TODO: If more than a handful of pages, limit the number of dots to 5 and make the side dots smaller
    Row(modifier = modifier) {
        repeat(pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration) selectedColor else unselectedColor

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}