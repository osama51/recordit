package com.toddler.recordit.screens.dashboard

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.startActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.toddler.recordit.MainActivity
import com.toddler.recordit.MyApplication
import com.toddler.recordit.R
import com.toddler.recordit.screens.ImageRecordingViewModel
import com.toddler.recordit.screens.LoadingStates
import com.toddler.recordit.ui.theme.Abel
import com.toddler.recordit.ui.theme.ErrorRed
import com.toddler.recordit.ui.theme.Gray
import com.toddler.recordit.ui.theme.OffWhite
import com.toddler.recordit.ui.theme.Russo
import com.toddler.recordit.upload.UploadCompletionListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: ImageRecordingViewModel,
    startRecordScreen: () -> Unit,
    logOut: () -> Unit,
) {
    // this val should be an argument passed not initiated here
//    val value: FirebaseAuth

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    Log.i("HomeScreen", "HomeScreen RECOMPOSED !!")

    val userName = MainActivity.sharedPreferences.getString("userName", null)
    val application = viewModel.applicationContext as MyApplication
    val googleUserName = application.firebaseAuth.currentUser?.displayName

    val numberOfImages = viewModel.numberOfImages.collectAsState().value
    val numberOfImagesNotRecorded = viewModel.numberOfImagesNotRecorded.collectAsState().value

    val loadingState = viewModel.loadingState.collectAsState().value
    val connected = viewModel.connected.collectAsState().value
    val uploading = viewModel.isUploading.collectAsState().value

    var showNetworkMsg by remember { mutableStateOf(false) }

    var imagesReady by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf("") }



    val snackbarHostState = remember { SnackbarHostState() }
    val state = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { wasGranted ->
            if (wasGranted) {
                Log.i("HomeScreen", "wasGranted in launcher")
                startRecordScreen()
            }
        }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) {
        viewModel.determineNumberOfImagesNotRecorded()

        val pagerState = rememberPagerState(
            initialPage = 0,
            initialPageOffsetFraction = 0.0f //within the range -0.5 to 0.5
        ) {
            // provide pageCount
            numberOfImages
        }

        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    Column(
                        modifier = Modifier
                            .weight(3f)
                            .padding(start = 32.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "$googleUserName",
                            fontSize = 28.sp,
                            style = LocalTextStyle.current.copy(
                                lineHeight = 36.sp,
                            ),
                        )
                    }

                    Divider()
                    Column(
                        modifier = Modifier
                            .weight(5f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = "Upload my records",
                                    fontFamily = Abel, fontSize = 24.sp
                                )
                            },
                            selected = false,
                            onClick = {
                                coroutineScope.launch {
                                    /** good for later maybe*/
//                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    if (viewModel.numberOfImagesRecorded.value == 0) {
                                        Toast.makeText(
                                            context,
                                            "No records to upload",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    } else {
                                        viewModel.uploadAudioFiles(object :
                                            UploadCompletionListener {
                                            override fun onUploadComplete() {
                                                // All files uploaded successfully
                                                Toast.makeText(
                                                    context,
                                                    "Uploaded ${viewModel.numberOfImagesRecorded.value} records successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }

                                            override fun onUploadFailed(
                                                file: File,
                                                exception: Exception
                                            ) {
                                                // Handle individual upload failure, haven't set it up yet
                                                Toast.makeText(
                                                    context,
                                                    "Upload failed for ${file.name}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        })
                                    }

                                }
                            },
                            icon = {
                                Box(Modifier.size(30.dp)) {
                                    if (uploading) {
                                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                                    } else {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_upload),
                                            contentDescription = "Upload icon",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "version: ${viewModel.zipVersion.collectAsState().value}",
                            fontSize = 12.sp,
                            fontFamily = Abel,
                            color = Gray,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                        )
                        Divider()
                        NavigationDrawerItem(
                            modifier = Modifier
                                .padding(bottom = 32.dp),
                            label = {
                                Text(
                                    text = "Log out",
                                    fontFamily = Abel,
                                    fontSize = 24.sp
                                )
                            },
                            selected = false,
                            onClick = logOut,
                            icon = {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_logout),
                                    contentDescription = "Log out icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }

                    // ...other drawer items
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(it)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape = RoundedCornerShape(0.dp, 0.dp, 40.dp, 40.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        )
                        .fillMaxWidth()
                        .height(250.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(4.dp),
                        text = "$numberOfImagesNotRecorded",
                        fontFamily = Russo,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = MaterialTheme.typography.displayLarge.fontSize,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.onPrimary,
                                blurRadius = 50f,
                                offset = Offset(0f, 5f)
                            )
                        ),
                    )
//                            Text(
//                                text = "  /$numberOfImages",
//                                fontFamily = Russo,
//                                style = TextStyle(
//                                    fontSize = 24.sp,
//                                    color = MaterialTheme.colorScheme.onPrimary,
//                                    letterSpacing = 1.sp,
//                                    fontWeight = FontWeight.Light,
//                                ),
//                            )
//                        }
                    Text(
                        text = "Images left to record",
                        fontFamily = Abel,
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = OffWhite,
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(4.dp)
                    )

//                }
                }
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxWidth()
                    ,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // make sure it is the same value for one second
                    LaunchedEffect(connected) {
                        delay(1000)
                        showNetworkMsg = !connected
                    }
                    if(showNetworkMsg){
                        Text(
                            text = "Network not available",
                            fontFamily = Abel,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ErrorRed,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(4.dp)
                        )
                    }

                    when (loadingState) {
                        LoadingStates.DONE -> { imagesReady = true }
                        LoadingStates.LOADING ->{message = getString(context, R.string.loading_message)}
                        LoadingStates.DOWNLOADING ->{message = getString(context, R.string.downloading_message)}
                        LoadingStates.EXTRACTING ->{message = getString(context, R.string.extracting_message)}
                        LoadingStates.ERROR_EXTRACTING ->{ message = getString(context, R.string.error_extracting_message)}
                        LoadingStates.ERROR_DOWNLOADING ->{message = getString(context, R.string.error_downloading_message)}
                        LoadingStates.ERROR_LOADING ->{message = getString(context, R.string.error_loading_message)}
                        LoadingStates.NONE -> {}
                    }
                    if(imagesReady){
                    MyHorizontalPager(
                        context = context,
                        viewModel = viewModel,
                        pagerState = pagerState,
                        modifier = Modifier
                            .padding(0.dp, 24.dp)
                            .weight(1f),
                        numberOfImages = numberOfImages
                    )
                    Text(
                        text = "Total Images: $numberOfImages",
                        fontFamily = Russo,
                        fontSize = 18.sp,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Light,
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .weight(0.3f)
                    )
                    } else {
                        LoadingImagesMessage(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 50.dp),
                            state = loadingState,
                            message = message
                        ){
                            viewModel.getImagesBasedOnVersion()
                        }
                    }
                }



                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxSize(),
//                    contentAlignment = Alignment.BottomEnd
                ) {
                    ExtendedFloatingActionButton(
                        text = {
                            Text(
                                text = getString(context, R.string.start_record_screen),
//                            fontFamily = Abel,
                            )
                        },
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Start icon"
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        onClick = {
                            when (state.status) {
                                is PermissionStatus.Granted -> {
                                    Log.i("HomeScreen", "Permission granted")
                                    if(imagesReady && viewModel.itemList.value.isNotEmpty()){
                                        startRecordScreen()
                                    } else {
                                        Toast.makeText(context, getString(context, R.string.no_images_message), Toast.LENGTH_SHORT).show()
                                    }
                                }

                                else -> {
                                    Log.i("HomeScreen", "Permission not granted")
                                    if (state.status.shouldShowRationale) {
                                        coroutineScope.launch {
                                            val result =
                                                snackbarHostState.showSnackbar(
                                                    message = "Permission required",
                                                    actionLabel = "Go to settings"
                                                )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                val intent = Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts(
                                                        "package",
                                                        context.packageName,
                                                        null
                                                    )
                                                )
                                                startActivity(context, intent, null)
                                            }
                                        }
                                    } else {
                                        launcher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }


//            DotIndicators(
//                pageCount = pageCount,
//                pagerState = pagerState,
//                modifier = Modifier.align(Alignment.BottomCenter)
//            )
//        }
        }
    }
}

