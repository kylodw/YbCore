package com.ybzl.track

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class TrackingViewActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(title = {
                    Text(text = "追踪记录")
                }, navigationIcon = {
                    IconButton(onClick = {
                        finish()
                    }) {
                        Image(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                })
                TrackingViewScreen()
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun TrackingViewScreen() {
        var selectedTabIndex by remember {
            mutableIntStateOf(0)
        }
        val pageList = remember {
            mutableStateListOf<TrackingEventDBModel>()
        }
        LaunchedEffect(selectedTabIndex) {
            val eventCode = TrackingManager.getAllEventCodes().getOrNull(selectedTabIndex)
            val tabList = TrackingManager.queryEventCode(eventCode)
            pageList.clear()
            pageList.addAll(tabList)
        }
        val pageState = rememberPagerState() {
            return@rememberPagerState TrackingManager.getAllEventCodes().size
        }
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                TrackingManager.getAllEventCodes().forEachIndexed { index, s ->
                    val select = selectedTabIndex == index
                    val textColor = if (select) {
                        Color(0xFF3BB3C2)
                    } else {
                        Color(0xFF666666)
                    }
                    val bgColor = if (select) {
                        Color(0x1A3BB3C2)
                    } else {
                        Color.White
                    }
                    Text(
                        text = s,
                        fontSize = 13.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .background(
                                color = bgColor,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .height(48.dp)
                            .wrapContentSize(Alignment.Center)
                            .clickable {
                                selectedTabIndex = index
                            },

                        )

                }
            }
            HorizontalPager(state = pageState) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(pageList) {
                        Column {
                            Text(text = it.eventCode)
                            Text(text = it.json)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}