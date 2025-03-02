// WARNING: File autogenerated DO NOT modify
// https://www.khronos.org/registry/OpenGL/api/GLES2/gl2.h
@file:Suppress("unused", "RedundantUnitReturnType", "PropertyName")

package com.soywiz.kgl

import com.soywiz.kmem.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import kotlinx.cinterop.*
import platform.OpenGL.*
import platform.posix.*

val GL_MODULE by lazy { dlopen("/System/Library/Frameworks/OpenGL.framework/Versions/Current/OpenGL", RTLD_LAZY); }

internal actual fun glGetProcAddressAnyOrNull(name: String): COpaquePointer? {
    return dlsym(GL_MODULE, "_$name") ?: dlsym(GL_MODULE, name)
}

actual class KmlGlNative actual constructor() : NativeBaseKmlGl() {
    override fun activeTexture(texture: Int): Unit = tempBufferAddress { glActiveTexture(texture.convert()) }
    override fun attachShader(program: Int, shader: Int): Unit = tempBufferAddress { glAttachShader(program.convert(), shader.convert()) }
    override fun bindAttribLocation(program: Int, index: Int, name: String): Unit = memScoped { tempBufferAddress { glBindAttribLocation(program.convert(), index.convert(), name) } }
    override fun bindBuffer(target: Int, buffer: Int): Unit = tempBufferAddress { glBindBuffer(target.convert(), buffer.convert()) }
    override fun bindFramebuffer(target: Int, framebuffer: Int): Unit = tempBufferAddress { glBindFramebuffer(target.convert(), framebuffer.convert()) }
    override fun bindRenderbuffer(target: Int, renderbuffer: Int): Unit = tempBufferAddress { glBindRenderbuffer(target.convert(), renderbuffer.convert()) }
    override fun bindTexture(target: Int, texture: Int): Unit = tempBufferAddress { glBindTexture(target.convert(), texture.convert()) }
    override fun blendColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = tempBufferAddress { glBlendColor(red, green, blue, alpha) }
    override fun blendEquation(mode: Int): Unit = tempBufferAddress { glBlendEquation(mode.convert()) }
    override fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int): Unit = tempBufferAddress { glBlendEquationSeparate(modeRGB.convert(), modeAlpha.convert()) }
    override fun blendFunc(sfactor: Int, dfactor: Int): Unit = tempBufferAddress { glBlendFunc(sfactor.convert(), dfactor.convert()) }
    override fun blendFuncSeparate(sfactorRGB: Int, dfactorRGB: Int, sfactorAlpha: Int, dfactorAlpha: Int): Unit = tempBufferAddress { glBlendFuncSeparate(sfactorRGB.convert(), dfactorRGB.convert(), sfactorAlpha.convert(), dfactorAlpha.convert()) }
    override fun bufferData(target: Int, size: Int, data: FBuffer, usage: Int): Unit = tempBufferAddress { glBufferData(target.convert(), size.convert(), data.unsafeAddress(), usage.convert()) }
    override fun bufferSubData(target: Int, offset: Int, size: Int, data: FBuffer): Unit = tempBufferAddress { glBufferSubData(target.convert(), offset.convert(), size.convert(), data.unsafeAddress()) }
    override fun checkFramebufferStatus(target: Int): Int = tempBufferAddress { glCheckFramebufferStatus(target.convert()).convert() }
    override fun clear(mask: Int): Unit = tempBufferAddress { glClear(mask.convert()) }
    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = tempBufferAddress { glClearColor(red, green, blue, alpha) }
    override fun clearDepthf(d: Float): Unit = tempBufferAddress { glClearDepth(d.convertFloat()) }
    override fun clearStencil(s: Int): Unit = tempBufferAddress { glClearStencil(s.convert()) }
    override fun colorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean): Unit = tempBufferAddress { glColorMask(red.toInt().convert(), green.toInt().convert(), blue.toInt().convert(), alpha.toInt().convert()) }
    override fun compileShader(shader: Int): Unit = tempBufferAddress { glCompileShader(shader.convert()) }
    override fun compressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, imageSize: Int, data: FBuffer): Unit = tempBufferAddress { glCompressedTexImage2D(target.convert(), level.convert(), internalformat.convert(), width.convert(), height.convert(), border.convert(), imageSize.convert(), data.unsafeAddress()) }
    override fun compressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, imageSize: Int, data: FBuffer): Unit = tempBufferAddress { glCompressedTexSubImage2D(target.convert(), level.convert(), xoffset.convert(), yoffset.convert(), width.convert(), height.convert(), format.convert(), imageSize.convert(), data.unsafeAddress()) }
    override fun copyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int): Unit = tempBufferAddress { glCopyTexImage2D(target.convert(), level.convert(), internalformat.convert(), x.convert(), y.convert(), width.convert(), height.convert(), border.convert()) }
    override fun copyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int): Unit = tempBufferAddress { glCopyTexSubImage2D(target.convert(), level.convert(), xoffset.convert(), yoffset.convert(), x.convert(), y.convert(), width.convert(), height.convert()) }
    override fun createProgram(): Int = tempBufferAddress { glCreateProgram().convert() }
    override fun createShader(type: Int): Int = tempBufferAddress { glCreateShader(type.convert()).convert() }
    override fun cullFace(mode: Int): Unit = tempBufferAddress { glCullFace(mode.convert()) }
    override fun deleteBuffers(n: Int, items: FBuffer): Unit = tempBufferAddress { glDeleteBuffers(n.convert(), items.unsafeAddress().reinterpret()) }
    override fun deleteFramebuffers(n: Int, items: FBuffer): Unit = tempBufferAddress { glDeleteFramebuffers(n.convert(), items.unsafeAddress().reinterpret()) }
    override fun deleteProgram(program: Int): Unit = tempBufferAddress { glDeleteProgram(program.convert()) }
    override fun deleteRenderbuffers(n: Int, items: FBuffer): Unit = tempBufferAddress { glDeleteRenderbuffers(n.convert(), items.unsafeAddress().reinterpret()) }
    override fun deleteShader(shader: Int): Unit = tempBufferAddress { glDeleteShader(shader.convert()) }
    override fun deleteTextures(n: Int, items: FBuffer): Unit = tempBufferAddress { glDeleteTextures(n.convert(), items.unsafeAddress().reinterpret()) }
    override fun depthFunc(func: Int): Unit = tempBufferAddress { glDepthFunc(func.convert()) }
    override fun depthMask(flag: Boolean): Unit = tempBufferAddress { glDepthMask(flag.toInt().convert()) }
    override fun depthRangef(n: Float, f: Float): Unit = tempBufferAddress { glDepthRange(n.convertFloat(), f.convertFloat()) }
    override fun detachShader(program: Int, shader: Int): Unit = tempBufferAddress { glDetachShader(program.convert(), shader.convert()) }
    override fun disable(cap: Int): Unit = tempBufferAddress { glDisable(cap.convert()) }
    override fun disableVertexAttribArray(index: Int): Unit = tempBufferAddress { glDisableVertexAttribArray(index.convert()) }
    override fun drawArrays(mode: Int, first: Int, count: Int): Unit = tempBufferAddress { glDrawArrays(mode.convert(), first.convert(), count.convert()) }
    override fun drawElements(mode: Int, count: Int, type: Int, indices: Int): Unit = tempBufferAddress { glDrawElements(mode.convert(), count.convert(), type.convert(), indices.toLong().toCPointer<IntVar>()) }
    override fun enable(cap: Int): Unit = tempBufferAddress { glEnable(cap.convert()) }
    override fun enableVertexAttribArray(index: Int): Unit = tempBufferAddress { glEnableVertexAttribArray(index.convert()) }
    override fun finish(): Unit = tempBufferAddress { glFinish() }
    override fun flush(): Unit = tempBufferAddress { glFlush() }
    override fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int): Unit = tempBufferAddress { glFramebufferRenderbuffer(target.convert(), attachment.convert(), renderbuffertarget.convert(), renderbuffer.convert()) }
    override fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int): Unit = tempBufferAddress { glFramebufferTexture2D(target.convert(), attachment.convert(), textarget.convert(), texture.convert(), level.convert()) }
    override fun frontFace(mode: Int): Unit = tempBufferAddress { glFrontFace(mode.convert()) }
    override fun genBuffers(n: Int, buffers: FBuffer): Unit = tempBufferAddress { glGenBuffers(n.convert(), buffers.unsafeAddress().reinterpret()) }
    override fun generateMipmap(target: Int): Unit = tempBufferAddress { glGenerateMipmap(target.convert()) }
    override fun genFramebuffers(n: Int, framebuffers: FBuffer): Unit = tempBufferAddress { glGenFramebuffers(n.convert(), framebuffers.unsafeAddress().reinterpret()) }
    override fun genRenderbuffers(n: Int, renderbuffers: FBuffer): Unit = tempBufferAddress { glGenRenderbuffers(n.convert(), renderbuffers.unsafeAddress().reinterpret()) }
    override fun genTextures(n: Int, textures: FBuffer): Unit = tempBufferAddress { glGenTextures(n.convert(), textures.unsafeAddress().reinterpret()) }
    override fun getActiveAttrib(program: Int, index: Int, bufSize: Int, length: FBuffer, size: FBuffer, type: FBuffer, name: FBuffer): Unit = tempBufferAddress { glGetActiveAttrib(program.convert(), index.convert(), bufSize.convert(), length.unsafeAddress().reinterpret(), size.unsafeAddress().reinterpret(), type.unsafeAddress().reinterpret(), name.unsafeAddress().reinterpret()) }
    override fun getActiveUniform(program: Int, index: Int, bufSize: Int, length: FBuffer, size: FBuffer, type: FBuffer, name: FBuffer): Unit = tempBufferAddress { glGetActiveUniform(program.convert(), index.convert(), bufSize.convert(), length.unsafeAddress().reinterpret(), size.unsafeAddress().reinterpret(), type.unsafeAddress().reinterpret(), name.unsafeAddress().reinterpret()) }
    override fun getAttachedShaders(program: Int, maxCount: Int, count: FBuffer, shaders: FBuffer): Unit = tempBufferAddress { glGetAttachedShaders(program.convert(), maxCount.convert(), count.unsafeAddress().reinterpret(), shaders.unsafeAddress().reinterpret()) }
    override fun getAttribLocation(program: Int, name: String): Int = memScoped { tempBufferAddress { glGetAttribLocation(program.convert(), name).convert() } }
    override fun getUniformLocation(program: Int, name: String): Int = memScoped { tempBufferAddress { glGetUniformLocation(program.convert(), name).convert() } }
    override fun getBooleanv(pname: Int, data: FBuffer): Unit = tempBufferAddress { glGetBooleanv(pname.convert(), data.unsafeAddress().reinterpret()) }
    override fun getBufferParameteriv(target: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetBufferParameteriv(target.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getError(): Int = tempBufferAddress { glGetError().convert() }
    override fun getFloatv(pname: Int, data: FBuffer): Unit = tempBufferAddress { glGetFloatv(pname.convert(), data.unsafeAddress().reinterpret()) }
    override fun getFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetFramebufferAttachmentParameteriv(target.convert(), attachment.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getIntegerv(pname: Int, data: FBuffer): Unit = tempBufferAddress { glGetIntegerv(pname.convert(), data.unsafeAddress().reinterpret()) }
    override fun getProgramInfoLog(program: Int, bufSize: Int, length: FBuffer, infoLog: FBuffer): Unit = tempBufferAddress { glGetProgramInfoLog(program.convert(), bufSize.convert(), length.unsafeAddress().reinterpret(), infoLog.unsafeAddress().reinterpret()) }
    override fun getRenderbufferParameteriv(target: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetRenderbufferParameteriv(target.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getProgramiv(program: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetProgramiv(program.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getShaderiv(shader: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetShaderiv(shader.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getShaderInfoLog(shader: Int, bufSize: Int, length: FBuffer, infoLog: FBuffer): Unit = tempBufferAddress { glGetShaderInfoLog(shader.convert(), bufSize.convert(), length.unsafeAddress().reinterpret(), infoLog.unsafeAddress().reinterpret()) }
    override fun getShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: FBuffer, precision: FBuffer): Unit = tempBufferAddress { Unit }
    override fun getShaderSource(shader: Int, bufSize: Int, length: FBuffer, source: FBuffer): Unit = tempBufferAddress { glGetShaderSource(shader.convert(), bufSize.convert(), length.unsafeAddress().reinterpret(), source.unsafeAddress().reinterpret()) }
    override fun getString(name: Int): String = tempBufferAddress { ((glGetString(name.convert()))?.toKString() ?: "") }
    override fun getTexParameterfv(target: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetTexParameterfv(target.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getTexParameteriv(target: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetTexParameteriv(target.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getUniformfv(program: Int, location: Int, params: FBuffer): Unit = tempBufferAddress { glGetUniformfv(program.convert(), location.convert(), params.unsafeAddress().reinterpret()) }
    override fun getUniformiv(program: Int, location: Int, params: FBuffer): Unit = tempBufferAddress { glGetUniformiv(program.convert(), location.convert(), params.unsafeAddress().reinterpret()) }
    override fun getVertexAttribfv(index: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetVertexAttribfv(index.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getVertexAttribiv(index: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glGetVertexAttribiv(index.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun getVertexAttribPointerv(index: Int, pname: Int, pointer: FBuffer): Unit = tempBufferAddress { glGetVertexAttribPointerv(index.convert(), pname.convert(), pointer.unsafeAddress().reinterpret()) }
    override fun hint(target: Int, mode: Int): Unit = tempBufferAddress { glHint(target.convert(), mode.convert()) }
    override fun isBuffer(buffer: Int): Boolean = tempBufferAddress { glIsBuffer(buffer.convert()).toBool() }
    override fun isEnabled(cap: Int): Boolean = tempBufferAddress { glIsEnabled(cap.convert()).toBool() }
    override fun isFramebuffer(framebuffer: Int): Boolean = tempBufferAddress { glIsFramebuffer(framebuffer.convert()).toBool() }
    override fun isProgram(program: Int): Boolean = tempBufferAddress { glIsProgram(program.convert()).toBool() }
    override fun isRenderbuffer(renderbuffer: Int): Boolean = tempBufferAddress { glIsRenderbuffer(renderbuffer.convert()).toBool() }
    override fun isShader(shader: Int): Boolean = tempBufferAddress { glIsShader(shader.convert()).toBool() }
    override fun isTexture(texture: Int): Boolean = tempBufferAddress { glIsTexture(texture.convert()).toBool() }
    override fun lineWidth(width: Float): Unit = tempBufferAddress { glLineWidth(width) }
    override fun linkProgram(program: Int): Unit = tempBufferAddress { glLinkProgram(program.convert()) }
    override fun pixelStorei(pname: Int, param: Int): Unit = tempBufferAddress { glPixelStorei(pname.convert(), param.convert()) }
    override fun polygonOffset(factor: Float, units: Float): Unit = tempBufferAddress { glPolygonOffset(factor, units) }
    override fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: FBuffer): Unit = tempBufferAddress { glReadPixels(x.convert(), y.convert(), width.convert(), height.convert(), format.convert(), type.convert(), pixels.unsafeAddress()) }
    override fun releaseShaderCompiler(): Unit = tempBufferAddress { Unit }
    override fun renderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int): Unit = tempBufferAddress { glRenderbufferStorage(target.convert(), internalformat.convert(), width.convert(), height.convert()) }
    override fun sampleCoverage(value: Float, invert: Boolean): Unit = tempBufferAddress { glSampleCoverage(value, invert.toInt().convert()) }
    override fun scissor(x: Int, y: Int, width: Int, height: Int): Unit = tempBufferAddress { glScissor(x.convert(), y.convert(), width.convert(), height.convert()) }
    override fun shaderBinary(count: Int, shaders: FBuffer, binaryformat: Int, binary: FBuffer, length: Int): Unit = tempBufferAddress { throw KmlGlException("shaderBinary not implemented in Native") }
    override fun shaderSource(shader: Int, string: String): Unit = memScoped { tempBufferAddress { run {
                memScoped {
                    val lengths = allocArray<IntVar>(1)
                    val strings = allocArray<CPointerVar<ByteVar>>(1)
                    lengths[0] = strlen(string).convert()
                    strings[0] = string.cstr.placeTo(this)
                    glShaderSource(shader.convert(), 1.convert(), strings.reinterpret(), lengths.reinterpret())
                }
                } } }
    override fun stencilFunc(func: Int, ref: Int, mask: Int): Unit = tempBufferAddress { glStencilFunc(func.convert(), ref.convert(), mask.convert()) }
    override fun stencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int): Unit = tempBufferAddress { glStencilFuncSeparate(face.convert(), func.convert(), ref.convert(), mask.convert()) }
    override fun stencilMask(mask: Int): Unit = tempBufferAddress { glStencilMask(mask.convert()) }
    override fun stencilMaskSeparate(face: Int, mask: Int): Unit = tempBufferAddress { glStencilMaskSeparate(face.convert(), mask.convert()) }
    override fun stencilOp(fail: Int, zfail: Int, zpass: Int): Unit = tempBufferAddress { glStencilOp(fail.convert(), zfail.convert(), zpass.convert()) }
    override fun stencilOpSeparate(face: Int, sfail: Int, dpfail: Int, dppass: Int): Unit = tempBufferAddress { glStencilOpSeparate(face.convert(), sfail.convert(), dpfail.convert(), dppass.convert()) }
    override fun texImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: FBuffer?): Unit = tempBufferAddress { glTexImage2D(target.convert(), level.convert(), internalformat.convert(), width.convert(), height.convert(), border.convert(), format.convert(), type.convert(), pixels?.unsafeAddress()) }
    override fun texImage2D(target: Int, level: Int, internalformat: Int, format: Int, type: Int, data: NativeImage): Unit = tempBufferAddress { run { val intData = (data as BitmapNativeImage).intData; if (intData != null) {	intData.usePinned { dataPin -> glTexImage2D(target.convert(), level.convert(), internalformat.convert(), data.width.convert(), data.height.convert(), 0.convert(), format.convert(), type.convert(), dataPin.addressOf(0)) }} else {	glTexImage2D(target.convert(), level.convert(), internalformat.convert(), data.width.convert(), data.height.convert(), 0.convert(), format.convert(), type.convert(), null)}} }
    override fun texParameterf(target: Int, pname: Int, param: Float): Unit = tempBufferAddress { glTexParameterf(target.convert(), pname.convert(), param) }
    override fun texParameterfv(target: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glTexParameterfv(target.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun texParameteri(target: Int, pname: Int, param: Int): Unit = tempBufferAddress { glTexParameteri(target.convert(), pname.convert(), param.convert()) }
    override fun texParameteriv(target: Int, pname: Int, params: FBuffer): Unit = tempBufferAddress { glTexParameteriv(target.convert(), pname.convert(), params.unsafeAddress().reinterpret()) }
    override fun texSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: FBuffer): Unit = tempBufferAddress { glTexSubImage2D(target.convert(), level.convert(), xoffset.convert(), yoffset.convert(), width.convert(), height.convert(), format.convert(), type.convert(), pixels.unsafeAddress()) }
    override fun uniform1f(location: Int, v0: Float): Unit = tempBufferAddress { glUniform1f(location.convert(), v0) }
    override fun uniform1fv(location: Int, count: Int, value: FBuffer): Unit = tempBufferAddress { glUniform1fv(location.convert(), count.convert(), value.unsafeAddress().reinterpret()) }
    override fun uniform1i(location: Int, v0: Int): Unit = tempBufferAddress { glUniform1i(location.convert(), v0.convert()) }
    override fun uniform1iv(location: Int, count: Int, value: FBuffer): Unit = tempBufferAddress { glUniform1iv(location.convert(), count.convert(), value.unsafeAddress().reinterpret()) }
    override fun uniform2f(location: Int, v0: Float, v1: Float): Unit = tempBufferAddress { glUniform2f(location.convert(), v0, v1) }
    override fun uniform2fv(location: Int, count: Int, value: FBuffer): Unit = tempBufferAddress { glUniform2fv(location.convert(), count.convert(), value.unsafeAddress().reinterpret()) }
    override fun uniform2i(location: Int, v0: Int, v1: Int): Unit = tempBufferAddress { glUniform2i(location.convert(), v0.convert(), v1.convert()) }
    override fun uniform2iv(location: Int, count: Int, value: FBuffer): Unit = tempBufferAddress { glUniform2iv(location.convert(), count.convert(), value.unsafeAddress().reinterpret()) }
    override fun uniform3f(location: Int, v0: Float, v1: Float, v2: Float): Unit = tempBufferAddress { glUniform3f(location.convert(), v0, v1, v2) }
    override fun uniform3fv(location: Int, count: Int, value: FBuffer): Unit = tempBufferAddress { glUniform3fv(location.convert(), count.convert(), value.unsafeAddress().reinterpret()) }
    override fun uniform3i(location: Int, v0: Int, v1: Int, v2: Int): Unit = tempBufferAddress { glUniform3i(location.convert(), v0.convert(), v1.convert(), v2.convert()) }
    override fun uniform3iv(location: Int, count: Int, value: FBuffer): Unit = tempBufferAddress { glUniform3iv(location.convert(), count.convert(), value.unsafeAddress().reinterpret()) }
    override fun uniform4f(location: Int, v0: Float, v1: Float, v2: Float, v3: Float): Unit = tempBufferAddress { glUniform4f(location.convert(), v0, v1, v2, v3) }
    override fun uniform4fv(location: Int, count: Int, value: FBuffer): Unit = tempBufferAddress { glUniform4fv(location.convert(), count.convert(), value.unsafeAddress().reinterpret()) }
    override fun uniform4i(location: Int, v0: Int, v1: Int, v2: Int, v3: Int): Unit = tempBufferAddress { glUniform4i(location.convert(), v0.convert(), v1.convert(), v2.convert(), v3.convert()) }
    override fun uniform4iv(location: Int, count: Int, value: FBuffer): Unit = tempBufferAddress { glUniform4iv(location.convert(), count.convert(), value.unsafeAddress().reinterpret()) }
    override fun uniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: FBuffer): Unit = tempBufferAddress { glUniformMatrix2fv(location.convert(), count.convert(), transpose.toInt().convert(), value.unsafeAddress().reinterpret()) }
    override fun uniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FBuffer): Unit = tempBufferAddress { glUniformMatrix3fv(location.convert(), count.convert(), transpose.toInt().convert(), value.unsafeAddress().reinterpret()) }
    override fun uniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FBuffer): Unit = tempBufferAddress { glUniformMatrix4fv(location.convert(), count.convert(), transpose.toInt().convert(), value.unsafeAddress().reinterpret()) }
    override fun useProgram(program: Int): Unit = tempBufferAddress { glUseProgram(program.convert()) }
    override fun validateProgram(program: Int): Unit = tempBufferAddress { glValidateProgram(program.convert()) }
    override fun vertexAttrib1f(index: Int, x: Float): Unit = tempBufferAddress { glVertexAttrib1f(index.convert(), x) }
    override fun vertexAttrib1fv(index: Int, v: FBuffer): Unit = tempBufferAddress { glVertexAttrib1fv(index.convert(), v.unsafeAddress().reinterpret()) }
    override fun vertexAttrib2f(index: Int, x: Float, y: Float): Unit = tempBufferAddress { glVertexAttrib2f(index.convert(), x, y) }
    override fun vertexAttrib2fv(index: Int, v: FBuffer): Unit = tempBufferAddress { glVertexAttrib2fv(index.convert(), v.unsafeAddress().reinterpret()) }
    override fun vertexAttrib3f(index: Int, x: Float, y: Float, z: Float): Unit = tempBufferAddress { glVertexAttrib3f(index.convert(), x, y, z) }
    override fun vertexAttrib3fv(index: Int, v: FBuffer): Unit = tempBufferAddress { glVertexAttrib3fv(index.convert(), v.unsafeAddress().reinterpret()) }
    override fun vertexAttrib4f(index: Int, x: Float, y: Float, z: Float, w: Float): Unit = tempBufferAddress { glVertexAttrib4f(index.convert(), x, y, z, w) }
    override fun vertexAttrib4fv(index: Int, v: FBuffer): Unit = tempBufferAddress { glVertexAttrib4fv(index.convert(), v.unsafeAddress().reinterpret()) }
    override fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, pointer: Long): Unit = tempBufferAddress { glVertexAttribPointer(index.convert(), size.convert(), type.convert(), normalized.toInt().convert(), stride.convert(), pointer.toCPointer<IntVar>()) }
    override fun viewport(x: Int, y: Int, width: Int, height: Int): Unit = tempBufferAddress { glViewport(x.convert(), y.convert(), width.convert(), height.convert()) }

    override val isInstancedSupported: Boolean get() = true
    override fun drawArraysInstanced(mode: Int, first: Int, count: Int, instancecount: Int) = glDrawArraysInstancedARB(mode.convert(), first, count, instancecount)
    override fun drawElementsInstanced(mode: Int, count: Int, type: Int, indices: Int, instancecount: Int) = glDrawElementsInstancedARB(mode.convert(), count, type.convert(), indices.toLong().toCPointer<IntVar>(), instancecount)
    override fun vertexAttribDivisor(index: Int, divisor: Int) = glVertexAttribDivisorARB(index.convert(), divisor.convert())
    override fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int) = glRenderbufferStorageMultisample(target.convert(), samples, internalformat.convert(), width, height)
}
