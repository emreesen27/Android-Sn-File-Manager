package com.sn.snfilemanager.core.extensions

fun <T> MutableCollection<T>.removeFirst(): T {
    val iterator = iterator()
    val element = iterator.next()
    iterator.remove()
    return element
}

fun <K, V> MutableMap<K, V>.removeFirst(): Map.Entry<K, V> {
    val iterator = iterator()
    val element = iterator.next()
    iterator.remove()
    return element
}

fun <T> MutableCollection<T>.removeFirst(predicate: (T) -> Boolean): T? {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (predicate(element)) {
            iterator.remove()
            return element
        }
    }
    return null
}

fun <K, V> MutableMap<K, V>.removeFirst(predicate: (Map.Entry<K, V>) -> Boolean): Map.Entry<K, V>? = entries.removeFirst(predicate)
