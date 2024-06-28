package it.spaghetticode.huffman_file_compressor.core

import java.util.*

data class HuffmanTreeNode<T>(
    val frequency: Int,
    val symbol: T?,
    val left: HuffmanTreeNode<T>? = null,
    val right: HuffmanTreeNode<T>? = null
) {
    fun isLeaf(): Boolean {
        return left == null && right == null
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