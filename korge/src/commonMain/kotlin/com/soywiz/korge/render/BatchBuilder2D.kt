@file:OptIn(KorgeInternal::class)

package com.soywiz.korge.render

import com.soywiz.kds.fastArrayListOf
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klogger.Logger
import com.soywiz.kmem.*
import com.soywiz.korag.AG
import com.soywiz.korag.DefaultShaders
import com.soywiz.korag.FragmentShaderDefault
import com.soywiz.korag.VertexShaderDefault
import com.soywiz.korag.shader.Attribute
import com.soywiz.korag.shader.FragmentShader
import com.soywiz.korag.shader.Operand
import com.soywiz.korag.shader.Precision
import com.soywiz.korag.shader.Program
import com.soywiz.korag.shader.Uniform
import com.soywiz.korag.shader.VarType
import com.soywiz.korag.shader.Varying
import com.soywiz.korag.shader.VertexLayout
import com.soywiz.korge.internal.KorgeInternal
import com.soywiz.korge.view.BlendMode
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.color.ColorAdd
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korio.async.Signal
import com.soywiz.korio.util.OS
import com.soywiz.korma.geom.Matrix
import com.soywiz.korma.geom.Matrix3D
import com.soywiz.korma.geom.Point
import kotlin.jvm.JvmOverloads
import kotlin.math.min
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.concurrent.ThreadLocal

@SharedImmutable
private val logger = Logger("BatchBuilder2D")

/**
 * Allows to draw quads and sprites buffering the geometry to limit the draw batches executed calling [AG] (Accelerated Graphics).
 * This class handles a vertex structure of: x, y, u, v, colorMul, colorAdd. Allowing to draw texturized and tinted primitives.
 *
 * You should call: [drawQuad], [drawQuadFast], [drawNinePatch], [drawVertices] for buffering the geometries.
 *
 * For performance the actual drawing/rendering doesn't happen until the [flush] method is called (normally that happens automatically).
 * Then the engine will call [flush] when required, automatically once the buffer is filled, at the end of the frame, or when [RenderContext.flush] is executed
 * by other renderers.
 */
// @TODO: We could dynamically select a fragment shader based on the number of textures to reduce the numbers of IFs per pixel
class BatchBuilder2D constructor(
    @property:KorgeInternal
    val ctx: RenderContext,
    /** Maximum number of quads that could be drawn in a single batch.
     * Bigger numbers will increase memory usage, but might reduce the number of batches per frame when using the same texture and properties.
     */
    val reqMaxQuads: Int = DEFAULT_BATCH_QUADS
) {
    val maxTextures = BB_MAX_TEXTURES

    @KorgeInternal
    val viewMat: Matrix3D get() = ctx.viewMat
    @KorgeInternal
    val viewMat2D: Matrix get() = ctx.viewMat2D
    @KorgeInternal
    val uniforms: AG.UniformValues get() = ctx.uniforms

    inline fun use(block: (BatchBuilder2D) -> Unit) = ctx.useBatcher(this, block)

    val maxQuads: Int = min(reqMaxQuads, MAX_BATCH_QUADS)

    val texManager = ctx.agBitmapTextureManager
    //constructor(ag: AG, maxQuads: Int = DEFAULT_BATCH_QUADS) : this(RenderContext(ag), maxQuads)
    val ag: AG = ctx.ag
	init {
        logger.trace { "BatchBuilder2D[0]" }
        ctx.flushers.add { flush() }
    }

	var flipRenderTexture = true
	//var flipRenderTexture = false
	val maxQuadsMargin = maxQuads + 9

    /** Maximum number of vertices that can be buffered here in a single batch. It depens on the [maxQuads] parameter */
	val maxVertices = maxQuads * 4
    /** Maximum number of indices that can be buffered here in a single batch. It depens on the [maxQuads] parameter */
	val maxIndices = maxQuads * 6

	init { logger.trace { "BatchBuilder2D[1]" } }

	@PublishedApi internal val vertices = FBuffer.alloc(6 * 4 * maxVertices)
    @PublishedApi internal val verticesTexIndex = ByteArray(maxVertices)
    @PublishedApi internal val verticesPremultiplied = ByteArray(maxVertices)
    @PublishedApi internal val verticesWrap = ByteArray(maxVertices)
    @PublishedApi internal val indices = FBuffer.alloc(2 * maxIndices)
    //@PublishedApi internal val indices = ShortArray(maxIndices)
    //internal val vertices = FBuffer.allocNoDirect(6 * 4 * maxVertices)
    //internal val indices = FBuffer.allocNoDirect(2 * maxIndices)
    val indicesI16 = indices.i16
    //val indicesI16 = indices
    private val verticesI32 = vertices.i32
    private val verticesF32 = vertices.f32
    private val verticesData = vertices.data
    internal val verticesFast32 = vertices.fast32

	init { logger.trace { "BatchBuilder2D[2]" } }

    @PublishedApi internal var _vertexCount = 0

    var vertexCount: Int
        get() = _vertexCount
        //set(value) { _vertexCount = value }
        internal set(value) { _vertexCount = value }

    @PublishedApi internal var vertexPos = 0
    @PublishedApi internal var indexPos = 0
    @PublishedApi internal var currentTexIndex = 0

    @PublishedApi internal var currentTexN: Array<AG.Texture?> = Array(maxTextures) { null }

	//@PublishedApi internal var currentTex0: AG.Texture? = null
    //@PublishedApi internal var currentTex1: AG.Texture? = null

    @PublishedApi internal var currentSmoothing: Boolean = false

    @PublishedApi internal var currentBlendMode: BlendMode = BlendMode.NORMAL
    @PublishedApi internal var currentProgram: Program? = null

	init { logger.trace { "BatchBuilder2D[3]" } }

	private val vertexBuffer = ag.createVertexBuffer()
    private val texIndexVertexBuffer = ag.createVertexBuffer()
    private val texWrapVertexBuffer = ag.createVertexBuffer()
	private val indexBuffer = ag.createIndexBuffer()

	init { logger.trace { "BatchBuilder2D[4]" } }

    /** The current stencil state. If you change it, you must call the [flush] method to ensure everything has been drawn. */
	var stencil = AG.StencilState()

	init { logger.trace { "BatchBuilder2D[5]" } }

    /** The current color mask state. If you change it, you must call the [flush] method to ensure everything has been drawn. */
	var colorMask = AG.ColorMaskState()

	init { logger.trace { "BatchBuilder2D[6]" } }

    /** The current scissor state. If you change it, you must call the [flush] method to ensure everything has been drawn. */
	var scissor: AG.Scissor? = null

	private val identity = Matrix()

	init { logger.trace { "BatchBuilder2D[7]" } }

	private val ptt1 = Point()
	private val ptt2 = Point()

	private val pt1 = Point()
	private val pt2 = Point()
	private val pt3 = Point()
	private val pt4 = Point()
	private val pt5 = Point()

	private val pt6 = Point()
	private val pt7 = Point()
	private val pt8 = Point()

	init { logger.trace { "BatchBuilder2D[8]" } }

	init { logger.trace { "BatchBuilder2D[9]" } }

    val textureUnitN = Array(maxTextures) { AG.TextureUnit(null, linear = false) }

    //@KorgeInternal val textureUnit0 = AG.TextureUnit(null, linear = false)
    //@KorgeInternal val textureUnit1 = AG.TextureUnit(null, linear = false)

    init { logger.trace { "BatchBuilder2D[10]" } }

	// @TODO: kotlin-native crash: [1]    80122 segmentation fault  ./sample1-native.kexe
	//private val uniforms = mapOf<Uniform, Any>(
	//	DefaultShaders.u_ProjMat to projMat,
	//	DefaultShaders.u_Tex to textureUnit
	//)

    init {
        ctx.uniforms.put(AG.UniformValues(
            *Array(maxTextures) { BatchBuilder2D.u_TexN[it] to textureUnitN[it] },
        ))
    }

	init { logger.trace { "BatchBuilder2D[11]" } }

    fun readVertices(): List<VertexInfo> = (0 until vertexCount).map { readVertex(it) }

    fun readVertex(n: Int, out: VertexInfo = VertexInfo()): VertexInfo {
        out.read(this.vertices, n)
        val source = textureUnitN[0].texture?.source
        out.texWidth = source?.width ?: -1
        out.texHeight = source?.height ?: -1
        return out
    }

	// @TODO: copy data from TexturedVertexArray
	fun addVertex(x: Float, y: Float, u: Float, v: Float, colorMul: RGBA, colorAdd: ColorAdd, texIndex: Int = currentTexIndex, premultiplied: Boolean, wrap: Boolean) {
        vertexPos += _addVertex(verticesFast32, vertexPos, x, y, u, v, colorMul.value, colorAdd.value, texIndex, premultiplied, wrap)
        vertexCount++
	}

    fun _addVertex(vd: Fast32Buffer, vp: Int, x: Float, y: Float, u: Float, v: Float, colorMul: Int, colorAdd: Int, texIndex: Int = currentTexIndex, premultiplied: Boolean, wrap: Boolean): Int {
        vd.setF(vp + 0, x)
        vd.setF(vp + 1, y)
        vd.setF(vp + 2, u)
        vd.setF(vp + 3, v)
        vd.setI(vp + 4, colorMul)
        vd.setI(vp + 5, colorAdd)
        verticesTexIndex[vp / 6] = texIndex.toByte()
        verticesPremultiplied[vp / 6] = premultiplied.toByte()
        verticesWrap[vp / 6] = wrap.toByte()
        //println("texIndex.toByte()=${texIndex.toByte()}")
        return TEXTURED_ARRAY_COMPONENTS_PER_VERTEX
    }

	inline fun addIndex(idx: Int) {
		indicesI16[indexPos++] = idx.toShort()
	}

    inline fun addIndexRelative(idx: Int) {
        indicesI16[indexPos++] = (vertexCount + idx).toShort()
    }

	private fun _addIndices(indicesI16: Int16Buffer, pos: Int, i0: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int): Int {
        indicesI16[pos + 0] = i0.toShort()
        indicesI16[pos + 1] = i1.toShort()
        indicesI16[pos + 2] = i2.toShort()
        indicesI16[pos + 3] = i3.toShort()
        indicesI16[pos + 4] = i4.toShort()
        indicesI16[pos + 5] = i5.toShort()
        return 6
	}

    /**
     * Draws/buffers a textured ([tex]) and colored ([colorMul] and [colorAdd]) quad with this shape:
     *
     * 0..1
     * |  |
     * 3..2
     *
     * Vertices:
     * 0: [x0], [y0] (top left)
     * 1: [x1], [y1] (top right)
     * 2: [x2], [y2] (bottom right)
     * 3: [x3], [y3] (bottom left)
     */
	fun drawQuadFast(
		x0: Float, y0: Float,
		x1: Float, y1: Float,
		x2: Float, y2: Float,
		x3: Float, y3: Float,
		tex: TextureCoords,
		colorMul: RGBA, colorAdd: ColorAdd,
        texIndex: Int = currentTexIndex,
        premultiplied: Boolean = tex.premultiplied,
        wrap: Boolean = false
	) {
        //println("drawQuadFast[${ag.currentRenderBuffer}, renderingToTexture=${ag.renderingToTexture}]: ($x0,$y0)-($x2,$y2) tex=$tex")
        //println("viewMat=$viewMat, projMat=$projMat")

        ensure(6, 4)
        addQuadIndices()
        var vp = vertexPos
        val vd = verticesFast32
        vp += _addVertex(vd, vp, x0, y0, tex.tl_x, tex.tl_y, colorMul.value, colorAdd.value, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x1, y1, tex.tr_x, tex.tr_y, colorMul.value, colorAdd.value, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x2, y2, tex.br_x, tex.br_y, colorMul.value, colorAdd.value, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x3, y3, tex.bl_x, tex.bl_y, colorMul.value, colorAdd.value, texIndex, premultiplied, wrap)
        vertexPos = vp
        vertexCount += 4
	}

    fun drawQuadFast(
        x0: Float, y0: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        x3: Float, y3: Float,
        tx0: Float, ty0: Float,
        tx1: Float, ty1: Float,
        colorMul: RGBA,
        colorAdd: ColorAdd,
        texIndex: Int = currentTexIndex,
        premultiplied: Boolean,
        wrap: Boolean,
    ) {
        ensure(6, 4)
        addQuadIndices()
        addQuadVerticesFastNormal(x0, y0, x1, y1, x2, y2, x3, y3, tx0, ty0, tx1, ty1, colorMul.value, colorAdd.value, texIndex, premultiplied, wrap)
    }

    @JvmOverloads
    fun addQuadIndices(vc: Int = vertexCount) {
        indexPos += _addIndices(indicesI16, indexPos, vc + 0, vc + 1, vc + 2, vc + 3, vc + 0, vc + 2)
    }

    fun addQuadIndicesBatch(batchSize: Int) {
        var vc = vertexCount
        var ip = indexPos
        val i16 = indicesI16
        for (n in 0 until batchSize) {
            ip += _addIndices(i16, ip, vc + 0, vc + 1, vc + 2, vc + 3, vc + 0, vc + 2)
            vc += 4
        }
        indexPos = ip
        vertexCount = vc
    }

    fun addQuadVerticesFastNormal(
        x0: Float, y0: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        x3: Float, y3: Float,
        tx0: Float, ty0: Float,
        tx1: Float, ty1: Float,
        colorMul: Int,
        colorAdd: Int,
        texIndex: Int = currentTexIndex,
        premultiplied: Boolean,
        wrap: Boolean
    ) {
        vertexPos = _addQuadVerticesFastNormal(vertexPos, verticesFast32, x0, y0, x1, y1, x2, y2, x3, y3, tx0, ty0, tx1, ty1, colorMul, colorAdd, texIndex, premultiplied, wrap)
        vertexCount += 4
    }

    fun _addQuadVerticesFastNormal(
        vp: Int,
        vd: Fast32Buffer,
        x0: Float, y0: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float,
        x3: Float, y3: Float,
        tx0: Float, ty0: Float,
        tx1: Float, ty1: Float,
        colorMul: Int,
        colorAdd: Int,
        texIndex: Int = currentTexIndex,
        premultiplied: Boolean,
        wrap: Boolean,
    ): Int {
        var vp = vp
        vp += _addVertex(vd, vp, x0, y0, tx0, ty0, colorMul, colorAdd, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x1, y1, tx1, ty0, colorMul, colorAdd, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x2, y2, tx1, ty1, colorMul, colorAdd, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x3, y3, tx0, ty1, colorMul, colorAdd, texIndex, premultiplied, wrap)
        return vp
    }

    fun _addQuadVerticesFastNormalNonRotated(
        vp: Int,
        vd: Fast32Buffer,
        x0: Float, y0: Float,
        x1: Float, y1: Float,
        tx0: Float, ty0: Float,
        tx1: Float, ty1: Float,
        colorMul: Int,
        colorAdd: Int,
        texIndex: Int = currentTexIndex,
        premultiplied: Boolean, wrap: Boolean,
    ): Int {
        var vp = vp
        vp += _addVertex(vd, vp, x0, y0, tx0, ty0, colorMul, colorAdd, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x1, y0, tx1, ty0, colorMul, colorAdd, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x1, y1, tx1, ty1, colorMul, colorAdd, texIndex, premultiplied, wrap)
        vp += _addVertex(vd, vp, x0, y1, tx0, ty1, colorMul, colorAdd, texIndex, premultiplied, wrap)
        return vp
    }

    /**
     * Draws/buffers a set of textured and colorized array of vertices [array] with the current state previously set by calling [setStateFast].
     */
    fun drawVertices(
        array: TexturedVertexArray,
        matrix: Matrix?,
        vcount: Int = array.vcount,
        icount: Int = array.icount,
        texIndex: Int = currentTexIndex,
        premultiplied: Boolean, wrap: Boolean,
    ) {
        ensure(icount, vcount)
        //println("doFlush=$doFlush : icount=$icount, vcount=$vcount")

        val i16 = indicesI16
        val ip = indexPos
        val vc = vertexCount
        val arrayIndices = array.indices
        val icount = min(icount, array.icount)

        arraycopy(arrayIndices, 0, i16, ip, icount)
        arrayadd(i16, vc.toShort(), ip, ip + icount)
        //println("added: $vc"); for (n in 0 until icount) print(",${i16[n]}") ; println()
        //for (n in 0 until icount) i16[ip + n] = (vc + arrayIndices[n]).toShort()

        val vp = vertexPos
        val src = array._data.i32
        val dst = vertices.i32
        arraycopy(src, 0, dst, vp, vcount * 6)
        //for (n in 0 until vcount * 6) dst[vp + n] = src[n]

        //println("texIndex=$texIndex")
        val vp6 = vertexPos / 6
        //println("texIndex=$texIndex")
        arrayfill(verticesTexIndex, texIndex.toByte(), vp6, vp6 + vcount)
        arrayfill(verticesPremultiplied, premultiplied.toByte(), vp6, vp6 + vcount)
        arrayfill(verticesWrap, wrap.toByte(), vp6, vp6 + vcount)

        if (matrix != null) {
            applyMatrix(matrix, vertexPos, vcount)
        }

        _vertexCount += vcount
        vertexPos += vcount * 6
        indexPos += icount
    }

    private fun applyMatrix(matrix: Matrix, idx: Int, vcount: Int) {
        val f32 = vertices.f32
        var idx = idx

        val ma = matrix.af
        val mb = matrix.bf
        val mc = matrix.cf
        val md = matrix.df
        val mtx = matrix.txf
        val mty = matrix.tyf

        for (n in 0 until vcount) {
            val x = f32[idx + 0]
            val y = f32[idx + 1]
            f32[idx + 0] = Matrix.transformXf(ma, mb, mc, md, mtx, mty, x, y)
            f32[idx + 1] = Matrix.transformYf(ma, mb, mc, md, mtx, mty, x, y)
            idx += VERTEX_INDEX_SIZE
        }
    }

    /**
     * Draws/buffers a set of textured and colorized array of vertices [array] with the specified texture [tex] and optionally [smoothing] it and an optional [program].
     */
    inline fun drawVertices(
        array: TexturedVertexArray, tex: TextureBase, smoothing: Boolean, blendMode: BlendMode,
        vcount: Int = array.vcount, icount: Int = array.icount, program: Program? = null, matrix: Matrix? = null,
        premultiplied: Boolean = tex.premultiplied, wrap: Boolean = false
    ) {
        setStateFast(tex.base, smoothing, blendMode, program, icount, vcount)
        drawVertices(array, matrix, vcount, icount, premultiplied = premultiplied, wrap = wrap)
	}

	private fun checkAvailable(indices: Int, vertices: Int): Boolean {
		return (this.indexPos + indices < maxIndices) || (this.vertexPos + vertices < maxVertices)
	}

	fun ensure(indices: Int, vertices: Int): Boolean {
        if (indices == 0 && vertices == 0) return false
        val doFlush = !checkAvailable(indices, vertices)
		if (doFlush) flush()
		if (!checkAvailable(indices, vertices)) error("Too much vertices: indices=$indices, vertices=$vertices")
        return doFlush
	}

    /**
     * Sets the current texture [tex], [smoothing], [blendMode] and [program] that will be used by the following drawing calls not specifying these attributes.
     */
	fun setStateFast(
        tex: TextureBase, smoothing: Boolean, blendMode: BlendMode, program: Program?, icount: Int, vcount: Int,
    ) {
        setStateFast(tex.base, smoothing, blendMode, program, icount, vcount)
    }

    /**
     * Sets the current texture [tex], [smoothing], [blendMode] and [program] that will be used by the following drawing calls not specifying these attributes.
     */
    inline fun setStateFast(
        tex: AG.Texture?, smoothing: Boolean, blendMode: BlendMode, program: Program?, icount: Int, vcount: Int,
    ) {
        ensure(icount, vcount)

        val isCurrentStateFast = isCurrentStateFast(tex, smoothing, blendMode, program)
        //println("isCurrentStateFast=$isCurrentStateFast, tex=$tex, currentTex=$currentTex, currentTex2=$currentTex2")
        if (isCurrentStateFast) return
        flush()
        currentTexIndex = 0
        currentTexN[0] = tex
        currentSmoothing = smoothing
        currentBlendMode = blendMode
        currentProgram = program
    }

    fun hasTex(tex: AG.Texture?): Boolean {
        currentTexN.fastForEach { if (it === tex) return true }
        return false
    }

    //@PublishedApi internal fun resetCachedState() {
    //    for (n in currentTexN.indices) {
    //        currentTexN[n] = null
    //    }
    //    currentTexIndex = 0
    //    currentSmoothing = false
    //    currentBlendMode = BlendMode.NORMAL
    //    currentProgram = null
    //}

    @PublishedApi internal fun isCurrentStateFast(
        tex: AG.Texture?, smoothing: Boolean, blendMode: BlendMode, program: Program?,
    ): Boolean {
        var hasTex = hasTex(tex)
        if (currentTexN[0] !== null && !hasTex) {
            for (n in 1 until currentTexN.size) {
                if (currentTexN[n] === null) {
                    currentTexN[n] = tex
                    hasTex = true
                    break
                }
            }
        }

        for (n in currentTexN.indices) {
            if (tex === currentTexN[n]) {
                currentTexIndex = n
                break
            }
        }

        return hasTex
            && (currentSmoothing == smoothing)
            && (currentBlendMode === blendMode)
            && (currentProgram === program)
    }

    fun setStateFast(tex: Bitmap, smoothing: Boolean, blendMode: BlendMode, program: Program?, icount: Int, vcount: Int) {
        setStateFast(texManager.getTextureBase(tex), smoothing, blendMode, program, icount, vcount)
    }

    fun setStateFast(tex: BmpSlice, smoothing: Boolean, blendMode: BlendMode, program: Program?, icount: Int, vcount: Int) {
        setStateFast(texManager.getTexture(tex).base, smoothing, blendMode, program, icount, vcount)
    }

    /**
     * Draws/buffers a 9-patch image with the texture [tex] at [x], [y] with the total size of [width] and [height].
     * [posCuts] and [texCuts] are [Point] an array of 4 points describing ratios (values between 0 and 1) inside the width/height of the area to be drawn,
     * and the positions inside the texture.
     *
     * The 9-patch looks like this (dividing the image in 9 parts).
     *
     * 0--+-----+--+
     * |  |     |  |
     * |--1-----|--|
     * |  |SSSSS|  |
     * |  |SSSSS|  |
     * |  |SSSSS|  |
     * |--|-----2--|
     * |  |     |  |
     * +--+-----+--3
     *
     * 0: Top-left of the 9-patch
     * 1: Top-left part where scales starts
     * 2: Bottom-right part where scales ends
     * 3: Bottom-right of the 9-patch
     *
     * S: Is the part that is scaled. The other regions are not scaled.
     *
     * It uses the transform [m] matrix, with an optional [filtering] and [colorMul]/[colorAdd], [blendMode] and [program]
     */
	fun drawNinePatch(
        tex: TextureCoords,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        posCuts: Array<Point>,
        texCuts: Array<Point>,
        m: Matrix = identity,
        filtering: Boolean = true,
        colorMul: RGBA = Colors.WHITE,
        colorAdd: ColorAdd = ColorAdd.NEUTRAL,
        blendMode: BlendMode = BlendMode.NORMAL,
        program: Program? = null,
        premultiplied: Boolean = tex.premultiplied,
        wrap: Boolean = false
	) {
		setStateFast(tex.base, filtering, blendMode, program, icount = 6 * 9, vcount = 4 * 4)
        val texIndex: Int = currentTexIndex

		val p_o = pt1.setToTransform(m, ptt1.setTo(x, y))
		val p_dU = pt2.setToSub(ptt1.setToTransform(m, ptt1.setTo(x + width, y)), p_o)
		val p_dV = pt3.setToSub(ptt1.setToTransform(m, ptt1.setTo(x, y + height)), p_o)

		val t_o = pt4.setTo(tex.tl_x, tex.tl_y)
		val t_dU = pt5.setToSub(ptt1.setTo(tex.tr_x, tex.tr_y), t_o)
		val t_dV = pt6.setToSub(ptt1.setTo(tex.bl_x, tex.bl_y), t_o)

		val start = vertexCount

		for (cy in 0 until 4) {
			val posCutY = posCuts[cy].y
			val texCutY = texCuts[cy].y
			for (cx in 0 until 4) {
				val posCutX = posCuts[cx].x
				val texCutX = texCuts[cx].x

				val p = pt7.setToAdd(
					p_o,
					ptt1.setToAdd(
						ptt1.setToMul(p_dU, posCutX),
						ptt2.setToMul(p_dV, posCutY)
					)
				)

				val t = pt8.setToAdd(
					t_o,
					ptt1.setToAdd(
						ptt1.setToMul(t_dU, texCutX),
						ptt2.setToMul(t_dV, texCutY)
					)
				)

				addVertex(p.x.toFloat(), p.y.toFloat(), t.x.toFloat(), t.y.toFloat(), colorMul, colorAdd, texIndex, premultiplied, wrap)
			}
		}

		for (cy in 0 until 3) {
			for (cx in 0 until 3) {
				// v0...v1
				// .    .
				// v2...v3

				val v0 = start + cy * 4 + cx
				val v1 = v0 + 1
				val v2 = v0 + 4
				val v3 = v0 + 5

				addIndex(v0)
				addIndex(v1)
				addIndex(v2)
				addIndex(v2)
				addIndex(v1)
				addIndex(v3)
			}
		}
	}

    fun drawQuad(
        tex: TextureCoords,
        x: Float = 0f,
        y: Float = 0f,
        width: Float = tex.width.toFloat(),
        height: Float = tex.height.toFloat(),
        m: Matrix = identity,
        filtering: Boolean = true,
        colorMul: RGBA = Colors.WHITE,
        colorAdd: ColorAdd = ColorAdd.NEUTRAL,
        blendMode: BlendMode = BlendMode.NORMAL,
        program: Program? = null,
        premultiplied: Boolean = tex.premultiplied,
        wrap: Boolean = false
    ): Unit = drawQuad(tex, x, y, width, height, m, filtering, colorMul, colorAdd, blendMode, program, premultiplied, wrap ,Unit)

    /**
     * Draws a textured [tex] quad at [x], [y] and size [width]x[height].
     *
     * It uses [m] transform matrix, an optional [filtering] and [colorMul], [colorAdd], [blendMode] and [program] as state for drawing it.
     *
     * Note: To draw solid quads, you can use [Bitmaps.white] + [AgBitmapTextureManager] as texture and the [colorMul] as quad color.
     */
	fun drawQuad(
        tex: TextureCoords,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        m: Matrix = identity,
        filtering: Boolean = true,
        colorMul: RGBA = Colors.WHITE,
        colorAdd: ColorAdd = ColorAdd.NEUTRAL,
        blendMode: BlendMode = BlendMode.NORMAL,
        program: Program? = null,
        premultiplied: Boolean = tex.premultiplied,
        wrap: Boolean = false,
        unit: Unit = Unit,
	) {
        val x0 = x
        val x1 = (x + width)
        val y0 = y
        val y1 = (y + height)
        setStateFast(tex.base, filtering, blendMode, program, icount = 6, vcount = 4)
        drawQuadFast(
            m.transformXf(x0, y0), m.transformYf(x0, y0),
            m.transformXf(x1, y0), m.transformYf(x1, y0),
            m.transformXf(x1, y1), m.transformYf(x1, y1),
            m.transformXf(x0, y1), m.transformYf(x0, y1),
        	tex, colorMul, colorAdd,
            premultiplied = premultiplied, wrap = wrap
        )
	}

    enum class AddType(val index: Int) {
        NO_ADD(0),
        POST_ADD(1),
        PRE_ADD(2);
    }

    companion object {
        val MAX_BATCH_QUADS = 16383
        val DEFAULT_BATCH_QUADS = 4096
        //val DEFAULT_BATCH_QUADS = MAX_BATCH_QUADS

        init { logger.trace { "BatchBuilder2D.Companion[0]" } }

        @KorgeInternal
		val a_ColMul: Attribute get() = DefaultShaders.a_Col
        @KorgeInternal
		val a_ColAdd: Attribute = Attribute("a_Col2", VarType.Byte4, normalized = true)

		init { logger.trace { "BatchBuilder2D.Companion[1]" } }

        @KorgeInternal
		val v_ColMul: Varying get() = DefaultShaders.v_Col
        @KorgeInternal
		val v_ColAdd: Varying = Varying("v_Col2", VarType.Float4)

        val a_TexIndex: Attribute = Attribute("a_TexIndex", VarType.UByte1, normalized = false, precision = Precision.LOW)
        val a_Wrap: Attribute = Attribute("a_Wrap", VarType.UByte1, normalized = false, precision = Precision.LOW)

        val v_TexIndex: Varying = Varying("v_TexIndex", VarType.Float1, precision = Precision.LOW)
        val v_Wrap: Varying = Varying("v_Wrap", VarType.Float1, precision = Precision.LOW)
        //val u_Tex0 = Uniform("u_Tex0", VarType.TextureUnit)

        //val u_DoWrap: Uniform = Uniform("u_DoWrap", VarType.Bool1)
        //val u_InputPre: Uniform = Uniform("u_InputPre", VarType.Bool1)
        val u_OutputPre: Uniform = Uniform("u_OutputPre", VarType.Bool1)
        val u_TexN: Array<Uniform> = Array(BB_MAX_TEXTURES) { Uniform("u_Tex$it", VarType.Sampler2D) }

        fun DO_OUTPUT_FROM(builder: Program.Builder, out: Operand, u_OutputPre: Operand = BatchBuilder2D.u_OutputPre) {
            builder.apply {
                // We come from premultiplied, but output wants straight
                IF(u_OutputPre.not()) {
                    SET(out["rgb"], out["rgb"] / out["a"])
                }
            }
        }

        fun DO_INPUT_OUTPUT(builder: Program.Builder, out: Operand) {
            builder.apply {
                IF(u_OutputPre.not()) {
                    SET(out["rgb"], out["rgb"] / out["a"])
                }
            }
        }

        //val u_Tex0 = DefaultShaders.u_Tex
        //val u_Tex1 = Uniform("u_Tex1", VarType.TextureUnit)

		init { logger.trace { "BatchBuilder2D.Companion[2]" } }

        @KorgeInternal
		val LAYOUT = VertexLayout(DefaultShaders.a_Pos, DefaultShaders.a_Tex, a_ColMul, a_ColAdd)
        @KorgeInternal
        val LAYOUT_TEX_INDEX = VertexLayout(a_TexIndex)
        @KorgeInternal
        val LAYOUT_WRAP = VertexLayout(a_Wrap)
        @KorgeInternal
		val VERTEX = VertexShaderDefault {
            SET(v_Tex, a_Tex)
            SET(v_TexIndex, a_TexIndex)
            SET(v_Wrap, a_Wrap)
            SET(v_ColMul, vec4(a_ColMul["rgb"] * a_ColMul["a"], a_ColMul["a"])) // premultiplied colorMul
            SET(v_ColAdd, a_ColAdd)
            SET(out, (u_ProjMat * u_ViewMat) * vec4(a_Pos, 0f.lit, 1f.lit))
		}

		init { logger.trace { "BatchBuilder2D.Companion[3]" } }

        private fun getShaderProgramIndex(add: AddType): Int = 0
            .insert(add.index, 0, 2)

        private fun getOrCreateStandardProgram(preaddType: AddType): Program {
            val index = getShaderProgramIndex(preaddType)
            if (BATCH_BUILDER2D_PROGRAMS[index] == null) BATCH_BUILDER2D_PROGRAMS[index] = _createProgramUncached(preaddType)
            return BATCH_BUILDER2D_PROGRAMS[index]!!
        }

        private fun _createProgramUncached(addType: AddType): Program {
            val fragment = buildTextureLookupFragment(add = addType)
            val addString = when (addType) {
                AddType.NO_ADD -> ".NoAdd"
                AddType.PRE_ADD -> ".PreAdd"
                AddType.POST_ADD -> ".PostAdd"
            }
            return Program(
                vertex = VERTEX,
                fragment = fragment,
                name = "BatchBuilder2D.Tinted${addString}"
            )
        }

        @KorgeInternal
		val PROGRAM: Program = getOrCreateStandardProgram(AddType.NO_ADD)

        init { logger.trace { "BatchBuilder2D.Companion[4]" } }

        @KorgeInternal
        fun getTextureLookupProgram(add: AddType = AddType.POST_ADD): Program = getOrCreateStandardProgram(add)

		//val PROGRAM_NORMAL = Program(
		//	vertex = VERTEX,
		//	fragment = FragmentShader {
		//		SET(out, texture2D(DefaultShaders.u_Tex, DefaultShaders.v_Tex["xy"])["rgba"] * v_Col2["rgba"])
		//		SET(out, out + v_Col2)
		//		// Required for shape masks:
		//		IF(out["a"] le 0f.lit) { DISCARD() }
		//	},
		//	name = "BatchBuilder2D.Tinted"
		//)

        /**
         * Builds a [FragmentShader] for textured and colored drawing that works matching if the texture is [premultiplied]
         *
         * Shader is expected to return a premultiplied alpha color.
         */
        @KorgeInternal
		internal fun buildTextureLookupFragment(add: AddType) = FragmentShaderDefault {
            IF (v_Wrap ne 0f.lit) {
                SET(t_Temp0["xy"], fract(v_Tex["xy"]))
            } ELSE {
                SET(t_Temp0["xy"], v_Tex["xy"])
            }

            IF_ELSE_BINARY_LOOKUP(v_TexIndex, 0, BB_MAX_TEXTURES - 1) { n ->
                SET(out, texture2D(u_TexN[n], t_Temp0["xy"]))
            }
            if (add == AddType.NO_ADD) {
                SET(out, out * v_ColMul)
            } else {
                SET(t_Temp0, (v_ColAdd - vec4(.5f.lit)) * 2f.lit)
                when (add) {
                    AddType.POST_ADD -> SET(out, (out * v_ColMul) + t_Temp0)
                    else -> SET(out, clamp(out + t_Temp0, 0f.lit, 1f.lit) * v_ColMul)
                }
            }
            IF(out["a"] le 0f.lit) { DISCARD() }
            DO_OUTPUT_FROM(this, out)
        }

		//init { println(PROGRAM_PRE.fragment.toGlSl()) }
	}

    val beforeFlush = Signal<BatchBuilder2D>()
    val onInstanceCount = Signal<Int>()

    fun uploadVertices() {
        vertexBuffer.upload(vertices, 0, vertexPos * 4)
        texIndexVertexBuffer.upload(verticesTexIndex, 0, vertexPos / 6)
        texWrapVertexBuffer.upload(verticesWrap, 0, vertexPos / 6)
    }

    fun uploadIndices() {
        indexBuffer.upload(indices, 0, indexPos * 2)
    }

    private val vertexData = fastArrayListOf(
        AG.VertexData(vertexBuffer, LAYOUT),
        AG.VertexData(texIndexVertexBuffer, LAYOUT_TEX_INDEX),
        AG.VertexData(texWrapVertexBuffer, LAYOUT_WRAP),
    )

    fun updateStandardUniforms() {
        //println("updateStandardUniforms: ag.currentSize(${ag.currentWidth}, ${ag.currentHeight}) : ${ag.currentRenderBuffer}")
        ctx.updateStandardUniforms()

        for (n in 0 until maxTextures) {
            val textureUnit = textureUnitN[n]
            textureUnit.set(currentTexN[n], currentSmoothing)
        }

        updateStandardUniformsPre()
    }

    fun updateStandardUniformsPre() {
        //uniforms[u_InputPre] = currentTexN[0]?.premultiplied == true
        uniforms[u_OutputPre] = ag.isRenderingToTexture
    }

    fun getIsPremultiplied(texture: AG.Texture?): Boolean = texture?.premultiplied == true
    fun getDefaultProgram(): Program = PROGRAM
    fun getDefaultProgramForTexture(): Program = getDefaultProgram()

    /** When there are vertices pending, this performs a [AG.draw] call flushing all the buffered geometry pending to draw */
	fun flush(uploadVertices: Boolean = true, uploadIndices: Boolean = true) {
        //println("vertexCount=${vertexCount}")
		if (vertexCount > 0) {
            updateStandardUniforms()

			//println("ORTHO: ${ag.backHeight.toFloat()}, ${ag.backWidth.toFloat()}")

			val factors = currentBlendMode

			if (uploadVertices) uploadVertices()
            if (uploadIndices) uploadIndices()

			//println("MyUniforms: $uniforms")

			//println("RENDER: $realFactors")
            //println("DRAW: $uniforms")

            val program = currentProgram ?: PROGRAM
            //println("program=$program, currentTexN[0]=${currentTexN[0]}")
            ag.drawV2(
                vertexData = vertexData,
                indices = indexBuffer,
                program = program,
                //program = PROGRAM_PRE,
                type = AG.DrawType.TRIANGLES,
                vertexCount = indexPos,
                blending = factors.factors(ag.isRenderingToTexture),
                uniforms = uniforms,
                stencil = stencil,
                colorMask = colorMask,
                scissor = scissor
            )
            beforeFlush(this)
		}

		vertexCount = 0
		vertexPos = 0
		indexPos = 0
        for (n in 0 until maxTextures) currentTexN[n] = null
        currentTexIndex = 0
        //resetCachedState()
	}

    fun simulateBatchStats(vertexCount: Int) {
        val oldVertexCount = this.vertexCount
        this.vertexCount = vertexCount
        try {
            beforeFlush(this)
        } finally {
            this.vertexCount = oldVertexCount
        }
    }

    /**
     * Executes [callback] while setting temporarily the view matrix to [matrix]
     */
	inline fun setViewMatrixTemp(matrix: Matrix, crossinline callback: () -> Unit) = ctx.setViewMatrixTemp(matrix, callback)

    /**
     * Executes [callback] while setting temporarily an [uniform] to a [value]
     */
	inline fun setTemporalUniform(uniform: Uniform, value: Any?, flush: Boolean = true, callback: (AG.UniformValues) -> Unit) = ctx.setTemporalUniform(uniform, value, flush, callback)

    inline fun <reified T> setTemporalUniforms(uniforms: Array<Uniform>, values: Array<T>, count: Int = values.size, olds: Array<T?> = arrayOfNulls<T>(count), flush: Boolean = true, callback: (AG.UniformValues) -> Unit) =
        ctx.setTemporalUniforms(uniforms, values, count, olds, flush, callback)

    /**
     * Executes [callback] while setting temporarily a set of [uniforms]
     */
	inline fun setTemporalUniforms(uniforms: AG.UniformValues?, callback: (AG.UniformValues) -> Unit) = ctx.setTemporalUniforms(uniforms, callback)
}

@ThreadLocal
private val BATCH_BUILDER2D_PROGRAMS: Array<Program?> = arrayOfNulls(64)

internal val BB_MAX_TEXTURES = when (OS.rawName) {
    "linuxArm32Hfp",
    "iosArm32" -> 1
    else -> 4
}
