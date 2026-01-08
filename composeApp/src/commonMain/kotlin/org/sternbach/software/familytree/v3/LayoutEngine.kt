package sternbach.software.familytreecompose


object LayoutEngine {

    fun computeGenerations(graph: FamilyGraph): Map<String, Int> {
        val persons = graph.persons
        val inDegree = mutableMapOf<String, Int>().apply {
            persons.keys.forEach { put(it, 0) }
        }

        val adjacency = mutableMapOf<String, MutableList<String>>()

        // Build adjacency for parent -> child
        for (person in persons.values) {
            for (childId in person.childrenIds) {
                adjacency.getOrPut(person.id) { mutableListOf() }.add(childId)
                inDegree[childId] = (inDegree[childId] ?: 0) + 1
            }
            for (childId in person.adoptiveChildrenIds) {
                adjacency.getOrPut(person.id) { mutableListOf() }.add(childId)
                inDegree[childId] = (inDegree[childId] ?: 0) + 1
            }
        }

        // Topological sort / Level assignment
        val queue = ArrayDeque<String>()
        val generation = mutableMapOf<String, Int>()

        // Initialize ancestors
        inDegree.forEach { (id, deg) ->
            if (deg == 0) {
                generation[id] = 0
                queue.add(id)
            }
        }

        // BFS to assign generations (child = parent + 1)
        while (!queue.isEmpty()) {
            val currentId = queue.removeFirst()
            val currentGen = generation[currentId] ?: 0

            adjacency[currentId]?.forEach { childId ->
                val prevGen = generation[childId] ?: -1
                val newGen = if (prevGen > (currentGen + 1)) prevGen else (currentGen + 1)
                generation[childId] = newGen

                inDegree[childId] = (inDegree[childId] ?: 0) - 1
                if ((inDegree[childId] ?: 0) == 0) {
                    queue.add(childId)
                }
            }
        }

        // Handle remaining nodes
        persons.keys.forEach { id ->
            if (!generation.containsKey(id)) {
                generation[id] = 0
            }
        }

        // --- Cycle / Spouse Constraint Handling ---
        var changed = true
        var iterationCount = 0
        val maxIterations = (persons.size * 2).coerceAtLeast(100) // Safety break

        while (changed && iterationCount < maxIterations) {
            changed = false
            iterationCount++

            for (union in graph.unions) {
                val spouseGens = union.spouseIds.mapNotNull { generation[it] }
                if (spouseGens.isNotEmpty()) {
                    var maxGen = 0
                    for (g in spouseGens) {
                        if (g > maxGen) maxGen = g
                    }

                    union.spouseIds.forEach { spouseId ->
                        if ((generation[spouseId] ?: 0) < maxGen) {
                            generation[spouseId] = maxGen
                            changed = true
                            propagateDown(spouseId, maxGen, adjacency, generation)
                        }
                    }
                }
            }
        }

        return generation
    }

    private fun propagateDown(
        personId: String,
        currentGen: Int,
        adjacency: Map<String, List<String>>,
        generation: MutableMap<String, Int>
    ) {
        val children = adjacency[personId] ?: return
        children.forEach { childId ->
            val childGen = generation[childId] ?: 0
            if (childGen <= currentGen) {
                // To avoid infinite recursion in direct parent-child loops, we could check here too
                // But the outer loop limit handles the overall stability.
                generation[childId] = currentGen + 1
                propagateDown(childId, currentGen + 1, adjacency, generation)
            }
        }
    }

    fun calculateLayout(
        graph: FamilyGraph,
        xSpacing: Float,
        ySpacing: Float
    ) {
        val gens = computeGenerations(graph)
        graph.persons.values.forEach { it.generation = gens[it.id] ?: 0 }

        // 1. Assign Layers
        val layers = mutableMapOf<Int, MutableList<Person>>()
        var maxGen = 0
        graph.persons.values.forEach { p ->
            val list = layers.getOrPut(p.generation) { mutableListOf() }
            list.add(p)
            if (p.generation > maxGen) maxGen = p.generation
        }

        // 2. Initial X Assignment
        layers.keys.forEach { gen ->
            layers[gen]?.sortBy { it.id }
            var currentX = 0f
            layers[gen]?.forEach { p ->
                p.x = currentX
                p.y = gen * ySpacing
                currentX += xSpacing
            }
        }

        // 3. Barycenter / Averaging Passes
        val iterations = 5
        for (i in 0 until iterations) {
            // Bottom-Up
            for (gen in maxGen downTo 0) {
                graph.unions.forEach { union ->
                    val spouseGens = union.spouseIds.mapNotNull { graph.persons[it]?.generation }
                    if (spouseGens.contains(gen)) {
                        val children = union.childrenIds.mapNotNull { graph.persons[it] }
                        if (children.isNotEmpty()) {
                            var sumX = 0f
                            children.forEach { sumX += it.x }
                            val avgChildrenX = sumX / children.size

                            val spouses = union.spouseIds.mapNotNull { graph.persons[it] }
                            val totalWidth = (spouses.size - 1) * xSpacing
                            var startX = avgChildrenX - (totalWidth / 2)

                            spouses.forEach { s ->
                                s.x = startX
                                startX += xSpacing
                            }
                        }
                    }
                }
                resolveOverlaps(layers[gen] ?: emptyList(), xSpacing)
            }

            // Top-Down
            for (gen in 0..maxGen) {
                graph.unions.forEach { union ->
                    val spouses = union.spouseIds.mapNotNull { graph.persons[it] }
                    if (spouses.isNotEmpty() && spouses.any { it.generation == gen }) {
                        var sumX = 0f
                        spouses.forEach { sumX += it.x }
                        val unionX = sumX / spouses.size

                        val children = union.childrenIds.mapNotNull { graph.persons[it] }
                        if (children.isNotEmpty()) {
                            val totalChildWidth = (children.size - 1) * xSpacing
                            var startX = unionX - (totalChildWidth / 2)
                            children.forEach { c ->
                                c.x = startX
                                startX += xSpacing
                            }
                        }
                    }
                }
                resolveOverlaps(layers[gen + 1] ?: emptyList(), xSpacing)
            }
        }

        // Final Union Positioning
        graph.unions.forEach { union ->
            val spouses = union.spouseIds.mapNotNull { graph.persons[it] }
            if (spouses.isNotEmpty()) {
                var sumX = 0f
                var sumY = 0f
                spouses.forEach {
                    sumX += it.x
                    sumY += it.y
                }
                val avgX = sumX / spouses.size
                val avgY = sumY / spouses.size
                union.x = avgX
                union.y = avgY
            }
        }

        // Calculate total graph bounds
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE

        fun updateBounds(x: Float, y: Float) {
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }

        graph.persons.values.forEach { updateBounds(it.x, it.y) }
        graph.unions.forEach { updateBounds(it.x, it.y) }

        if (graph.persons.isEmpty()) {
            graph.width = 0f
            graph.height = 0f
        } else {
            val padding = xSpacing / 2
            val shiftX = -minX + padding
            val shiftY = -minY + padding

            graph.persons.values.forEach {
                it.x += shiftX
                it.y += shiftY
            }
            graph.unions.forEach {
                it.x += shiftX
                it.y += shiftY
            }

            graph.width = (maxX - minX) + (2 * padding)
            graph.height = (maxY - minY) + (2 * padding)
        }
    }

    private fun resolveOverlaps(layer: List<Person>, minSpacing: Float) {
        if (layer.isEmpty()) return
        val sorted = layer.sortedBy { it.x }

        for (i in 0 until sorted.size - 1) {
            val p1 = sorted[i]
            val p2 = sorted[i+1]
            if (p2.x < p1.x + minSpacing) {
                p2.x = p1.x + minSpacing
            }
        }
    }
}