package org.sternbach.software.familytree

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform