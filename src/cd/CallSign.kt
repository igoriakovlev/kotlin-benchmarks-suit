package cd

class CallSign(private val value: Int) : Comparable<CallSign> {
    override fun compareTo(other: CallSign): Int =
        value.compareTo(other.value)
}