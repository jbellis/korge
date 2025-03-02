package com.soywiz.kds

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.kds.lock.Lock

open class ConcurrentPool<T : Any>(private val reset: (T) -> Unit = {}, preallocate: Int = 0, private val gen: (Int) -> T)
    : Pool<T>(reset, preallocate, gen) {
    private val lock = Lock()

    override fun alloc(): T {
        return lock { super.alloc() }
    }

    override fun clear() {
        lock { super.clear() }
    }

    override fun free(element: T) {
        lock { super.free(element) }
    }
}

/**
 * Structure containing a set of reusable objects.
 *
 * The method [alloc] retrieves from the pool or allocates a new object,
 * while the [free] method pushes back one element to the pool and resets it to reuse it.
 */
open class Pool<T : Any>(private val reset: (T) -> Unit = {}, preallocate: Int = 0, private val gen: (Int) -> T) {
    companion object {
        fun <T : Poolable> fromPoolable(preallocate: Int = 0, gen: (Int) -> T): Pool<T> =
            Pool(reset = { it.reset() }, preallocate = preallocate, gen = gen)
    }

    constructor(preallocate: Int = 0, gen: (Int) -> T) : this({}, preallocate, gen)

    private val items = Stack<T>()
    private var lastId = 0

    val totalAllocatedItems get() = lastId
    val totalItemsInUse get() = totalAllocatedItems - itemsInPool
    val itemsInPool: Int get() = items.size

    init {
        for (n in 0 until preallocate) items.push(gen(lastId++))
    }

    open fun alloc(): T {
        return if (items.isNotEmpty()) items.pop() else gen(lastId++)
    }

    interface Poolable {
        fun reset()
    }

    open fun clear() {
        items.clear()
        lastId = 0
    }

    open fun free(element: T) {
        reset(element)
        items.push(element)
    }

    fun freeNotNull(element: T?) {
        if (element == null) return
        free(element)
    }

    fun free(vararg elements: T) { elements.fastForEach { free(it) } }
    fun free(elements: Iterable<T>) { for (element in elements) free(element) }
    fun free(elements: List<T>) { elements.fastForEach { free(it) } }

    inline operator fun <R> invoke(callback: (T) -> R): R = alloc(callback)

    inline fun <R> alloc(callback: (T) -> R): R {
        val temp = alloc()
        try {
            return callback(temp)
        } finally {
            free(temp)
        }
    }

    inline fun <R> allocMultiple(count: Int, temp: FastArrayList<T> = FastArrayList(), callback: (FastArrayList<T>) -> R): R {
        temp.clear()
        for (n in 0 until count) temp.add(alloc())
        try {
            return callback(temp)
        } finally {
            while (temp.isNotEmpty()) free(temp.removeLast())
        }
    }

    inline fun <R> allocThis(callback: T.() -> R): R {
        val temp = alloc()
        try {
            return callback(temp)
        } finally {
            free(temp)
        }
    }

    override fun hashCode(): Int = items.hashCode()
    override fun equals(other: Any?): Boolean = (other is Pool<*>) && this.items == other.items && this.itemsInPool == other.itemsInPool
}
