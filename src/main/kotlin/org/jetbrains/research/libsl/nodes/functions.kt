package org.jetbrains.research.libsl.nodes

import org.jetbrains.research.libsl.context.LslContextBase
import org.jetbrains.research.libsl.nodes.references.AutomatonReference
import org.jetbrains.research.libsl.nodes.references.TypeReference
import org.jetbrains.research.libsl.utils.BackticksPolitics

data class Function(
    val name: String,
    val automatonReference: AutomatonReference,
    var args: MutableList<FunctionArgument> = mutableListOf(),
    val returnType: TypeReference?,
    var contracts: MutableList<Contract> = mutableListOf(),
    var statements: MutableList<Statement> = mutableListOf(),
    val hasBody: Boolean = statements.isNotEmpty(),
    var targetAutomatonRef: AutomatonReference? = null,
    val context: LslContextBase
) : Node() {
    val fullName: String
        get() = "${automatonReference.name}.$name"
    var resultVariable: Variable? = null

    override fun dumpToString(): String = buildString {
        append("fun ${BackticksPolitics.forIdentifier(name)}")

        append(
            args.joinToString(separator = ", ", prefix = "(", postfix = ")") { arg -> buildString {
                if (arg.annotation != null) {
                    append("@")
                    append(BackticksPolitics.forIdentifier(arg.annotation!!.name))

                    if (arg.annotation!!.values.isNotEmpty()) {
                        append("(")
                        append(arg.annotation!!.values.joinToString(separator = ", ") { v ->
                            v.dumpToString()
                        })
                        append(")")
                    }

                    append(IPrinter.SPACE)
                }
                append(BackticksPolitics.forIdentifier(arg.name))
                append(": ")

                if (arg.annotation != null && arg.annotation is TargetAnnotation) {
                    append(targetAutomatonRef!!.name)
                } else {
                    append(arg.typeReference.resolveOrError().fullName)
                }
            } }
        )

        if (returnType != null) {
            append(": ")
            append(returnType.resolveOrError().fullName)
        }

        if (contracts.isNotEmpty()) {
            appendLine()
            append(withIndent(formatListEmptyLineAtEndIfNeeded(contracts)))
        }

        if (!hasBody && contracts.isEmpty()) {
            appendLine(";")
        } else if (hasBody) {
            if (contracts.isEmpty()) {
                append(IPrinter.SPACE)
            }
            appendLine("{")
            append(withIndent(formatListEmptyLineAtEndIfNeeded(statements)))
            appendLine("}")
        }
    }
}

data class ArgumentWithValue(
    val name: String,
    val value: Expression
) : IPrinter {
    override fun dumpToString(): String = "${BackticksPolitics.forIdentifier(name)} = ${value.dumpToString()}"
}