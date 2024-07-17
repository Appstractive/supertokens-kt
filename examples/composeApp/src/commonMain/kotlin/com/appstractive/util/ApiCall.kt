package com.appstractive.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import com.appstractive.getPlatformDialogProperties
import com.appstractive.ui.ApiErrorDialog
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuit.retained.rememberRetained
import kotlin.math.max
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val apiLoading: MutableStateFlow<Int> by lazy { MutableStateFlow(0) }
val isLoading = apiLoading.asStateFlow()

@Composable fun isLoading(): Boolean = isLoading.collectAsState().value > 0

typealias ErrorHandler = (defaultHandler: (Exception) -> Unit, Exception) -> Unit

class ApiCallController(
    private val scope: CoroutineScope,
    private val overlayHost: OverlayHost,
    private val keyboardController: SoftwareKeyboardController?,
    private val defaultErrorHandler: (Exception) -> Unit = { ex ->
      scope.launch { overlayHost.show(ApiErrorDialog(exception = ex)) }
    },
) {
  private var isCalling = false

  fun call(
      withLoading: MutableStateFlow<Int>? = apiLoading,
      hideKeyboard: Boolean = true,
      onError: (ErrorHandler)? = { _, ex -> defaultErrorHandler(ex) },
      apiCall: suspend CoroutineScope.() -> Unit,
  ) {
    if (!isCalling) {
      isCalling = true

      if (hideKeyboard) {
        keyboardController?.hide()
      }

      scope.launch(Dispatchers.IO) {
        try {
          coroutineScope {
            withLoading?.update { it + 1 }

            apiCall()
          }
        } catch (ex: CancellationException) {
          // NOOP
        } catch (ex: Exception) {
          ex.printStackTrace()
          onError?.invoke(defaultErrorHandler, ex)
        } finally {
          isCalling = false
          withLoading?.update { max(it - 1, 0) }
        }
      }
    }
  }
}

@Composable
fun rememberApiCallController(): ApiCallController {
  val scope = rememberRetainedCoroutineScope()
  val overlayHost = LocalOverlayHost.current
  val keyboardController = LocalSoftwareKeyboardController.current
  return rememberRetained {
    ApiCallController(
        scope = scope,
        keyboardController = keyboardController,
        overlayHost = overlayHost,
    )
  }
}

@Composable
fun ApiCallLoading(content: @Composable (Boolean) -> Unit) {
  val loadingState by apiLoading.collectAsState()
  var showLoading by remember { mutableStateOf(false) }

  LaunchedEffect(loadingState > 0) {
    showLoading =
        if (loadingState > 0) {
          delay(200)
          loadingState > 0
        } else {
          false
        }
  }

  Box {
    content(showLoading)

    if (showLoading) {
      Dialog(
          onDismissRequest = {},
          properties =
              getPlatformDialogProperties(
                  scrimColor = Color.Black.copy(alpha = 0.5f),
                  dismissOnClickOutside = false,
                  dismissOnBackPress = false,
              ),
      ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }
}
