package org.folg.gedcom.parser

class ValueSetter(
    private val getter: (() -> String?)? = null,
    private val setter: ((String) -> Unit)? = null,
    private val adder: ((String) -> Unit)? = null
) {
    var value: String?
        get() = getter?.invoke()
        set(value) {
            value?.let { setter?.invoke(it) }
        }

    fun appendValue(newValue: String) {
        if (adder != null) {
            adder.invoke(newValue)
        } else if (setter != null && getter != null) {
            val current = getter.invoke() ?: ""
            setter.invoke(current + newValue)
        } else if (setter != null) {
             // If we only have setter, we can't really "append" unless we track state here.
             // But ModelParser is stateful.
             // However, usually we can provide a getter.
             setter.invoke(newValue) // Fallback: overwrite
        }
    }
}
