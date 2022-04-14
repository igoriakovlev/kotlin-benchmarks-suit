package som

import som.Dictionary.CustomHash

class IdentityDictionary<K : CustomHash?, V> : Dictionary<K, V> {
    class IdEntry<K, V>(hash: Int, key: K, value: V, next: Entry<K, V>?) :
        Entry<K, V>(hash, key, value, next) {
        override fun match(hash: Int, key: K): Boolean {
            return this.hash == hash && this.key === key
        }
    }

    constructor(size: Int) : super(size)
    constructor() : super(INITIAL_CAPACITY)

    override fun newEntry(key: K, value: V, hash: Int): Entry<K, V> {
        return IdEntry(hash, key, value, null)
    }
}