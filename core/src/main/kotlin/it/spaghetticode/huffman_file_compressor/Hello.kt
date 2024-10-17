package it.spaghetticode.huffman_file_compressor

import it.spaghetticode.huffman_file_compressor.core.unzip
import it.spaghetticode.huffman_file_compressor.core.zip
import java.io.File

fun main(args: Array<String>) {
    val before = System.currentTimeMillis()
    zip(File("test.bin"), File("out.bin"))
    unzip(File("out.bin"), File("original.bin"))
    val after = System.currentTimeMillis()
    println("mills: " + (after - before))
}

