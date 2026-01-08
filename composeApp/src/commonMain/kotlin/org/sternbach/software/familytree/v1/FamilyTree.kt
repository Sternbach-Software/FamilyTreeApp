package org.sternbach.software.familytree.v1

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun FamilyTree(modifier: Modifier = Modifier, root: Person) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
//            SpouseCard(modifier, root.copy(name = root.name + "-spouse", parent = null), true)
            SpouseCard(modifier, root, false)
        }
        Row {
            root.children.forEachIndexed { index, child ->
                FamilyTree(
                    Modifier.drawBehind {
                        val mid = size.width / 2
                        if (index > 0) drawLine(//draw left line if siblings to the left (i.e. is not first sibling)
                            Color.Black,
                            Offset(mid, 0f),
                            Offset(0f, 0F)
                        )
                        if (index != root.children.lastIndex) drawLine( //draw right line if siblings to the right (i.e. is not last sibling)
                            Color.Black,
                            Offset(mid, 0f),
                            Offset(size.width, 0F)
                        )
                    },
                    child.copy(name = "${child.name}$index")
                )
            }
        }
    }
}

@Composable
fun SpouseCard(modifier: Modifier, person: Person, isLeft: Boolean) {
    var columnSize: IntSize? by remember { mutableStateOf(null) }
    Column(
        modifier = modifier.onSizeChanged {
            columnSize = it
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VerticalLine(person.parent != null)
        Box(
            Modifier
                .padding(horizontal = 5.dp)
                .border(1.dp, Color.Black)
                .drawBehind {
                    val mid = size.height / 2
                    // if is left, draw from right/end of name to right/end of column,
                    // else draw from left/start of name to left/start of column - to make it look like a line is going from the name to the column
                    drawLine(
                        Color.Black,
                        Offset(if (isLeft) size.width else 0F, mid),
                        Offset(if (isLeft) columnSize?.width?.toFloat() ?: 0F else 0F, mid)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = person.name)
        }
    }
}

@Composable
private fun VerticalLine(drawLine: Boolean) {
    Box(
        Modifier
            .background(if(drawLine) Color.Black else Color.Transparent)
            .size(0.5.dp, 5.dp)
    )
}

@Composable
fun PreviewFamilyTree() {
    val alice = Person("Alice", null)
    val bob = Person("Bob", alice)
    alice.spouse = bob
    val sally = Person("Sally")
    val john = Person("John")
    val jane = Person("Jane")
    val jill = Person("Jill")
    val jack = Person("Jack")
    val jone = Person("Jone")
    val jessica = Person("Jessica")
    bob.children = listOf(
        sally,
        john
    ).onEach { it.parent = bob }
    john.children = listOf(
        jane,
        jack,
        jill,
        jone,
        jessica
    ).onEach { it.parent = john }
    jill.children = listOf(
        Person("Jill's kid 1"),
        Person("Jill's kid 2")
    ).onEach { it.parent = jill }
    jessica.children = listOf(
        Person("Jessica's kid 1"),
        Person("Jessica's kid 2")
    ).onEach { it.parent = jessica }
    Surface(
        Modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        FamilyTree(Modifier, bob)
    }
}

data class Person(
    val name: String,
    var spouse: Person? = null,
    var parent: Person? = null,
    var children: List<Person> = emptyList()
)