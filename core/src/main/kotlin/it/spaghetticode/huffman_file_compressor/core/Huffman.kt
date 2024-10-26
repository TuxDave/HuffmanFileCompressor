package it.spaghetticode.huffman_file_compressor.core

import com.sun.org.apache.xpath.internal.operations.Bool
import it.spaghetticode.huffman_file_compressor.core.HuffmanTreeNode.Companion.huffmanize
import java.io.BufferedOutputStream
import java.io.File
import java.util.*
import kotlin.Char
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.math.pow

const val MAGIC: Byte = 69

private data class HuffmanTreeNode<T>(
    val frequency: Int,
    val symbol: T?,
    val left: HuffmanTreeNode<T>? = null,
    val right: HuffmanTreeNode<T>? = null
) {
    fun isLeaf(): Boolean {
        return left == null && right == null
    }

    /*private*/ fun getCompressionMap(): Map<T, List<Boolean>> {
        val map: HashMap<T, List<Boolean>> = HashMap()
        fun explorer(tree: HuffmanTreeNode<T>, path: List<Boolean> = listOf()) {
            if (tree.isLeaf()) {
                tree.symbol?.let { symbol ->
                    map[symbol] = path
                    println("${Char((symbol as Byte).toUShort())}, $symbol\t=\t${tree.frequency}: $path")
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

    fun doCompression(input: File, output: File): Boolean {
        val w = output.outputStream().buffered()
        val r = input.inputStream().buffered()

        val map = getCompressionMap() as HashMap<Byte, List<Boolean>>
        if (writePrelude(w, map).isFailure) {
            return false
        }

        w.write(byteArrayOf(MAGIC))

        val rBuff = ByteArray(1)
        var wBuff: Queue<Byte> = LinkedList<Byte>()
        while (r.read(rBuff) != -1) {
            wBuff.addAll(map[rBuff[0]]?.map { if (it) 1 else 0 } ?: emptyList())
            fromQueueToByte(wBuff)?.let { it ->
                w.write(byteArrayOf(it))
            }
        }

        if(wBuff.isEmpty()) {
            w.write(byteArrayOf(0))
        } else if (wBuff.size in 1..< 8) {
            val n: Byte = (8 - wBuff.size).toByte()
            wBuff.addAll(ByteArray(n.toInt()).toList())
            fromQueueToByte(wBuff)?.let { it ->
                w.write(byteArrayOf(it))
            }
            w.write(byteArrayOf(n))
        }

        w.close()
        r.close()
        return true
    }

    /**
     * writes the prelude inside the file.
     * PRELUDE is composed of all the needed information to decompress the result file.
     * it comprends the huffman tree ecc
     * */
    private fun writePrelude(w: BufferedOutputStream, map: Map<Byte, List<Boolean>>): Result<Boolean> {
        w.write(byteArrayOf(1))
        val symbols = ByteArray(4)
        for (i in 0..<symbols.size) {
            symbols[4 - 1 - i] = (map.size shr i * 8).toByte()
        }
        w.write(symbols)

        for (entry in map) {
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

fun zip(input: File, output: File): Boolean{
    val tree = huffmanize(countOccurence(input))
    return tree.doCompression(input, output)
}

fun unzip(zipped: File, unzipped: File): Boolean {
    //retrieving information
    val r = zipped.inputStream().buffered()
    val w = unzipped.outputStream().buffered()

    var rBuff = ByteArray(1)
    r.read(rBuff)
    val len = rBuff[0]

    rBuff = ByteArray(4)
    r.read(rBuff)
    val symbols = rBuff
        .reversed()
        .mapIndexed { i, it ->
            it.toInt() shl (i * 8)
        }.sum()

    val map = HashMap<List<Boolean>, Byte>()
    val nBytesBuff = ByteArray(len.toInt())
    val nBitsBuff = ByteArray(1)
    lateinit var seqBitsBuff: ByteArray
    for (symbol in 0..<symbols) {
        r.read(nBytesBuff)
        r.read(nBitsBuff)

        val bytes = ceil(nBitsBuff[0] / 8.0).toInt()
        seqBitsBuff = ByteArray(bytes)
        r.read(seqBitsBuff) // contiene adesso la sequenza shortenata, va shiftata
        val bitArr = BooleanArray(bytes * 8)
        seqBitsBuff.forEachIndexed { i, it ->
            fromByteToBooleans(it).forEachIndexed { i2, it2 ->
                bitArr[i * 8 + i2] = it2
            }
        }
        map[bitArr.drop(bytes * 8 - nBitsBuff[0])] = nBytesBuff[0]
    }

    run {
        val byte = ByteArray(1)
        r.read(byte)
        if(byte[0] != MAGIC) {
            return false
        }
    }

    var bitsBuff = listOf<Boolean>()
    rBuff = ByteArray(len.toInt())
    var red = 0
    do {
        if(bitsBuff.size < len * 8) {
            // legge e se il file è finito termina
            red = r.read(rBuff)
            if (red != -1) {
                bitsBuff += fromByteToBooleans(rBuff[0]).toList()
            }
        }
        for (i in bitsBuff.size - 1 downTo 0) {
            val value = map[bitsBuff.dropLast(i)]
            if (value != null) {
                w.write(byteArrayOf(value))
                bitsBuff = bitsBuff.drop(bitsBuff.size - i)
                break
            }
        }
    } while (red != -1)

    r.close()
    w.close()

    return true
}