package com.soywiz.korma.geom

import com.soywiz.kds.DoubleArrayList
import com.soywiz.kds.Extra
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.kds.mapDouble
import com.soywiz.korma.internal.appendNice
import com.soywiz.korma.math.roundDecimalPlaces
import kotlin.math.sqrt

interface IVectorArrayList : Extra {
    val closed: Boolean
    val size: Int
    val dimensions: Int
    fun get(index: Int, dim: Int): Double
    fun getGeneric(index: Int): GenericVector = GenericVector(dimensions, DoubleArray(dimensions) { get(index, it) })
}

inline fun IVectorArrayList.getOrElse(index: Int, dim: Int, default: Double = 0.0): Double {
    if (index < 0 || index >= size) return default
    if (dim < 0 || dim >= dimensions) return default
    return this.get(index, dim)
}

inline fun <T : IVectorArrayList> T.fastForEachGeneric(block: T.(n: Int) -> Unit): Unit {
    for (n in 0 until size) {
        block(this, n)
    }
}

fun IVectorArrayList.getX(index: Int): Double = get(index, 0)
fun IVectorArrayList.getY(index: Int): Double = get(index, 1)
fun IVectorArrayList.getZ(index: Int): Double = get(index, 2)

class VectorArrayList(
    override val dimensions: Int,
    capacity: Int = 7,
) : IVectorArrayList, Extra by Extra.Mixin() {
    val data = DoubleArrayList(capacity * dimensions)

    override var closed: Boolean = false
    override val size: Int get() = data.size / dimensions

    override fun get(index: Int, dim: Int): Double = data[index * dimensions + dim]
    override fun getGeneric(index: Int): GenericVector = GenericVector(dimensions, data.data, index * dimensions)

    private fun checkDimensions(dim: Int) {
        if (dim != dimensions) error("Invalid dimensions $dim != $dimensions")
    }

    fun set(index: Int, dim: Int, value: Double) {
        data[index * dimensions + dim] = value
    }
    private inline fun setInternal(dims: Int, index: Int, block: (Int) -> Unit) {
        checkDimensions(dims)
        block(index * dims)
    }
    fun set(index: Int, vararg values: Double) {
        val rindex = index * dimensions
        for (n in 0 until dimensions) data[rindex + n] = values[n]
    }
    fun set(index: Int, v0: Double) = setInternal(1, index) { data[it] = v0 }
    fun set(index: Int, v0: Double, v1: Double) = setInternal(2, index) { data[it] = v0; data[it + 1] = v1 }
    fun set(index: Int, v0: Double, v1: Double, v2: Double) = setInternal(3, index) { data[it] = v0; data[it + 1] = v1; data[it + 2] = v2 }
    fun set(index: Int, v0: Double, v1: Double, v2: Double, v3: Double) = setInternal(4, index) { data[it] = v0; data[it + 1] = v1; data[it + 2] = v2; data[it + 3] = v3 }
    fun set(index: Int, v0: Double, v1: Double, v2: Double, v3: Double, v4: Double) = setInternal(5, index) { data[it] = v0; data[it + 1] = v1; data[it + 2] = v2; data[it + 3] = v3; data[it + 4] = v4 }
    fun set(index: Int, v0: Double, v1: Double, v2: Double, v3: Double, v4: Double, v5: Double) = setInternal(6, index) { data[it] = v0; data[it + 1] = v1; data[it + 2] = v2; data[it + 3] = v3; data[it + 4] = v4; data[it + 5] = v5 }

    fun set(index: Int, values: DoubleArray, offset: Int = 0) {
        val rindex = index * dimensions
        for (n in 0 until dimensions) data[rindex + n] = values[offset + n]
    }
    fun set(index: Int, vector: GenericVector) {
        set(index, vector.data, vector.offset)
    }
    fun set(index: Int, vector: IGenericVector) {
        val rindex = index * dimensions
        for (n in 0 until dimensions) data[rindex + n] = vector.get(n)
    }
    fun add(values: DoubleArrayList, offset: Int = 0, count: Int = 1) {
        add(values.data, offset, count)
    }
    fun add(values: DoubleArray, offset: Int = 0, count: Int = 1) {
        data.add(values, offset, dimensions * count)
    }
    fun add(v0: Double) = checkDimensions(1).also { data.add(v0) }
    fun add(v0: Double, v1: Double) = checkDimensions(2).also { data.add(v0, v1) }
    fun add(v0: Double, v1: Double, v2: Double) = checkDimensions(3).also { data.add(v0, v1, v2) }
    fun add(v0: Double, v1: Double, v2: Double, v3: Double) = checkDimensions(4).also { data.add(v0, v1, v2, v3) }
    fun add(v0: Double, v1: Double, v2: Double, v3: Double, v4: Double) = checkDimensions(5).also { data.add(v0, v1, v2, v3, v4) }
    fun add(v0: Double, v1: Double, v2: Double, v3: Double, v4: Double, v5: Double) = checkDimensions(6).also { data.add(v0, v1, v2, v3, v4, v5) }
    fun add(vararg values: Double) = checkDimensions(values.size).also { data.add(values) }
    fun add(vector: GenericVector) {
        add(vector.data, vector.offset)
    }
    fun add(vector: IGenericVector) {
        for (n in 0 until dimensions) data.add(vector[n])
    }

    fun vectorToStringBuilder(index: Int, out: StringBuilder) {
        out.appendGenericArray(dimensions) { appendNice(this@VectorArrayList.get(index, it)) }
    }

    fun vectorToString(index: Int): String = buildString { vectorToStringBuilder(index, this) }

    override fun equals(other: Any?): Boolean = other is VectorArrayList && this.dimensions == other.dimensions && this.data == other.data
    override fun hashCode(): Int = data.hashCode()

    override fun toString(): String = buildString {
        append("VectorArrayList[${this@VectorArrayList.size}](\n")
        for (n in 0 until this@VectorArrayList.size) {
            if (n != 0) append(", \n")
            append("   ")
            this@VectorArrayList.vectorToStringBuilder(n, this)
        }
        append("\n)")
    }

    fun add(other: VectorArrayList, index: Int, count: Int = 1) {
        add(other.data.data, index * dimensions, count)
    }

    fun clone(): VectorArrayList = VectorArrayList(dimensions, this.size).also { it.add(this, 0, size) }

    fun roundDecimalPlaces(places: Int): VectorArrayList {
        for (n in 0 until data.size) data[n] = data[n].roundDecimalPlaces(places)
        return this
    }
}

fun vectorArrayListOf(vararg vectors: IGenericVector, dimensions: Int = vectors.first().dimensions): VectorArrayList =
    VectorArrayList(dimensions, vectors.size).also { array -> vectors.fastForEach { array.add(it) } }

fun vectorArrayListOf(vararg vectors: GenericVector, dimensions: Int = vectors.first().dimensions): VectorArrayList =
    VectorArrayList(dimensions, vectors.size).also { array -> vectors.fastForEach { array.add(it) } }

fun vectorArrayListOf(vararg data: Double, dimensions: Int): VectorArrayList {
    if (data.size % dimensions != 0) error("${data.size} is not multiple of $dimensions")
    val out = VectorArrayList(dimensions, data.size / dimensions)
    out.data.add(data)
    return out
}
fun vectorArrayListOf(vararg data: Float, dimensions: Int): VectorArrayList =
    vectorArrayListOf(*data.mapDouble { it.toDouble() }, dimensions = dimensions)
fun vectorArrayListOf(vararg data: Int, dimensions: Int): VectorArrayList =
    vectorArrayListOf(*data.mapDouble { it.toDouble() }, dimensions = dimensions)

interface IGenericVector {
    val dimensions: Int
    operator fun get(dim: Int): Double
    operator fun set(dim: Int, value: Double)
}

val IGenericVector.length: Double get() {
    var ssum = 0.0
    for (n in 0 until dimensions) ssum += this[n]
    return sqrt(ssum)
}

fun IGenericVector.toStringBuilder(out: StringBuilder) {
    out.appendGenericArray(dimensions) { appendNice(this@toStringBuilder[it]) }
}

@PublishedApi internal fun StringBuilder.appendGenericArray(size: Int, appendElement: StringBuilder.(Int) -> Unit) {
    append("[")
    for (n in 0 until size) {
        if (n != 0) append(", ")
        appendElement(n)
    }
    append("]")
}

// @TODO: Potential candidate for value class when multiple values are supported
class GenericVector(override val dimensions: Int, val data: DoubleArray, val offset: Int = 0) : IGenericVector {
    constructor(vararg data: Double) : this(data.size, data)
    constructor(vararg data: Float) : this(data.size, data.mapDouble { it.toDouble() })
    constructor(vararg data: Int) : this(data.size, data.mapDouble { it.toDouble() })

    override operator fun get(dim: Int): Double = data[offset + dim]
    override operator fun set(dim: Int, value: Double) { data[offset + dim] = value }

    override fun toString(): String = buildString { toStringBuilder(this) }
}
