package org.sternbach.software.familytree.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import kotlin.random.Random

const val seed = 4
val random = Random(seed)

const val rotation = /*18*/0F

@Composable
fun PannableFamilyTree(root: Person) {
    // State to hold the offset for panning
    val offset = remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            FamilyTree(
                root = root
            ) {
                Card(elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.rotate(rotation)) {
                    Text(
                        it.name,
                        Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/***
 *
 * Every person/[FamilyTree] is responsible for drawing themselves, their children, their spouse, and any of their children's in-laws.
 * If their spouse has parents, they should add the parents to [parentsOfPreviousGeneration], and [root]'s parent will draw them.
 *
 * There are 4 straight/orthogonal lines drawn between people:
 * 1. the child connector:          Vertical line from the center-top of the child card to halfway between the parents and the children.
 * 2. the sibling connector:        Horizontal line from the child connector of the left child to the child connector of the right child.
 * 3. the spouse connector:         Horizontal line from the right edge of the left parent to the left edge of the right parent.
 * 4. the spouse-sibling connector: Vertical line from the center of the spouse connector to the sibling connector.
 * */
@Composable
fun FamilyTree(
    root: Person,
    modifier: Modifier = Modifier,
    parentsOfPreviousGeneration: SnapshotStateList<Person> = remember { mutableStateListOf() },
    personContent: @Composable (Person) -> Unit = { person ->
        Text(
            person.name,
        )
    }
) {
    var leftChildCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var rightChildCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var subtreeCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val parentsOfSameGeneration: SnapshotStateList<Person> =
        remember { mutableStateListOf<Person>() }


    Column(
        Modifier
            .background(
                Color(
                    random.nextInt(),
                    random.nextInt(),
                    random.nextInt(),
                )
            )
            .onGloballyPositioned { subtreeCoords = it }
            .drawBehind {
                //draw line from left child to right child, or if only one child, from left child to parents connector

                if (leftChildCoordinates != null && rightChildCoordinates != null) {

                    val leftPos =
                        subtreeCoords?.localPositionOf(leftChildCoordinates!!, Offset.Zero)!!
                    val rightPos =
                        subtreeCoords?.localPositionOf(rightChildCoordinates!!, Offset.Zero)!!
                    drawLine(
                        Color.Black,
                        Offset(leftPos.x + leftChildCoordinates!!.size.width / 2, leftPos.y),
                        Offset(rightPos.x + rightChildCoordinates!!.size.width / 2, rightPos.y),
                        2F
                    )
                } else if (leftChildCoordinates != null) { //only one child, draw from left child to halfway between spouses
                    val leftPos =
                        subtreeCoords?.localPositionOf(leftChildCoordinates!!, Offset.Zero)!!
                    val spousePos = subtreeCoords?.localPositionOf(subtreeCoords!!, Offset.Zero)!!
                    drawLine(
                        Color.Black,
                        Offset(leftPos.x + leftChildCoordinates!!.size.width / 2, leftPos.y),
                        Offset(spousePos.x + subtreeCoords!!.size.center.x, leftPos.y),
                        2F
                    )

                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            Row {
                PersonLayout(
                    modifier = modifier,
                    person = root,
                    spouseAtEnd = root.spouse != null,
                    content = personContent
                )
                root.spouse?.let {
                    if (it.parent != null) {
                        parentsOfPreviousGeneration.add(it.parent!!)
                    }
                    PersonLayout(
                        person = it,
                        hasParent = false,
                        spouseAtStart = true,
                        content = personContent
                    )
                }
            }

            //row of other parents in this generation; should fill as much as needed, and space children evenly
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                for (parent in parentsOfSameGeneration) {
                    //TODO
                    PersonLayout(
                        person = parent,
                        hasParent = false,
                        content = personContent
                    )
                }
            }
        }
        // Draw the connection lines and children recursively
        if (root.children.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.Center) {
                root.children.forEachIndexed { index, child ->
                    FamilyTree(
                        root = child,
                        modifier = Modifier
                            .let {
                                if (child.spouse == null) it.padding(end = 16.dp)
                                else it
                            }
                            .onGloballyPositioned {
                                val pos = it
                                if (root.children.size == 1) {
                                    leftChildCoordinates = pos
                                    rightChildCoordinates = null
                                } else {
                                    if (index == 0) {
                                        leftChildCoordinates = pos
                                    } else if (index == root.children.lastIndex) {
                                        rightChildCoordinates = pos
                                    }
                                }
                            },
                        parentsOfSameGeneration,
                        personContent = personContent
                    )
                }
            }
        }
    }
}

private val personLayoutPadding = 8.dp

@Composable
private fun PersonLayout(
    modifier: Modifier = Modifier,
    person: Person,
    spouseAtEnd: Boolean = false,
    spouseAtStart: Boolean = false,
    hasParent: Boolean = true,
    content: @Composable (Person) -> Unit
) {
    Box(
        modifier = modifier
//            .background(Color(random.nextInt(), random.nextInt(), random.nextInt()))
            .padding(personLayoutPadding)
            .drawBehind {
                val centerY = size.center.y
                if (spouseAtEnd) {
                    val endX = size.width + personLayoutPadding.toPx()
                    drawLine(
                        Color.Black,
                        Offset(size.width, centerY),
                        Offset(endX, centerY),
                        2F
                    )
                    if (person.children.isNotEmpty())
                        drawLine(
                            Color.Black,
                            Offset(endX, centerY),
                            Offset(endX, size.height + personLayoutPadding.toPx()),
                            2F
                        )
                }
                if (spouseAtStart) {
                    val endX = 0F - personLayoutPadding.toPx()
                    drawLine(
                        Color.Black,
                        Offset(0F, centerY),
                        Offset(endX, centerY),
                        2F
                    )
                    if (person.children.isNotEmpty())
                        drawLine(
                            Color.Black,
                            Offset(endX, centerY),
                            Offset(endX, size.height + personLayoutPadding.toPx()),
                            2F
                        )
                }

                if (hasParent)
                    drawLine(
                        Color.Black,
                        Offset(size.center.x, 0F),
                        Offset(size.center.x, 0F - personLayoutPadding.toPx()),
                        2F
                    )
            }
    ) {
        content(person)
    }
}

fun randomString(length: Int = 8): String {
    val allowedChars = ('A'..'Z') + ('a'..'z')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

@Composable
fun PreviewFamilyTree() {
//    Timber.plant(Timber.DebugTree())
    val alice = Person(1, "Alice")
    val bob = Person(2, "Bob")
    val sally = Person(3, "Sally")
    val john = Person(4, "John")
    /*p1.associateWithFamily(
        Family(
            p2, listOf(p3, p4)
        )
    )*/
    val jane = Person(5, "Jane")
    val jill = Person(6, "Jill")
    val jack = Person(7, "Jack")
    val jone = Person(8, "Jone")
    val jessica = Person(9, "Jessica")
    val p10 = Person(10, randomString())
    val p11 = Person(11, randomString())
    val p12 = Person(12, randomString())

    val husband1 = Person(13, "Husband 1")
    val husband2 = Person(14, "Husband 2")
    val husband3 = Person(15, "Husband 3")
//    husband2.associateWithFamily(Family(husband3, listOf(husband1)))
    alice.associateWithFamily(
        Family(
            bob,
            listOf(
                sally,
            )
        )
    )
    sally.associateWithFamily(
        Family(
            husband1,
            listOf(
                john,
                jane,
                Person(Int.MAX_VALUE - 1, "Child 1"),
                Person(Int.MAX_VALUE - 2, "Child 2"),
                Person(Int.MAX_VALUE - 3, "Child 3"),
                Person(Int.MAX_VALUE - 4, "Child 4"),
                Person(Int.MAX_VALUE - 5, "Child 5"),
                Person(Int.MAX_VALUE - 6, "Child 6"),
                Person(Int.MAX_VALUE - 7, "Child 7"),
                Person(Int.MAX_VALUE - 8, "Child 8"),
                Person(Int.MAX_VALUE - 9, "Child 9"),
                Person(Int.MAX_VALUE - 10, "Child 10"),
                Person(Int.MAX_VALUE - 11, "Child 11"),
                Person(Int.MAX_VALUE - 12, "Child 12"),
                Person(Int.MAX_VALUE - 13, "Child 13"),
                Person(Int.MAX_VALUE - 14, "Child 14"),
                Person(Int.MAX_VALUE - 15, "Child 15"),
                Person(Int.MAX_VALUE - 16, "Child 16"),
                Person(Int.MAX_VALUE - 17, "Child 17"),

                )
        )
    )
    val sallyInLaw = Person(100, "Sally's In-Law")
    sallyInLaw.associateWithFamily(
        Family(
            Person(101, randomString()),
            listOf(
                husband1,
                Person(14, "Child 2"),
                Person(15, "Child 3"),
            )
        )
    )
    jane.associateWithFamily(
        Family(
            jill,
            listOf(
                jack,
                Person(14, "Child 2"),
                Person(15, "Child 3"),
            )
        )
    )
    jack.associateWithFamily(
        Family(
            jone,
            listOf(
                jessica,
                Person(16, "Child 1"),
                Person(17, "Child 2"),
                Person(18, "Child 3"),
            )
        )
    )
    val jChild1 = Person(20, "Child 1")
    jessica.associateWithFamily(
        Family(
            Person(19, randomString()),
            listOf(
                jChild1,
                Person(21, "Child 2"),
                Person(22, "Child 3"),
                Person(23, "Child 4"),
                Person(24, "Child 5"),
            )
        )
    )
    val kChild1 = Person(26, "Child 1")
    jChild1.associateWithFamily(
        Family(
            Person(25, randomString()),
            listOf(
                kChild1,
                Person(27, "Child 2"),
                Person(28, "Child 3"),
                Person(29, "Child 4"),
                Person(30, "Child 5"),
            )
        )
    )
    val lChild1 = Person(32, "Child 2")
    kChild1.associateWithFamily(
        Family(
            Person(31, randomString()),
            listOf(
                lChild1,
                Person(33, "Child 3"),
                Person(34, "Child 4"),
                Person(35, "Child 5"),
                Person(36, "Child 6"),
            )
        )
    )
    val mChild1 = Person(38, "Child 1")
    lChild1.associateWithFamily(
        Family(
            Person(37, randomString()),
            listOf(
                mChild1,
                Person(39, "Child 2"),
                Person(40, "Child 3"),
                Person(41, "Child 4"),
                Person(42, "Child 5"),
            )
        )
    )
    val nChild1 = Person(44, "Child 2")
    mChild1.associateWithFamily(
        Family(
            Person(43, randomString()),
            listOf(
                nChild1,
                Person(45, "Child 3"),
                Person(46, "Child 4"),
                Person(47, "Child 5"),
                Person(48, "Child 6"),
            )
        )
    )
    val oChild1 = Person(50, "Child 1")
    nChild1.associateWithFamily(
        Family(
            Person(49, randomString()),
            listOf(
                oChild1,
                Person(51, "Child 2"),
                Person(52, "Child 3"),
                Person(53, "Child 4"),
                Person(54, "Child 5"),
            )
        )
    )
    val pChild1 = Person(56, "Child 2")
    oChild1.associateWithFamily(
        Family(
            Person(55, randomString()),
            listOf(
                pChild1,
                Person(57, "Child 3"),
                Person(58, "Child 4"),
                Person(59, "Child 5"),
                Person(60, "Child 6"),
            )
        )
    )
    val qChild1 = Person(62, "Child 1")
    pChild1.associateWithFamily(
        Family(
            Person(61, randomString()),
            listOf(
                qChild1,
                Person(63, "Child 2"),
                Person(64, "Child 3"),
                Person(65, "Child 4"),
                Person(66, "Child 5"),
            )
        )
    )
    val rChild1 = Person(68, "Child 2")
    qChild1.associateWithFamily(
        Family(
            Person(67, randomString()),
            listOf(
                rChild1,
                Person(69, "Child 3"),
                Person(70, "Child 4"),
                Person(71, "Child 5"),
                Person(72, "Child 6"),
            )
        )
    )
    val sChild1 = Person(74, "Child 1")
    rChild1.associateWithFamily(
        Family(
            Person(73, randomString()),
            listOf(
                sChild1,
                Person(75, "Child 2"),
                Person(76, "Child 3"),
                Person(77, "Child 4"),
                Person(78, "Child 5"),
            )
        )
    )
    val tChild1 = Person(80, "Child 2")
    sChild1.associateWithFamily(
        Family(
            Person(79, randomString()),
            listOf(
                tChild1,
                Person(81, "Child 3"),
                Person(82, "Child 4"),
                Person(83, "Child 5"),
                Person(84, "Child 6"),
            )
        )
    )
    val uChild1 = Person(86, "Child 1")
    tChild1.associateWithFamily(
        Family(
            Person(85, randomString()),
            listOf(
                uChild1,
                Person(87, "Child 2"),
                Person(88, "Child 3"),
                Person(89, "Child 4"),
                Person(90, "Child 5"),
            )
        )
    )
    val vChild1 = Person(92, "Child 2")
    uChild1.associateWithFamily(
        Family(
            Person(91, randomString()),
            listOf(
                vChild1,
                Person(93, "Child 3"),
                Person(94, "Child 4"),
                Person(95, "Child 5"),
                Person(96, "Child 6"),
            )
        )
    )
    val wChild1 = Person(98, "Child 1")
    vChild1.associateWithFamily(
        Family(
            Person(97, randomString()),
            listOf(
                wChild1,
                Person(99, "Child 2"),
                Person(100, "Child 3"),
                Person(101, "Child 4"),
                Person(102, "Child 5"),
            )
        )
    )
    val xChild1 = Person(104, "Child 2")
    wChild1.associateWithFamily(
        Family(
            Person(103, randomString()),
            listOf(
                xChild1,
                Person(105, "Child 3"),
                Person(106, "Child 4"),
                Person(107, "Child 5"),
                Person(108, "Child 6"),
            )
        )
    )
    val yChild1 = Person(110, "Child 1")
    xChild1.associateWithFamily(
        Family(
            Person(109, randomString()),
            listOf(
                yChild1,
                Person(111, "Child 2"),
                Person(112, "Child 3"),
                Person(113, "Child 4"),
                Person(114, "Child 5"),
            )
        )
    )
    val zChild1 = Person(116, "Child 2")
    yChild1.associateWithFamily(
        Family(
            Person(115, randomString()),
            listOf(
                zChild1,
                Person(117, "Child 3"),
                Person(118, "Child 4"),
                Person(119, "Child 5"),
                Person(120, "Child 6"),
            )
        )
    )
    val aaChild1 = Person(122, "Child 1")
    zChild1.associateWithFamily(
        Family(
            Person(121, randomString()),
            listOf(
                aaChild1,
                Person(123, "Child 2"),
                Person(124, "Child 3"),
                Person(125, "Child 4"),
                Person(126, "Child 5"),
            )
        )
    )
    val abChild1 = Person(128, "Child 2")
    aaChild1.associateWithFamily(
        Family(
            Person(127, randomString()),
            listOf(
                abChild1,
                Person(129, "Child 3"),
                Person(130, "Child 4"),
                Person(131, "Child 5"),
                Person(132, "Child 6"),
            )
        )
    )



    Column(
        Modifier
            .fillMaxSize()
            .rotate(rotation),
//            .verticalScroll(rememberScrollState())
//            .horizontalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PannableFamilyTree(alice)
    }
}

private fun Person.associateWithFamily(family: Family) {
    children = family.children.onEach { it.parent = this }
    spouse = family.spouse.also { it.spouse = this }
}

data class Person(
    val id: Int,
    var name: String,
    var spouse: Person? = null,
    var parent: Person? = null,
    var children: List<Person> = emptyList(),
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (other is Person && other.id == id)
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "Person(name='$name', spouse=${spouse?.name}, parent=${parent?.name}, children=${children.map { it.name }})"
    }
}

data class Family(
    val spouse: Person,
    val children: List<Person>,
)