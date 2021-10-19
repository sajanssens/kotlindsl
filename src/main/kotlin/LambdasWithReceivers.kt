fun main() {

    // Build a string...

    // The classic way:
    val sb = StringBuilder()
    sb.append("This is a story about DSLs.")

    // Using a builder function:
    // pass normal lambda
    val sb0 = buildString { sb ->
        sb.append("This is a story about DSLs.")
        sb.reverse()
    }

    // pass normal lambda using 'it'
    val sb1 = buildString {
        it.append("This is a story about DSLs.")
        it.reverse()
    }

    // pass lambda with receiver: you can omit 'it'
    val sb2 = buildStringClean {
        append("This is a story about DSLs.")
        reverse()
    }

}

fun buildString(action: (StringBuilder) -> Unit): String { // action has sb as param
    val sb = StringBuilder()
    action(sb) // call lambda WITH sb as param
    return sb.toString()
}

fun buildStringClean(action: StringBuilder.() -> Unit): String { // action has sb as receiver
    val sb = StringBuilder()
    sb.action() // call lambda ON sb
    return sb.toString()
}
