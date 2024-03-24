package ir.ehsannarmani.calendar.utils

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState

@OptIn(ExperimentalFoundationApi::class)
suspend fun PagerState.animateToPrevious(spec: AnimationSpec<Float> = tween(400)){
    if (currentPage == 0){
        animateScrollToPage(pageCount-1, animationSpec = spec)
    }else{
        animateScrollToPage(currentPage-1, animationSpec = spec)
    }
}
@OptIn(ExperimentalFoundationApi::class)
suspend fun PagerState.animateToNext(spec: AnimationSpec<Float> = tween(400)){
    if (currentPage == pageCount-1){
        animateScrollToPage(0, animationSpec = spec)
    }else{
        animateScrollToPage(currentPage+1, animationSpec = spec)
    }
}