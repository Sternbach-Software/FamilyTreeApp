package org.sternbach.software.familytree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import familytree.composeapp.generated.resources.Res
import familytree.composeapp.generated.resources.compose_multiplatform
import org.folg.gedcom.parser.ModelParser
import sternbach.software.familytreecompose.FamilyGraph
import sternbach.software.familytreecompose.FamilyTreePreview
import sternbach.software.familytreecompose.LayoutEngine
import sternbach.software.familytreecompose.LazyFamilyTree
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import sternbach.software.familytreecompose.FamilyTree

@Composable
@Preview
fun App() {
    MaterialTheme {
        var graph by remember { mutableStateOf<FamilyGraph?>(null) }
        val density = LocalDensity.current

        val launcher = rememberFilePicker { content ->
            if (content != null) {
                println("content: ${content.take(100)}")
                try {
                    val parser = ModelParser()
                    val gedcom = parser.parseGedcom(content.lineSequence())
                    println("Parsed GEDCOM: $gedcom")
                    if (gedcom != null) {
                        println("Mapping to family")
                        val newGraph = GedcomMapper.mapGedcomToFamilyGraph(gedcom)
                        println("Mapped to family graph")

                        val xSpacing = with(density) { 150.dp.toPx() }
                        val ySpacing = with(density) { 160.dp.toPx() }
                        LayoutEngine.calculateLayout(newGraph, xSpacing, ySpacing)
                        println("Calculated layout")

                        graph = newGraph
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle error (show snackbar or dialog)
                }
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { launcher.launch() }) {
                Text("Import GEDCOM")
            }

            val currentGraph = graph
            if (currentGraph != null) {
                LazyFamilyTree(currentGraph)
            } else {
                FamilyTreePreview()
            }
        }
    }
}