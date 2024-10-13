package it.spaghetticode.huffman_file_compressor.core

import java.io.File

fun countOccurence(f: File): HashMap<Byte, Int> {
    val occurence: HashMap<Byte, Int> = hashMapOf()

    val r = f.inputStream().buffered()
    val buff = ByteArray(1)
    while (r.read(buff) != -1) {
        val byte = buff[0]
        occurence[byte] = (occurence[byte] ?: 0) + 1
    }
    return occurence
}

