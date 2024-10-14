package it.spaghetticode.huffman_file_compressor

import it.spaghetticode.huffman_file_compressor.core.HuffmanTreeNode
import it.spaghetticode.huffman_file_compressor.core.countOccurence
import java.io.File

fun main(args: Array<String>) {
    val before = System.currentTimeMillis()
    val tree = HuffmanTreeNode.huffmanize(countOccurence(File("test.bin")))
    tree.doCompression(File("test.bin"), File("out.bin"))
    val after = System.currentTimeMillis()
    println("mills: " + (after - before))
}

