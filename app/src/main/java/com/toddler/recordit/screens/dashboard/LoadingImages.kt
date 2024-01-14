package com.toddler.recordit.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toddler.recordit.R
import com.toddler.recordit.screens.LoadingStates
import com.toddler.recordit.ui.theme.Abel
import com.toddler.recordit.ui.theme.MyDarkGray

@Composable
fun LoadingImagesMessage(
    modifier: Modifier,
    state: LoadingStates,
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(54.dp)) {
            when(state){
                LoadingStates.DOWNLOADING, LoadingStates.EXTRACTING, LoadingStates.LOADING-> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                LoadingStates.ERROR_DOWNLOADING, LoadingStates.ERROR_EXTRACTING, LoadingStates.ERROR_LOADING -> {
                    IconButton(
                        modifier = Modifier
                            .fillMaxSize(),
                        onClick = { onRetry() },
                        content = {
                            Icon(
                                modifier = Modifier.align(Alignment.Center).fillMaxSize(),
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_refresh),
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                }
                else -> {

                }
            }
        }
        Text(
            text = message,
            fontFamily = Abel,
            style = TextStyle(
                fontSize = 18.sp,
                color = MyDarkGray,
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

}
