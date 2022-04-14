/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 * 
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards

open class TaskState : RBObject() {
    var isPacketPending = false
    var isTaskWaiting = false
    var isTaskHolding = false

    fun packetPending() {
        isPacketPending = true
        isTaskWaiting = false
        isTaskHolding = false
    }

    fun running() {
        isTaskHolding = false
        isTaskWaiting = false
        isPacketPending = false
    }

    fun waiting() {
        isTaskHolding = false
        isPacketPending = false
        isTaskWaiting = true
    }

    fun waitingWithPacket() {
        isTaskHolding = false
        isPacketPending = true
        isTaskWaiting = true
    }

    val isRunning: Boolean
        get() = !isPacketPending && !isTaskWaiting && !isTaskHolding
    val isTaskHoldingOrWaiting: Boolean
        get() = isTaskHolding || !isPacketPending && isTaskWaiting
    val isWaiting: Boolean
        get() = !isPacketPending && isTaskWaiting && !isTaskHolding
    val isWaitingWithPacket: Boolean
        get() = isPacketPending && isTaskWaiting && !isTaskHolding

    companion object {
        fun createPacketPending(): TaskState {
            val t = TaskState()
            t.packetPending()
            return t
        }

        fun createRunning(): TaskState {
            val t = TaskState()
            t.running()
            return t
        }

        fun createWaiting(): TaskState {
            val t = TaskState()
            t.waiting()
            return t
        }

        fun createWaitingWithPacket(): TaskState {
            val t = TaskState()
            t.waitingWithPacket()
            return t
        }
    }
}