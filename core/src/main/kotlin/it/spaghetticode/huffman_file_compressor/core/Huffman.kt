package it.spaghetticode.huffman_file_compressor.core

import java.util.*
import kotlin.collections.HashMap

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
                    map.put(symbol, path)
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

    //TODO: scrivere l'albero (in qualche modo) dentro il file compresso e applicare la compressione

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