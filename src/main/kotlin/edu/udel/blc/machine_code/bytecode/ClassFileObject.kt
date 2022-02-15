package edu.udel.blc.machine_code.bytecode

class ClassFileObject(
    val slashBinaryName: String,
    val bytes: ByteArray
) {

    fun load(): Class<*> =
        ByteArrayClassLoader.defineClass(slashBinaryName, bytes)

    companion object {

        object ByteArrayClassLoader : ClassLoader() {
            init {
                registerAsParallelCapable()
            }

            fun defineClass(name: String, bytes: ByteArray): Class<*> {
                return defineClass(name, bytes, 0, bytes.size)
            }
        }

    }

}