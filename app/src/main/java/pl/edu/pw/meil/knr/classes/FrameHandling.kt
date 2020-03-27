package pl.edu.pw.meil.knr.classes

import java.util.*
import kotlin.experimental.and

class FrameHandling {
    private val halAPP = HalAPP.instance

    fun sendFrameInt(tag: Int, length: Int, output: IntArray) {
        val outputTab = ByteArray(19)
        var y = "#"
        var z1 = y.toByteArray()

        outputTab[0] = z1[0]

        var hex = Integer.toHexString(tag)
        y = "" + hex[0]
        z1 = y.toByteArray()
        outputTab[1] = z1[0]
        y = "" + hex[1]
        z1 = y.toByteArray()
        outputTab[2] = z1[0]

        for (i in 0 until length) {
            val unsignedByte: Byte = output[i].toByte() and 0xff.toByte()
            hex = String.format("%02x", unsignedByte).toUpperCase(Locale.ROOT)
            y = "" + hex[0]
            z1 = y.toByteArray()
            outputTab[i * 2 + 3] = z1[0]
            y = "" + hex[1]
            z1 = y.toByteArray()
            outputTab[i * 2 + 4] = z1[0]
        }

        for (i in 0 until 8 - length) {
            y = "x"
            z1 = y.toByteArray()
            outputTab[i * 2 + 3 + 2 * length] = z1[0]
            y = "x"
            z1 = y.toByteArray()
            outputTab[i * 2 + 4 + 2 * length] = z1[0]
        }
        halAPP!!.bluetoothConnection!!.write(outputTab)
    }
}