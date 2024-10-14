package it.spaghetticode.huffman_file_compressor.core

import java.io.File
import java.util.Queue
import java.util.Scanner
import kotlin.math.pow

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

fun fromQueueToByte(bits: Queue<Byte>): Byte? {
    if(bits.size < 8) {
        return null
    } else {
        var byte = 0
        for(i in 7 downTo 0) {
            byte += bits.poll() * 2.0.pow(i).toInt()
        }
        return byte.toByte()
    }
}

fun fromByteToBooleans(b: Byte): BooleanArray {
    var b = b.toInt()
    val ret = BooleanArray(8)
    for(i in 7 ..< 0) {
        if(b % 2 != 0) {
            ret[i] = true
        }
        b = b shr 1
    }
    return ret
}
