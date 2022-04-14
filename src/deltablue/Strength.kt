/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * DeltaBlue.
 *
 * It is modified to use the SOM class library and Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package deltablue

import som.Dictionary.CustomHash
import som.IdentityDictionary

/*
 * Strengths are used to measure the relative importance of constraints. New
 * strengths may be inserted in the strength hierarchy without disrupting
 * current constraints. Strengths cannot be created outside this class, so
 * pointer comparison can be used for value comparison.
 */
class Strength private constructor(symbolicValue: Sym) {
    class Sym internal constructor(private val hash: Int) : CustomHash {
        override fun customHash(): Int = hash
    }

    private val arithmeticValue: Int =
        strengthTable.at(symbolicValue)!!

    fun sameAs(s: Strength): Boolean =
        arithmeticValue == s.arithmeticValue

    fun stronger(s: Strength): Boolean =
        arithmeticValue < s.arithmeticValue

    fun weaker(s: Strength): Boolean =
        arithmeticValue > s.arithmeticValue

    fun strongest(s: Strength): Strength =
        if (s.stronger(this)) s else this

    fun weakest(s: Strength): Strength =
        if (s.weaker(this)) s else this

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        val ABSOLUTE_STRONGEST = Sym(0)
        val REQUIRED = Sym(1)
        val STRONG_PREFERRED = Sym(2)
        val PREFERRED = Sym(3)
        val STRONG_DEFAULT = Sym(4)
        val DEFAULT = Sym(5)
        val WEAK_DEFAULT = Sym(6)
        val ABSOLUTE_WEAKEST = Sym(7)
        fun of(strength: Sym): Strength =
            strengthConstant.at(strength)!!

        private fun createStrengthTable(): IdentityDictionary<Sym, Int> {
            val strengthTable = IdentityDictionary<Sym, Int>()
            strengthTable.atPut(ABSOLUTE_STRONGEST, -10000)
            strengthTable.atPut(REQUIRED, -800)
            strengthTable.atPut(STRONG_PREFERRED, -600)
            strengthTable.atPut(PREFERRED, -400)
            strengthTable.atPut(STRONG_DEFAULT, -200)
            strengthTable.atPut(DEFAULT, 0)
            strengthTable.atPut(WEAK_DEFAULT, 500)
            strengthTable.atPut(ABSOLUTE_WEAKEST, 10000)
            return strengthTable
        }

        private val strengthTable: IdentityDictionary<Sym, Int> = createStrengthTable()
        private val strengthConstant: IdentityDictionary<Sym, Strength> = createStrengthConstants()
        private val absoluteWeakest: Strength = of(ABSOLUTE_WEAKEST)
        private val required: Strength = of(REQUIRED)

        private fun createStrengthConstants(): IdentityDictionary<Sym, Strength> {
            val strengthConstant = IdentityDictionary<Sym, Strength>()
            strengthTable.keys.forEach { key: Sym -> strengthConstant.atPut(key, Strength(key)) }
            return strengthConstant
        }

        fun absoluteWeakest(): Strength = absoluteWeakest

        fun required(): Strength = required
   }
}