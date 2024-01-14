package com.toddler.recordit.screens.dashboard

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toddler.recordit.screens.ImageRecordingViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyHorizontalPager(
    context: Context,
    viewModel: ImageRecordingViewModel,
    pagerState: PagerState,
    modifier: Modifier,
    numberOfImages: Int,
    ) {
    HorizontalPager(
        modifier = modifier,
        beyondBoundsPageCount = 0,
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 16.dp, state = pagerState,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(numberOfImages),
//                        lowVelocityAnimationSpec = AnimationSpec(1f),
        ),
    ) { page ->
        CarouselItem(context, viewModel.itemList.collectAsState().value[page])
    }
}