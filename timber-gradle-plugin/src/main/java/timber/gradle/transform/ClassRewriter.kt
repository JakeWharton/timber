package timber.gradle.transform

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

internal class ClassRewriter(cv: ClassVisitor): ClassVisitor(Opcodes.ASM7, cv) {
  override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
    val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
    return MethodRewriter(access, descriptor, mv)
  }
}