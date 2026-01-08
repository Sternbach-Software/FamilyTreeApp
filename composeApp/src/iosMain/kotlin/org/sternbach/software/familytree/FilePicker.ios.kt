package org.sternbach.software.familytree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.*
import platform.Foundation.*
import platform.UniformTypeIdentifiers.*
import platform.darwin.NSObject

@Composable
actual fun rememberFilePicker(onFilePicked: (String?) -> Unit): FilePickerLauncher {
    val launcher = remember {
        FilePickerLauncherIOS(onFilePicked)
    }
    return launcher
}

class FilePickerLauncherIOS(
    private val onFilePicked: (String?) -> Unit
) : FilePickerLauncher {

    @OptIn(ExperimentalForeignApi::class)
    private val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            if (url != null) {
                // Security scoped resource access might be needed depending on where the file is
                val requestAccess = url.startAccessingSecurityScopedResource()
                try {
                    val content = NSString.stringWithContentsOfURL(url, encoding = NSUTF8StringEncoding, error = null)
                    onFilePicked(content as String?)
                } catch (e: Exception) {
                    onFilePicked(null)
                } finally {
                    if (requestAccess) {
                        url.stopAccessingSecurityScopedResource()
                    }
                }
            } else {
                onFilePicked(null)
            }
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            onFilePicked(null)
        }
    }

    override fun launch() {

        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTType.typeWithFilenameExtension("ged") ?: UTTypeData),
            asCopy = true
        )
        picker.delegate = delegate

        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController

        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}
