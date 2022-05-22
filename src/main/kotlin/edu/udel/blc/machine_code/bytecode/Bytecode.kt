package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.machine_code.MachineCode
import java.io.File
import java.util.jar.Attributes.Name.MAIN_CLASS
import java.util.jar.Attributes.Name.MANIFEST_VERSION
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class Bytecode(
    val main: ClassFileObject,
    val addedClasses: List<ClassFileObject>
) : MachineCode {

    fun run() {
        addedClasses.forEach { it.load() }
        val mainClass: Class<*> = main.load()
        val main = mainClass.getMethod("main", Array<String>::class.java)
        main.invoke(null, arrayOf<String>())
    }

    override fun writeTo(file: File) {
        val manifest = Manifest()
        manifest.mainAttributes[MANIFEST_VERSION] = "1.0"
        manifest.mainAttributes[MAIN_CLASS] = main.slashBinaryName

        file.outputStream().use { out ->
            JarOutputStream(out, manifest).use { jar ->
                jar.putNextEntry(JarEntry("${main.slashBinaryName}.class"))
                jar.write(main.bytes)
                jar.closeEntry()
                for (struct in addedClasses) {
                    jar.putNextEntry(JarEntry("${struct.slashBinaryName}.class"))
                    jar.write(struct.bytes)
                    jar.closeEntry()
                }
            }
        }
    }
}