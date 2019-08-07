package timber.gradle.transform

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.LocalVariablesSorter

private const val LOG_TYPE = "android/util/Log"
private const val OBJECT_TYPE = "java/lang/Object"
private const val STRING_TYPE = "java/lang/String"
private const val THROWABLE_TYPE = "java/lang/Throwable"
private const val TIMBER_TYPE = "timber/log/Timber"
private const val TREE_TYPE = "timber/log/Timber\$Tree"

private val TARGET_METHODS = setOf("d", "e", "i", "v", "w", "wtf")

internal class MethodRewriter(access: Int, desc: String, mv: MethodVisitor) : LocalVariablesSorter(Opcodes.ASM7, access, desc, mv) {
  override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
    if (opcode == Opcodes.INVOKESTATIC && owner == LOG_TYPE) {
      if (name in TARGET_METHODS) {
        if (descriptor == "(L$STRING_TYPE;L$STRING_TYPE;L$THROWABLE_TYPE;)I") {
          // Log.x(tag, msg, tr)
          rewriteTagMsgThrowable(name)
          return
        } else if (descriptor == "(L$STRING_TYPE;L$STRING_TYPE;)I") {
          // Log.x(tag, msg)
          rewriteTagMsg(name)
          return
        } else if (name.startsWith("w") && descriptor == "(L$STRING_TYPE;L$THROWABLE_TYPE;)I") {
          // Log.x(tag, tr)
          rewriteTagThrowable(name)
          return
        }
      } else if (name == "println" && descriptor == "(IL$STRING_TYPE;L$STRING_TYPE;)I") {
        // Log.println(priority, tag, msg)
        rewritePrintln()
        return
      }
    }
    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
  }

  /**
   * Rewrites `Log.$x(tag, msg)` into `Timber.tag(tag).$x(msg, new Object[0])`
   */
  private fun rewriteTagMsg(method: String) {
    swap() // tag, msg -> msg, tag
    invokeTag()
    swap() // msg, tree -> tree, msg
    emptyArray()
    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TREE_TYPE, method, "(L$STRING_TYPE;[L$OBJECT_TYPE;)V", false)
    const0()
  }

  /**
   * Rewrites `Log.$x(tag, msg, tr)` into `Timber.tag(tag).$x(tr, msg, new Object[0])`
   */
  private fun rewriteTagMsgThrowable(method: String) {
    val slot = store(THROWABLE_TYPE)
    swap() // tag, msg -> msg, tag
    invokeTag()
    swap() // msg, tree -> tree, msg
    load(slot)
    swap() // msg, throwable -> throwable, msg
    emptyArray()
    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TREE_TYPE, method, "(L$THROWABLE_TYPE;L$STRING_TYPE;[L$OBJECT_TYPE;)V", false)
    const0()
  }

  /**
   * Rewrites `Log.$x(tag, tr)` into `Timber.tag(tag).x(tr)`
   */
  private fun rewriteTagThrowable(method: String) {
    swap() // tag, tr -> tr, tag
    invokeTag()
    swap() // tr, tree -> tree, tr
    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TREE_TYPE, method, "(L$THROWABLE_TYPE;)V", false)
    const0()
  }

  /**
   * Rewrites `Log.println(priority, tag, msg)` to `Timber.tag(tag).log(priority, msg, new Object[0])`
   */
  private fun rewritePrintln() {
    val slot = store(STRING_TYPE)
    invokeTag()
    swap() // priority, tree -> tree, priority
    load(slot)
    emptyArray()
    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, TREE_TYPE, "log", "(IL$STRING_TYPE;[L$OBJECT_TYPE;)V", false)
    const0()
  }

  private fun swap() {
    super.visitInsn(Opcodes.SWAP)
  }

  private fun invokeTag() {
    super.visitMethodInsn(Opcodes.INVOKESTATIC, TIMBER_TYPE, "tag", "(L$STRING_TYPE;)L$TREE_TYPE;", false)
  }

  private fun emptyArray() {
    super.visitInsn(Opcodes.ICONST_0)
    super.visitTypeInsn(Opcodes.ANEWARRAY, OBJECT_TYPE)
  }

  private fun const0() {
    super.visitInsn(Opcodes.ICONST_0)
  }

  private fun store(type: String): Int {
    val slot = newLocal(Type.getObjectType(type))
    super.visitVarInsn(Opcodes.ASTORE, slot)
    return slot
  }

  private fun load(slot: Int) {
    super.visitVarInsn(Opcodes.ALOAD, slot)
  }
}