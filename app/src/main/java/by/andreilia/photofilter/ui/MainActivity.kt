package by.andreilia.photofilter.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import by.andreilia.photofilter.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            AppTheme {
                PhotoFilterApp(
                    state = state,
                    selectPhoto = viewModel::selectPhoto,
                    onIntensityChange = viewModel::setIntensity,
                    selectFilter = viewModel::selectFilter
                )
            }
        }
    }
}