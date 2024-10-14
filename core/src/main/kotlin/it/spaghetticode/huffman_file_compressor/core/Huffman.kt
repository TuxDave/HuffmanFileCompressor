package it.spaghetticode.huffman_file_compressor.core

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import javax.lang.model.type.NullType
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.pow

data class HuffmanTreeNode<T>(
    val frequency: Int,
    val symbol: T?,
    val left: HuffmanTreeNode<T>? = null,
    val right: HuffmanTreeNode<T>? = null
) {
    fun isLeaf(): Boolean {
        return left == null && right == null
    }

    fun getCompressionMap(): Map<T, List<Boolean>> {
        val map: HashMap<T, List<Boolean>> = HashMap()
        fun explorer(tree: HuffmanTreeNode<T>, path: List<Boolean> = listOf()) {
            if (tree.isLeaf()) {
                tree.symbol?.let { symbol ->
                    map[symbol] = path
                    println("${Char((symbol as Byte).toUShort())}\t=\t${tree.frequency}: $path")
                } ?: {
                    throw IllegalArgumentException("Bad Huffman Tree")
                }
            } else {
                tree.left?.let { left -> explorer(left, path + false) }
                tree.right?.let { right -> explorer(right, path + true) }
            }
        }

        explorer(this)
        return map
    }

    fun doCompression(input: File, output: File) {
        val w = output.outputStream().buffered()
        writePrelude(w);
        w.close()

        val r = output.inputStream().buffered()
        r.close()
    }

    /**
     * writes the prelude inside the file.
     * PRELUDE is composed of all the needed information to decompress the result file.
     * it comprends the huffman tree ecc
     * */
    private fun writePrelude(w: BufferedOutputStream): Result<Boolean> {
        w.write(byteArrayOf(1))
        for (entry in getCompressionMap()) {
            entry as Map.Entry<Byte, List<Boolean>>
            //key, length in bit of the value
            w.write(byteArrayOf(entry.key, entry.value.size.toByte()))

            val bytes = ByteArray(ceil(entry.value.size / 8.0).toInt()) { it -> 0 }
            val content = entry.value.reversed().mapIndexed { i, it ->
                (if (it) 1 else 0) * 2.0.pow(i.toDouble())
            }.sum().toInt() shl ((bytes.size - ceil(entry.value.size / 8.0)).toInt())

            for (i in 0..<bytes.size) {
                bytes[bytes.size - 1 - i] = (content shr (i * 8)).toByte()
            }
            w.write(bytes)
            w.flush()
        }
        return Result.success(true)
    }

    companion object {
        fun <T> huffmanize(m: HashMap<T, Int>): HuffmanTreeNode<T> {
            val pq = PriorityQueue<HuffmanTreeNode<T>> { it1, it2 ->
                it1.frequency compareTo it2.frequency
            }
            m.entries.forEach {
                pq.add(
                    HuffmanTreeNode(
                        frequency = it.value,
                        symbol = it.key,
                    )
                )
            }

            while (pq.size > 1) {
                val n1 = pq.remove()
                val n2 = pq.remove()
                pq.add(
                    HuffmanTreeNode(
                        frequency = n1.frequency + n2.frequency,
                        symbol = null,
                        right = n1,
                        left = n2
                    )
                )
            }
            return pq.remove()
        }
    }
}