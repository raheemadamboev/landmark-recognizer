package xyz.teamgravity.landmarkrecognizer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import xyz.teamgravity.landmarkrecognizer.ui.theme.LandmarkRecognizerTheme

class MainActivity : ComponentActivity() {

    private val analyzer: LandmarkImageAnalyzer by lazy {
        LandmarkImageAnalyzer(
            classifier = LandmarkClassifier(
                context = applicationContext
            )
        )
    }

    private val controller: LifecycleCameraController by lazy {
        LifecycleCameraController(applicationContext).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(applicationContext),
                analyzer
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermission()
        setContent {
            LandmarkRecognizerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var classifications by remember { mutableStateOf(persistentListOf<LandmarkClassificationModel>()) }

                    LaunchedEffect(
                        key1 = Unit,
                        block = {
                            analyzer.setOnResult { result ->
                                classifications = result.toPersistentList()
                            }
                        }
                    )

                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CameraPreview(
                            controller = controller
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7F))
                        ) {
                            classifications.forEach { classification ->
                                key(classification) {
                                    Text(
                                        text = classification.name,
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp)
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((constraints.maxWidth / resources.displayMetrics.density).dp)
                                .offset(
                                    x = 0.dp,
                                    y = ((constraints.maxHeight - constraints.maxWidth) / 2 / resources.displayMetrics.density).dp - 10.dp
                                )
                                .padding(10.dp)
                                .border(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                }
            }
        }
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
    }

    private fun checkPermission() {
        if (!hasPermission()) requestPermission()
    }
}

@Composable
fun CameraPreview(
    controller: LifecycleCameraController
) {
    val owner = LocalLifecycleOwner.current

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                this.controller = controller
                controller.bindToLifecycle(owner)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
