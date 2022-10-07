package org.jetbrains.research.libsl.errors

class ErrorManager {
    val errors: MutableList<LslError> = mutableListOf()

    fun addError(error: LslError) {
        errors.add(error)
    }

    operator fun invoke(error: LslError) {
        errors.add(error)
    }
}