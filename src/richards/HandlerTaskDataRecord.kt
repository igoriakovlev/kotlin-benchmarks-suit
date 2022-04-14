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

class HandlerTaskDataRecord : RBObject() {
    private var deviceIn: Packet = NO_WORK
    private var workIn: Packet = deviceIn

    fun deviceIn(): Packet = deviceIn

    fun deviceIn(aPacket: Packet) {
        deviceIn = aPacket
    }

    fun deviceInAdd(packet: Packet) {
        deviceIn = append(packet, deviceIn)
    }

    fun workIn(): Packet = workIn

    fun workIn(aWorkQueue: Packet) {
        workIn = aWorkQueue
    }

    fun workInAdd(packet: Packet) {
        workIn = append(packet, workIn)
    }
}