/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 *
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards

class Packet private constructor(var identity: Int = 0, val kind: Int = IDLER) : RBObject() {

    constructor(link: Packet, identity: Int, kind: Int) : this(identity, kind) {
        _link = link
    }

    private var _link: Packet? = null
    var link: Packet
        get() = _link
            ?: error("Should not be called for this element")
        set(value) { _link = value }

    var datum = 0
    val data: IntArray = IntArray(DATA_SIZE)

    override fun toString(): String =
        "Packet id: $identity kind: $kind"

    companion object {
        const val DATA_SIZE = 4
        val NO_WORK = Packet()
    }
}