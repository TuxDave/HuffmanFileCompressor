package it.spaghetticode.huffman_file_compressor

import it.spaghetticode.huffman_file_compressor.core.unzip
import it.spaghetticode.huffman_file_compressor.core.zip
import java.io.File

fun main(args: Array<String>) {
    if(args.size != 3) {
        println("Specificare: zip/unzip, filepath src, filepath target")
    }
    val before = System.currentTimeMillis()
    if(args[0] == "zip"){
        zip(File(args[1]), File(args[2]))
    } else {
        unzip(File(args[1]), File(args[2]))
    }
    val after = System.currentTimeMillis()
    println("mills: " + (after - before))
}

