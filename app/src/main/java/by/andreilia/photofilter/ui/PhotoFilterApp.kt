package by.andreilia.photofilter.ui

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import by.andreilia.photofilter.R
import by.andreilia.photofilter.ui.theme.AppTheme
import jp.co.cyberagent.android.gpuimage.GPUImage
import kotlinx.coroutines.launch

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScreenPreview() {
    val context = LocalContext.current

    val imageBitmap = remember {
        ContextCompat.getDrawable(
            context,
            R.drawable.ic_launcher_background
        )?.toBitmap()?.asImageBitmap()
    }

    val state = remember {
        if (imageBitmap != null) {
            UiState.PhotoSelected(
                imageBitmap = imageBitmap,
                filter = ImageFilter.Original,
                previews = ImageFilter.entries.map {
                    FilterPreview(
                        filter = it,
                        bitmap = it.applyTo(GPUImage(context), imageBitmap, 0.9f)
                    )
                },
                intensity = 0.5f
            )
        } else {
            UiState.NoPhotoSelected
        }
    }

    AppTheme {
        PhotoFilterApp(state = state, selectPhoto = {}, onIntensityChange = {}, selectFilter = {}, savePhoto = {})
    }
}

@Composable
private fun FilterPreviewItem(
    selected: Boolean,
    preview: FilterPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            modifier = Modifier
                .height(84.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (selected)
                        Modifier.border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                    else Modifier
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(onClick = onClick),
            bitmap = preview.bitmap,
            contentDescription = stringResource(preview.filter.title),
            contentScale = ContentScale.Crop
        )
        Text(
            text = stringResource(preview.filter.title),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun PhotoFilterApp(
    state: UiState,
    selectPhoto: (Uri) -> Unit,
    onIntensityChange: (Float) -> Unit,
    selectFilter: (ImageFilter) -> Unit,
    savePhoto: suspend () -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { selectPhoto(uri) }
    }

    var savingPhoto by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            if (state is UiState.PhotoSelected) {
                Image(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    bitmap = state.imageBitmap,
                    contentDescription = null,
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val value = (state as? UiState.PhotoSelected)?.intensity
                var localValue by remember(value) { mutableFloatStateOf(value ?: 0.5f) }

                Slider(
                    value = localValue,
                    onValueChange = { localValue = it },
                    enabled = state is UiState.PhotoSelected && state.filter.intensityAvailable,
                    onValueChangeFinished = { onIntensityChange(localValue) }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(vertical = 16.dp)
                    .padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
            ) {
                if (state is UiState.PhotoSelected) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.previews) {
                            FilterPreviewItem(
                                preview = it,
                                selected = it.filter == state.filter,
                                onClick = { selectFilter(it.filter) }
                            )
                        }
                    }
                    HorizontalDivider(
                        Modifier.padding(vertical = 12.dp),
                        color = DividerDefaults.color.copy(alpha = 0.5f)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    IconButton(
                        modifier = Modifier.align(alignment = Alignment.CenterStart),
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Icon(Icons.Outlined.AddBox, contentDescription = null)
                    }
                    Text(
                        modifier = Modifier.align(alignment = Alignment.Center),
                        text = stringResource(R.string.filters_title),
                        fontSize = 16.sp,
                        letterSpacing = 0.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    val successMessage = stringResource(R.string.image_saved)
                    val failureMessage = stringResource(R.string.image_save_failure)
                    Button(
                        modifier = Modifier.align(alignment = Alignment.CenterEnd),
                        onClick = {
                            if (!savingPhoto) {
                                savingPhoto = true
                                coroutineScope.launch {
                                    try {
                                        savePhoto()
                                        snackbarState.showSnackbar(successMessage)
                                    } catch (e: Exception) {
                                        snackbarState.showSnackbar(failureMessage)
                                        e.printStackTrace()
                                    } finally {
                                        savingPhoto = false
                                    }
                                }
                            }
                        },
                        enabled = state is UiState.PhotoSelected && !savingPhoto
                    ) {
                        Text(stringResource(R.string.save_button_title))
                    }
                }
            }
        }
    }
}