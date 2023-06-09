package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.nodes.references.AnnotationReference
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.type.Type.Companion.UNRESOLVED_TYPE_SYMBOL
import org.jetbrains.research.libsl.utils.BackticksPolitics

enum class ArithmeticUnaryOp(val string: String) {
    PLUS("+"), MINUS("-"), INVERSION("!"), INCREMENT("++"), DECREMENT("--");

    companion object {
        fun fromString(str: String) = ArithmeticUnaryOp.values().first { op -> op.string == str }
    }
}

enum class VariableKeyword(val string: String) {
    VAR("var"), VAL("val"), RESULT(""), ARGUMENT("");

    companion object {
        fun fromString(str: String) = VariableKeyword.values().first { op -> op.string == str }
    }
}

open class Variable(
    open val keyword: VariableKeyword,
    open var name: String,
    open var typeReference: TypeReference
) : Expression() {
    open val fullName: String
        get() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Variable) return false

        if (name != other.name) return false
        if (typeReference != other.typeReference) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + typeReference.hashCode()
        return result
    }
}

data class ResultVariable(
    override var typeReference: TypeReference
) : Variable(keyword = VariableKeyword.RESULT, name = "result", typeReference)

@Suppress("unused")
class FunctionArgument(
    name: String,
    typeReference: TypeReference,
    val index: Int,
    var annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
    var targetAutomaton: AutomatonReference? = null
) : Variable(keyword = VariableKeyword.ARGUMENT, name, typeReference) {
    lateinit var function: Function

    override val fullName: String
        get() = "${function.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotationUsages.isNotEmpty()) {
            append(
                formatListEmptyLineAtEndIfNeeded(
                    annotationUsages,
                    appendEndLineAtTheEnd = false,
                    onSeparatedLines = false
                )
            )
            append(IPrinter.SPACE)
        }
        append(BackticksPolitics.forIdentifier(name))
        append(": ")

        val typeName = if (targetAutomaton != null) {
            targetAutomaton!!.name
        } else {
            typeReference.name
        }

        append(BackticksPolitics.forTypeIdentifier(typeName))
    }
}

@Suppress("unused")
class ActionParameter(
    name: String,
    typeReference: TypeReference,
    val index: Int,
    var annotation: AnnotationReference? = null
) : Variable(keyword = VariableKeyword.ARGUMENT, name, typeReference)

@Suppress("MemberVisibilityCanBePrivate")
class DeclaredActionParameter(
    name: String,
    typeReference: TypeReference,
    val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
) : Variable(keyword = VariableKeyword.ARGUMENT, name, typeReference) {
    override fun dumpToString(): String = buildString {
        if (annotationUsages.isNotEmpty()) {
            append(formatListEmptyLineAtEndIfNeeded(annotationUsages, onSeparatedLines = false))
            append(IPrinter.SPACE)
        }

        append("${BackticksPolitics.forIdentifier(name)}: ${typeReference.name}")
    }
}

class ConstructorArgument(
    keyword: VariableKeyword,
    name: String,
    typeReference: TypeReference,
    private val annotationUsages: MutableList<AnnotationUsage> = mutableListOf(),
) : Variable(keyword, name, typeReference) {
    lateinit var automaton: Automaton

    override val fullName: String
        get() = "${automaton.name}.$name"

    override fun dumpToString(): String = buildString {
        if (annotationUsages.isNotEmpty()) {
            append(formatListEmptyLineAtEndIfNeeded(annotationUsages, onSeparatedLines = false))
            append(IPrinter.SPACE)
        }

        append("${keyword.string} ${BackticksPolitics.forIdentifier(name)}: ")
        append(BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL))
    }
}

@Suppress("MemberVisibilityCanBePrivate")
class VariableWithInitialValue(
    keyword: VariableKeyword,
    name: String,
    typeReference: TypeReference,
    val annotationUsage: MutableList<AnnotationUsage> = mutableListOf(),
    val initialValue: Expression?,
) : Variable(keyword, name, typeReference) {
    override fun dumpToString(): String = buildString {
        append(formatListEmptyLineAtEndIfNeeded(annotationUsage))
        append("${keyword.string} ${BackticksPolitics.forIdentifier(name)}: ")
        append(BackticksPolitics.forTypeIdentifier(typeReference.resolve()?.fullName ?: UNRESOLVED_TYPE_SYMBOL))
        if (initialValue != null) {
            append(" = ${initialValue.dumpToString()};")
        } else {
            append(";")
        }
    }
}
