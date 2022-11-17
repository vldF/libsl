package org.jetbrains.research.libsl.asg

import org.jetbrains.research.libsl.utils.BackticksPolitics

sealed class QualifiedAccess : Atomic() {
    abstract var childAccess: QualifiedAccess?
    abstract val type: Type

    override fun toString(): String = dumpToString()

    override val value: Any? = null

    val lastChild: QualifiedAccess
        get() = childAccess?.lastChild ?: childAccess ?: this
}

/**
 * Access for variable.field[.childAccess]
 */
data class VariableAccess(
    val fieldName: String,
    override var childAccess: QualifiedAccess?,
    override val type: Type,
    val variable: Variable?
) : QualifiedAccess() {
    override fun toString(): String = dumpToString()
    override fun dumpToString(): String = when {
        childAccess != null && childAccess is VariableAccess -> "${BackticksPolitics.forIdentifier(fieldName)}.${childAccess?.dumpToString()}"
        childAccess != null -> "${BackticksPolitics.forIdentifier(fieldName)}${childAccess?.dumpToString()}"
        else -> fieldName
    }
}

data class ArrayAccess(
    var index: Atomic,
    override val type: Type
) : QualifiedAccess() {
    override var childAccess: QualifiedAccess? = null

    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append("[${index.dumpToString()}]")
        if (childAccess != null) {
            append(childAccess!!.dumpToString())
        }
    }
}

data class AutomatonGetter(
    val automaton: Automaton,
    val arg: FunctionArgument,
    override var childAccess: QualifiedAccess?,
) : QualifiedAccess() {
    override val type: Type = automaton.type

    override fun toString(): String = dumpToString()

    override fun dumpToString(): String = buildString {
        append(BackticksPolitics.forPeriodSeparated(automaton.name))
        append("(")
        append(BackticksPolitics.forIdentifier(arg.name))
        append(")")

        if (childAccess != null) {
            append(".")
            append(childAccess!!.dumpToString())
        }
    }
}