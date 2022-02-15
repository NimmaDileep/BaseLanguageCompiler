package edu.udel.blc.machine_code

import java.io.File


interface MachineCode {
    fun writeTo(file: File)
}