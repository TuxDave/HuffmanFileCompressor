package it.spaghetticode.huffman_file_compressor

import it.spaghetticode.huffman_file_compressor.core.HuffmanTreeNode
import it.spaghetticode.huffman_file_compressor.core.countOccurence
import java.io.File

fun main(args: Array<String>) {
    val before = System.currentTimeMillis()
    HuffmanTreeNode.huffmanize(countOccurence(File("test.bin")))
    val after = System.currentTimeMillis()
    println("mills: " + (after - before))
}

