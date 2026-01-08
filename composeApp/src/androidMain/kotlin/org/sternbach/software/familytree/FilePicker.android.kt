package org.sternbach.software.familytree

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
actual fun rememberFilePicker(onFilePicked: (String?) -> Unit): FilePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri == null) {
                onFilePicked(null)
                return@rememberLauncherForActivityResult
            }

            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                reader.close()
                inputStream?.close()
                onFilePicked(sb.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                onFilePicked(null)
            }
        }
    )

    return remember {
        object : FilePickerLauncher {
            override fun launch() {
                launcher.launch(arrayOf("*/*")) // Allow all files, GEDCOMs usually have .ged but MIME type varies
            }
        }
    }
}
