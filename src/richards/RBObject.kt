/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 * 
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards

import richards.Packet.Companion.NO_WORK

abstract class RBObject {
    fun append(packet: Packet, queueHead: Packet): Packet {
        packet.link = NO_WORK
        if (NO_WORK == queueHead) {
            return packet
        }
        var mouse = queueHead
        var link: Packet
        while (NO_WORK != mouse.link.also { link = it }) {
            mouse = link
        }
        mouse.link = packet
        return queueHead
    }

    companion object {
        const val IDLER = 0
        const val WORKER = 1
        const val HANDLER_A = 2
        const val HANDLER_B = 3
        const val DEVICE_A = 4
        const val DEVICE_B = 5
        const val NUM_TYPES = 6
        const val DEVICE_PACKET_KIND = 0
        const val WORK_PACKET_KIND = 1
    }
}