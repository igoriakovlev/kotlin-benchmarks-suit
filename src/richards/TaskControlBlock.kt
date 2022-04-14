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

class TaskControlBlock private constructor(
    val identity: Int,
    val priority: Int,
    private var input: Packet,
    anInitialState: TaskState,
    aBlock: ProcessFunction,
    aPrivateData: RBObject
)  : TaskState() {
    private val function: ProcessFunction
    private val handle: RBObject
    private var _link: TaskControlBlock? = null

    constructor(link: TaskControlBlock,
                identity: Int,
                priority: Int,
                input: Packet,
                anInitialState: TaskState,
                aBlock: ProcessFunction,
                aPrivateData: RBObject) : this(identity, priority, input, anInitialState, aBlock, aPrivateData) {
                    _link = link
                }

    val link: TaskControlBlock get() = _link
        ?: error("Should not be called for this element")

    init {
        isPacketPending = anInitialState.isPacketPending
        isTaskWaiting = anInitialState.isTaskWaiting
        isTaskHolding = anInitialState.isTaskHolding
        function = aBlock
        handle = aPrivateData
    }

    fun addInputAndCheckPriority(
        packet: Packet,
        oldTask: TaskControlBlock
    ): TaskControlBlock {
        if (NO_WORK == input) {
            input = packet
            isPacketPending = true
            if (priority > oldTask.priority) {
                return this
            }
        } else {
            input = append(packet, input)
        }
        return oldTask
    }

    fun runTask(): TaskControlBlock {
        val message: Packet?
        if (isWaitingWithPacket) {
            message = input
            input = message.link
            if (NO_WORK == input) {
                running()
            } else {
                packetPending()
            }
        } else {
            message = NO_WORK
        }
        return function.apply(message, handle)
    }

    companion object {
        val NO_TASK: TaskControlBlock =
            TaskControlBlock(0, 0, NO_WORK, TaskState(), { _, _ -> error("Should not be called") }, IdleTaskDataRecord())
    }
}