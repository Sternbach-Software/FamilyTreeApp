package org.sternbach.software.familytree

import androidx.compose.runtime.Composable

interface FilePickerLauncher {
    fun launch()
}

@Composable
expect fun rememberFilePicker(onFilePicked: (String?) -> Unit): FilePickerLauncher
