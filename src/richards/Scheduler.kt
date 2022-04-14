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
import richards.TaskControlBlock.Companion.NO_TASK
import richards.TaskState.Companion.createRunning
import richards.TaskState.Companion.createWaiting
import richards.TaskState.Companion.createWaitingWithPacket

class Scheduler : RBObject() {
    private var taskList: TaskControlBlock = NO_TASK
    private var currentTask: TaskControlBlock = NO_TASK
    private var currentTaskIdentity = 0
    private val taskTable: Array<TaskControlBlock> = Array(NUM_TYPES) { NO_TASK }
    private var queuePacketCount = 0
    private var holdCount = 0
    private var layout = 0

    private fun createDevice(
        identity: Int, priority: Int,
        workPacket: Packet, state: TaskState
    ) {
        val data = DeviceTaskDataRecord()
        createTask(
            identity, priority, workPacket, state,
            { workArg: Packet, wordArg: RBObject ->
                val dataRecord = wordArg as DeviceTaskDataRecord
                var functionWork = workArg
                if (NO_WORK == functionWork) {
                    if (NO_WORK == dataRecord.pending.also { functionWork = it }) {
                        return@createTask markWaiting()
                    } else {
                        dataRecord.pending = NO_WORK
                        return@createTask queuePacket(functionWork)
                    }
                } else {
                    dataRecord.pending = functionWork
                    if (TRACING) {
                        trace(functionWork.datum)
                    }
                    return@createTask holdSelf()
                }
            },
            data
        )
    }

    private fun createHandler(
        identity: Int, priority: Int,
        workPaket: Packet, state: TaskState
    ) {
        val data = HandlerTaskDataRecord()
        createTask(identity, priority, workPaket, state,
            { work: Packet, word: RBObject ->
                val dataRecord = word as HandlerTaskDataRecord
                if (NO_WORK != work) {
                    if (WORK_PACKET_KIND == work.kind) {
                        dataRecord.workInAdd(work)
                    } else {
                        dataRecord.deviceInAdd(work)
                    }
                }
                var workPacket: Packet
                if (NO_WORK == dataRecord.workIn().also { workPacket = it }) {
                    return@createTask markWaiting()
                } else {
                    val count = workPacket.datum
                    if (count >= Packet.DATA_SIZE) {
                        dataRecord.workIn(workPacket.link)
                        return@createTask queuePacket(workPacket)
                    } else {
                        var devicePacket: Packet
                        if (NO_WORK == dataRecord.deviceIn().also { devicePacket = it }) {
                            return@createTask markWaiting()
                        } else {
                            dataRecord.deviceIn(devicePacket.link)
                            devicePacket.datum = workPacket.data[count]
                            workPacket.datum = count + 1
                            return@createTask queuePacket(devicePacket)
                        }
                    }
                }
            }, data
        )
    }

    private fun createIdler(
        identity: Int, priority: Int, work: Packet,
        state: TaskState
    ) {
        val data = IdleTaskDataRecord()
        createTask(identity, priority, work, state,
            { _: Packet, wordArg: RBObject ->
                val dataRecord = wordArg as IdleTaskDataRecord
                dataRecord.count = dataRecord.count - 1
                if (0 == dataRecord.count) {
                    return@createTask holdSelf()
                } else {
                    if (0 == dataRecord.control and 1) {
                        dataRecord.control = dataRecord.control / 2
                        return@createTask release(DEVICE_A)
                    } else {
                        dataRecord.control = dataRecord.control / 2 xor 53256
                        return@createTask release(DEVICE_B)
                    }
                }
            }, data
        )
    }

    private fun createPacket(link: Packet, identity: Int, kind: Int): Packet =
        Packet(link, identity, kind)

    private fun createTask(
        identity: Int, priority: Int,
        work: Packet, state: TaskState,
        aBlock: ProcessFunction, data: RBObject
    ) {
        val t = TaskControlBlock(
            taskList, identity,
            priority, work, state, aBlock, data
        )
        taskList = t
        taskTable[identity] = t
    }

    private fun createWorker(
        identity: Int, priority: Int,
        workPaket: Packet, state: TaskState
    ) {
        val dataRecord = WorkerTaskDataRecord()
        createTask(identity, priority, workPaket, state,
            { work: Packet, word: RBObject ->
                val data = word as WorkerTaskDataRecord
                if (NO_WORK == work) {
                    return@createTask markWaiting()
                } else {
                    data.destination = if (HANDLER_A == data.destination) HANDLER_B else HANDLER_A
                    work.identity = data.destination
                    work.datum = 0
                    for (i in 0 until Packet.DATA_SIZE) {
                        data.count = data.count + 1
                        if (data.count > 26) {
                            data.count = 1
                        }
                        work.data[i] = 65 + data.count - 1
                    }
                    return@createTask queuePacket(work)
                }
            }, dataRecord
        )
    }

    fun start(): Boolean {
        createIdler(IDLER, 0, NO_WORK, createRunning())
        var workQ: Packet = createPacket(NO_WORK, WORKER, WORK_PACKET_KIND)
        workQ = createPacket(workQ, WORKER, WORK_PACKET_KIND)
        createWorker(WORKER, 1000, workQ, createWaitingWithPacket())
        workQ = createPacket(NO_WORK, DEVICE_A, DEVICE_PACKET_KIND)
        workQ = createPacket(workQ, DEVICE_A, DEVICE_PACKET_KIND)
        workQ = createPacket(workQ, DEVICE_A, DEVICE_PACKET_KIND)
        createHandler(HANDLER_A, 2000, workQ, createWaitingWithPacket())
        workQ = createPacket(NO_WORK, DEVICE_B, DEVICE_PACKET_KIND)
        workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND)
        workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND)
        createHandler(HANDLER_B, 3000, workQ, createWaitingWithPacket())
        createDevice(DEVICE_A, 4000, NO_WORK, createWaiting())
        createDevice(DEVICE_B, 5000, NO_WORK, createWaiting())
        schedule()
        return queuePacketCount == 23246 && holdCount == 9297
    }

    private fun findTask(identity: Int): TaskControlBlock {
        val t = taskTable[identity]
        if (NO_TASK == t) {
            throw RuntimeException("findTask failed")
        }
        return t
    }

    private fun holdSelf(): TaskControlBlock {
        holdCount += 1
        currentTask.isTaskHolding = true
        return currentTask.link
    }

    private fun queuePacket(packet: Packet): TaskControlBlock {
        val t = findTask(packet.identity)
        if (NO_TASK == t) {
            return NO_TASK
        }
        queuePacketCount += 1
        packet.link = NO_WORK
        packet.identity = currentTaskIdentity
        return t.addInputAndCheckPriority(packet, currentTask)
    }

    private fun release(identity: Int): TaskControlBlock {
        val t = findTask(identity)
        if (NO_TASK == t) {
            return NO_TASK
        }
        t.isTaskHolding = false
        return if (t.priority > currentTask.priority) t else currentTask
    }

    private fun trace(id: Int) {
        layout -= 1
        if (0 >= layout) {
            println()
            layout = 50
        }
        print(id)
    }

    private fun markWaiting(): TaskControlBlock {
        currentTask.isTaskWaiting = true
        return currentTask
    }

    private fun schedule() {
        currentTask = taskList
        while (NO_TASK != currentTask) {
            if (currentTask.isTaskHoldingOrWaiting) {
                this.currentTask = currentTask.link
            } else {
                currentTaskIdentity = currentTask.identity
                if (TRACING) {
                    trace(currentTaskIdentity)
                }
                this.currentTask = currentTask.runTask()
            }
        }
    }

    companion object {
        private const val TRACING = false
    }
}