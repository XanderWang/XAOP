package com.xander.plugin.asm.lib

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter

open class TimeMethodVisitor(var methodName: String, access: Int, desc: String?, mv: MethodVisitor?)
  : LocalVariablesSorter(Opcodes.ASM5, access, desc, mv), Opcodes {

  /**
   * 访问注解
   */
  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor {
    val desc = desc.replace("/",".")
    if (desc.contains("com.xander.dev.tool.DebugTime")) {
      if (pluginConfig?.log == true) println("BaseMethodAdapter visitAnnotation :$desc")
      debugTime = true
    }
    return super.visitAnnotation(desc, visible)
  }

  /** Starts the visit of the method's code, if any (i.e. non abstract method). */
  override fun visitCode() {
    super.visitCode()
    if (debugTime) {
      timeVarIndex = newLocal(Type.LONG_TYPE)
      // 调用方法
      mv.visitMethodInsn(
          Opcodes.INVOKESTATIC,
          "java/lang/System",
          "currentTimeMillis",
          "()J",
          false
      )
      // 存入变量
      mv.visitVarInsn(Opcodes.LSTORE, timeVarIndex)
    }
  }

  override fun visitInsn(opcode: Int) {
    if (debugTime) {
      if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN || opcode == Opcodes.ATHROW) {
        // 新建一个变量
        val index = newLocal(Type.LONG_TYPE)
        // 调用方法
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "java/lang/System",
            "currentTimeMillis",
            "()J",
            false
        )
        // 读取参数
        mv.visitVarInsn(Opcodes.LLOAD, timeVarIndex)
        // 做减法
        mv.visitInsn(Opcodes.LSUB)
        // 写入变量
        mv.visitVarInsn(Opcodes.LSTORE, index)
        // 写入变量
        mv.visitLdcInsn(methodName)
        // 读取变量
        mv.visitVarInsn(Opcodes.LLOAD, index)
        // 调用方法
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/xander/dev/tool/help/LogHelper",
            "d",
            "(Ljava/lang/String;J)V",
            false
        )
      }
    }
    super.visitInsn(opcode)
  }

  private var timeVarIndex = 0
  private var debugTime = false
  protected var debugLine = false

  companion object {
    var pluginConfig = PluginConfig.debug
  }

  init {
    if (pluginConfig.log) {
      println("BaseMethodAdapter method:$methodName")
    }
  }
}