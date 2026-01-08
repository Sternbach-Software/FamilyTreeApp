package org.sternbach.software.familytree.v2
import kotlin.math.absoluteValue

class BuchheimWalkerAlgorithm(
    val tree: TreeNode,
    val siblingSeparation: Float = 200f,
    val subtreeSeparation: Float = 200f,
    val levelSeparation: Float = 300f,
    val levelHeight: Float = 200f // Define the height of each level of the tree
) {

    private val nodeDataMap = mutableMapOf<String, NodeData>()

    init {
        firstWalk(tree)
        println("First walk: $nodeDataMap")
        secondWalk(tree, 0f)
    }private fun firstWalk(node: TreeNode, depth: Int = 0, number: Int = 1) {
        val nodeData = nodeDataMap[node.id] ?: NodeData().also { nodeDataMap[node.id] = it }

        nodeData.depth = depth
        nodeData.number = number

        if (node.children.isEmpty()) {
            // If it's a leaf node and it's the first node in its siblings
            if (isLeaf(node) && isFirstSibling(node)) {
                nodeData.x = nextLeft(node)
            }
        } else {
            // Internal node
            var mid = 0f
            var numChildren = node.children.size

            node.children.forEachIndexed { index, child ->
                firstWalk(child, depth + 1, number + index)
                val childData = nodeDataMap[child.id]!!
                mid += childData.x // Sum up children's x-coordinates for midpoint calculation
            }

            mid /= numChildren // Compute the midpoint of children

            if (numChildren == 1) {
                nodeData.x = node.children.first().let { nodeDataMap[it.id]!!.x }
            } else {
                nodeData.x = mid
            }
        }

        // Set preliminary x coordinate
        if (isFirstSibling(node)) {
            nodeData.x = nextLeft(node)
        } else {
            getPreviousSibling(node)?.let { prevSibling ->
                println("Previous sibling: ${prevSibling.id}")
                println("Previous sibling data: ${nodeDataMap[prevSibling.id]?.x}")
                val prevSiblingData = nodeDataMap[prevSibling.id]!! //we know it is not null because of the if statement above which checks if it is the first sibling
                nodeData.x = prevSiblingData.x + prevSiblingData.mod + siblingSeparation
            }
        }

        if (node.children.isNotEmpty()) {
            executeShifts(node)
        }
    }

    private fun isLeaf(node: TreeNode): Boolean {
        return node.children.isEmpty()
    }
    private fun isFirstSibling(node: TreeNode): Boolean {
        val parentNode = node.parent
        if (parentNode != null) {
            // Check if the current node is the first child of its parent
            return parentNode.children.firstOrNull()?.id == node.id
        }
        return false // If the node has no parent, it's considered as the first sibling (root)
    }
    private fun getPreviousSibling(node: TreeNode): TreeNode? {
        val parentNode = node.parent
        if (parentNode != null) {
            val siblings = parentNode.children
            val nodeIndex = siblings.indexOfFirst { it.id == node.id }
            if (nodeIndex > 0) {
                // Return the previous sibling
                return siblings[nodeIndex - 1]
            }
        }
        return null // If the node is the first child or has no parent, there is no previous sibling
    }


    private fun executeShifts(node: TreeNode) {
        var shift = 0f
        var change = 0f

        node.children.asReversed().forEach { child ->
            val childData = nodeDataMap[child.id]!!
            childData.x += shift
            childData.shift = shift
            childData.change = change
            shift += childData.shift
            change += childData.change
        }

        if (node.children.isNotEmpty()) {
            val nodeData = nodeDataMap[node.id]!!
            val childrenMiddle = (nodeDataMap[node.children.first().id]!!.x +
                    nodeDataMap[node.children.last().id]!!.x) / 2

            shift = nodeData.x - childrenMiddle
            nodeData.x += shift

            redistribute(node, shift, change / node.children.size)
        }
    }

    private fun redistribute(node: TreeNode, shift: Float, change: Float) {
        var shiftAccumulator = shift
        var changeAccumulator = change

        node.children.forEach { child ->
            val childData = nodeDataMap[child.id]!!
            childData.x += shiftAccumulator
            shiftAccumulator += changeAccumulator + childData.shift
            changeAccumulator += childData.change
        }
    }

    private fun nextLeft(node: TreeNode): Float {
        var left = 0f
        var tempNode: TreeNode? = node

        while (tempNode != null) {
            val tempNodeData = nodeDataMap[tempNode.id]
            left += tempNodeData?.mod ?: 0f
            tempNode = if (tempNode.children.isNotEmpty()) {
                tempNode.children.first()
            } else {
                null
            }
        }

        return left
    }
    private fun secondWalk(node: TreeNode, modSum: Float) {
        val nodeData = nodeDataMap[node.id]
        nodeData?.let {
            it.x += modSum
            it.mod += modSum // Update the mod as the final modifier value for the node

            for (child in node.children) {
                secondWalk(child, modSum + it.mod)
            }
        }
    }


    fun getPositions(): Map<String, Pair<Float, Float>> {
        return nodeDataMap.mapValues { (_, v) ->
            Pair(v.x.absoluteValue, (v.depth * levelHeight).absoluteValue) // x is as calculated, y is based on depth
        }
    }
    private data class NodeData(
        var x: Float = 0f,
        var y: Float = 0f,
        var mod: Float = 0f,
        var depth: Int = -1,
        var number: Int = -1,
        var shift: Float = 0f,
        var change: Float = 0f,
        var firstWalk: Boolean = false
    )
}
