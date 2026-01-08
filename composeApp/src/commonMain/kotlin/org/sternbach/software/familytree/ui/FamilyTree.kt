package sternbach.software.familytreecompose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// --- State Management ---

@Stable
class FamilyTreeState(
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero
) {
    var scale by mutableStateOf(initialScale)
    var offset by mutableStateOf(initialOffset)

    fun onTransform(zoomChange: Float, panChange: Offset) {
        scale = (scale * zoomChange).coerceIn(0.1f, 3f)
        offset += panChange
    }
}

@Composable
fun rememberFamilyTreeState() = remember { FamilyTreeState() }

// --- Main Component ---

@Composable
fun LazyFamilyTree(
    graph: FamilyGraph,
    modifier: Modifier = Modifier,
    nodeWidth: Dp = 120.dp,
    nodeHeight: Dp = 70.dp,
    state: FamilyTreeState = rememberFamilyTreeState(),
    nodeContent: @Composable (Person) -> Unit = { DefaultPersonCard(it) }
) {
    val density = LocalDensity.current
    val nodeWidthPx = with(density) { nodeWidth.toPx() }
    val nodeHeightPx = with(density) { nodeHeight.toPx() }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportSize = Size(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())

        // Calculate the visible area in Graph Coordinates
        // Formula: (ViewportBoundary - Offset) / Scale
        val visibleRect = Rect(
            left = -state.offset.x / state.scale,
            top = -state.offset.y / state.scale,
            right = (viewportSize.width - state.offset.x) / state.scale,
            bottom = (viewportSize.height - state.offset.y) / state.scale
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        state.onTransform(zoom, pan)
                    }
                }
        ) {
            // 1. Drawing Layer (Canvas)
            // We apply transformations to the Canvas itself
            Canvas(modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = state.scale,
                    scaleY = state.scale,
                    translationX = state.offset.x,
                    translationY = state.offset.y
                )
            ) {
                drawUnionLines(graph, visibleRect, nodeWidthPx, nodeHeightPx)
            }

            // 2. Node Layer (Subcomposition)
            FamilyTreeSubcomposer(
                graph = graph,
                visibleRect = visibleRect,
                state = state,
                nodeWidth = nodeWidth,
                nodeHeight = nodeHeight,
                nodeContent = nodeContent
            )
        }
    }
}

@Composable
private fun FamilyTreeSubcomposer(
    graph: FamilyGraph,
    visibleRect: Rect,
    state: FamilyTreeState,
    nodeWidth: Dp,
    nodeHeight: Dp,
    nodeContent: @Composable (Person) -> Unit
) {
    SubcomposeLayout(modifier = Modifier.fillMaxSize()) { constraints ->
        // Identify persons within the viewport (with a small buffer/margin)
        val buffer = 100f
        val visiblePersons = graph.persons.values.filter { person ->
            val personRect = Rect(person.x, person.y, person.x + 400f, person.y + 200f) // approximate max size
            visibleRect.inflate(buffer).overlaps(personRect)
        }

        val nodePlaceables = visiblePersons.map { person ->
            // subcompose only visible persons
            val placeable = subcompose(person.id) {
                Box(modifier = Modifier.size(nodeWidth, nodeHeight)) {
                    nodeContent(person)
                }
            }.map { it.measure(Constraints()) }

            person to placeable
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            nodePlaceables.forEach { (person, placeables) ->
                placeables.forEach { placeable ->
                    // Map graph coordinates to screen coordinates
                    val screenX = (person.x * state.scale) + state.offset.x
                    val screenY = (person.y * state.scale) + state.offset.y

                    placeable.placeRelative(screenX.roundToInt(), screenY.roundToInt())
                }
            }
        }
    }
}

// --- Optimized Drawing Logic ---

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawUnionLines(
    graph: FamilyGraph,
    visibleRect: Rect,
    nodeWidthPx: Float,
    nodeHeightPx: Float
) {
    val centerX = nodeWidthPx / 2
    val centerY = nodeHeightPx / 2
    val unionLineDrop = 40.dp.toPx()

    graph.unions.forEach { union ->
        // Only draw if the union is roughly near the viewport
        if (!visibleRect.inflate(200f).contains(Offset(union.x, union.y))) return@forEach

        val spouseIds = union.spouseIds
        if (spouseIds.isEmpty()) return@forEach

        val s1 = graph.persons[spouseIds[0]] ?: return@forEach
        val s2 = if (spouseIds.size > 1) graph.persons[spouseIds[1]] else null
        val unionColor = if (union.isIncestuous) Color.Red else Color.DarkGray
        val unionStroke = if (union.isIncestuous) 4f else 2f

        // 1. Line between spouses
        if (s2 != null) {
            drawLine(
                color = unionColor,
                start = Offset(s1.x + centerX, s1.y + centerY),
                end = Offset(s2.x + centerX, s2.y + centerY),
                strokeWidth = unionStroke
            )
        }

        // 2. Vertical stem from union
        drawLine(
            color = unionColor,
            start = Offset(union.x + centerX, union.y + centerY),
            end = Offset(union.x + centerX, union.y + unionLineDrop),
            strokeWidth = unionStroke
        )

        // 3. Children connectors
        if (union.childrenIds.isNotEmpty()) {
            val children = union.childrenIds.mapNotNull { graph.persons[it] }
            if (children.isNotEmpty()) {
                val firstX = children.minOf { it.x }
                val lastX = children.maxOf { it.x }
                val childYTop = children.first().y - 20.dp.toPx()

                // Horizontal connector bar
                drawLine(
                    color = Color.Gray,
                    start = Offset(firstX + centerX, childYTop),
                    end = Offset(lastX + centerX, childYTop),
                    strokeWidth = 2f
                )

                // Vertical connector to horizontal bar
                drawLine(
                    color = unionColor,
                    start = Offset(union.x + centerX, union.y + unionLineDrop),
                    end = Offset(union.x + centerX, childYTop),
                    strokeWidth = unionStroke
                )

                // Individual vertical lines to children
                children.forEach { child ->
                    val isAdopted = s1.adoptiveChildrenIds.contains(child.id) ||
                            (s2?.adoptiveChildrenIds?.contains(child.id) == true)

                    drawLine(
                        color = Color.Gray,
                        start = Offset(child.x + centerX, childYTop),
                        end = Offset(child.x + centerX, child.y),
                        strokeWidth = 2f,
                        pathEffect = if (isAdopted) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyTree(
    graph: FamilyGraph,
    modifier: Modifier = Modifier,
    nodeContent: @Composable (Person) -> Unit = { person -> DefaultPersonCard(person) }
) {
    // Separate scroll states for each axis
    val verticalState = rememberScrollState()
    val horizontalState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize() // Fill the available screen space
            .verticalScroll(verticalState) // Outer scroll (Vertical)
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(horizontalState) // Inner scroll (Horizontal)
                .wrapContentSize(Alignment.TopStart) // Allow inner box to be larger than screen
        ) {
            val density = LocalDensity.current
            val graphWidthDp = with(density) { graph.width.toDp() }
            val graphHeightDp = with(density) { graph.height.toDp() }
            val canvasWidth = maxOf(graphWidthDp, 100.dp)
            val canvasHeight = maxOf(graphHeightDp, 100.dp)

            Box(modifier = Modifier.size(graphWidthDp, graphHeightDp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    graph.unions.forEach { union ->
                        val spouseIds = union.spouseIds

                        // Proceed if there is at least one spouse (Single parent or Couple)
                        if (spouseIds.isNotEmpty()) {
                            val s1 = graph.persons[spouseIds[0]]
                            // Try to get s2 if it exists
                            val s2 = if (spouseIds.size > 1) graph.persons[spouseIds[1]] else null

                            if (s1 != null) {
                                val cardWidthPx = 100.dp.toPx()
                                val cardHeightPx = 60.dp.toPx()
                                val centerX = cardWidthPx / 2
                                val centerY = cardHeightPx / 2

                                val unionColor = if (union.isIncestuous) Color.Red else Color.Black
                                val unionStroke = if (union.isIncestuous) 6f else 3f

                                // If we have a second spouse, draw the horizontal connector
                                if (s2 != null) {
                                    drawLine(
                                        color = unionColor,
                                        start = Offset(s1.x + centerX, s1.y + centerY),
                                        end = Offset(s2.x + centerX, s2.y + centerY),
                                        strokeWidth = unionStroke
                                    )
                                }

                                // Vertical line down from Union midpoint
                                // Union position (x,y) is calculated by LayoutEngine.
                                // LayoutEngine puts single parent union at the parent's location?
                                // Let's verify LayoutEngine logic for single parent:
                                // if (spouses.isNotEmpty()) { avgX = sumX / size ... } -> yes, if size=1, avgX = s1.x

                                val unionLineDrop = 50.dp.toPx()
                                drawLine(
                                    color = unionColor,
                                    start = Offset(union.x + centerX, union.y + centerY),
                                    end = Offset(union.x + centerX, union.y + unionLineDrop),
                                    strokeWidth = unionStroke
                                )

                                // Horizontal line across children
                                if (union.childrenIds.isNotEmpty()) {
                                    val children =
                                        union.childrenIds.mapNotNull { graph.persons[it] }
                                    if (children.isNotEmpty()) {
                                        val firstC = children.minByOrNull { it.x }
                                        val lastC = children.maxByOrNull { it.x }

                                        if (firstC != null && lastC != null) {
                                            val childY = firstC.y - (20.dp.toPx())

                                            drawLine(
                                                color = Color.Black,
                                                start = Offset(firstC.x + centerX, childY),
                                                end = Offset(lastC.x + centerX, childY),
                                                strokeWidth = 3f
                                            )

                                            drawLine(
                                                color = unionColor,
                                                start = Offset(
                                                    union.x + centerX,
                                                    union.y + unionLineDrop
                                                ),
                                                end = Offset(union.x + centerX, childY),
                                                strokeWidth = unionStroke
                                            )

                                            // Vertical lines to each child
                                            children.forEach { child ->
                                                // Check Adoption
                                                // Check s1 adoption list, and s2 (if exists)
                                                val isAdopted =
                                                    s1.adoptiveChildrenIds.contains(child.id) ||
                                                            (s2?.adoptiveChildrenIds?.contains(child.id) == true)

                                                val pathEffect = if (isAdopted) {
                                                    PathEffect.dashPathEffect(
                                                        floatArrayOf(
                                                            10f,
                                                            10f
                                                        ), 0f
                                                    )
                                                } else null

                                                drawLine(
                                                    color = Color.Black,
                                                    start = Offset(child.x + centerX, childY),
                                                    end = Offset(child.x + centerX, child.y),
                                                    strokeWidth = 3f,
                                                    pathEffect = pathEffect
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Draw Nodes
                graph.persons.values.forEach { person ->
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(person.x.toInt(), person.y.toInt()) }
                            .width(100.dp)
                            .height(60.dp)
                    ) {
                        nodeContent(person)
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultPersonCard(person: Person) {
    Card(modifier = Modifier.fillMaxSize().border(1.dp, Color.Gray)) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = person.name, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            if (person.birthDate != null) {
                Text(text = person.birthDate.year.toString(), style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun FamilyTreePreview() {
    val density = LocalDensity.current

    // 1. Create Data
    val p1 = Person("1", "John", LocalDate(1950, 1, 1))
    val p2 = Person("2", "Mary", LocalDate(1952, 5, 20))
    val p3 = Person("3", "Child1", LocalDate(1980, 2, 10))
    val p4 = Person("4", "Child2", LocalDate(1985, 6, 15))
    val aunt = Person("5", "Aunt", LocalDate(1982, 3, 3))
    val nephew = Person("6", "Nephew", LocalDate(2005, 1, 1))
    val adoptedChild = Person("7", "Adopted", LocalDate(2010, 1, 1))

    p1.spouseIds.add(p2.id)
    p2.spouseIds.add(p1.id)
    p1.childrenIds.addAll(listOf(p3.id, p4.id, aunt.id))
    p2.childrenIds.addAll(listOf(p3.id, p4.id, aunt.id))

    p3.childrenIds.add(nephew.id)

    // Aunt marries Nephew (Incest)
    aunt.spouseIds.add(nephew.id)
    nephew.spouseIds.add(aunt.id)

    // Adoption
    p4.adoptiveChildrenIds.add(adoptedChild.id)

    val u1 = Union("u1", listOf(p1.id, p2.id), listOf(p3.id, p4.id, aunt.id))
    val u2 = Union("u2", listOf(aunt.id, nephew.id), emptyList(), isIncestuous = true)
    // Union for p4 (single parent adoption)
    val u3 = Union("u3", listOf(p4.id), listOf(adoptedChild.id))
    // Union for p3 (Child1) -> Nephew (single parent biological)
    val u4 = Union("u4", listOf(p3.id), listOf(nephew.id))

    val persons = mapOf(
        p1.id to p1, p2.id to p2, p3.id to p3, p4.id to p4, aunt.id to aunt, nephew.id to nephew, adoptedChild.id to adoptedChild
    )
    val unions = listOf(u1, u2, u3, u4)
    val graph = FamilyGraph(persons, unions)

    val xSpacing = with(density) { 150.dp.toPx() }
    val ySpacing = with(density) { 160.dp.toPx() }

    LayoutEngine.calculateLayout(graph, xSpacing, ySpacing)

    LazyFamilyTree(graph) {
        Card(elevation = androidx.compose.material3.CardDefaults.cardElevation(8.dp)) {
            Column(Modifier.padding(8.dp)) {
                Text(it.name)
                Text(it.birthDate.toString())
            }
        }
    }
}
