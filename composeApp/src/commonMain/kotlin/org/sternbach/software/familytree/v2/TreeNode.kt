package org.sternbach.software.familytree.v2
data class TreeNode(
    val id: String,
    val name: String,
    val children: List<TreeNode> = listOf(),
    var parent: TreeNode? = null // Add a reference to the parent node
) {
    init {
        children.forEach { it.parent = this } // Set the parent for each child
    }
}
fun Person.toTreeNode(): TreeNode {
    return TreeNode(
        id = id.toString(),
        name = name,
        children = children.map { it.toTreeNode() }
    )
}