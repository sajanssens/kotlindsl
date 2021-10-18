fun main() {

    // ouderwetse manier
    val jm = Magazine("Java Magazine", 2021, 4)
    jm.append("<article>")
    jm.append(" <title>Kotlin DSLs</title>")
    jm.append(" <content>This is a story about DSLs.</content>")
    jm.append("</article>")
    jm.append("<article>")
    //         ...

    // api via method chaining
    val jm2 = Magazine("Java Magazine", 2021, 4)
    jm2.article()
        .title("Kotlin DSLs")
        .content("This is a story about DSLs.")
        .article()
        .title("Java 17")
    println(jm2)

    // lambdas with receivers:
    // old way:
    val sb = StringBuilder()
    sb.append("This is a story about DSLs.")

    // pass normal lambda
    val sb0 =
        buildString { sb ->
            sb.append("This is a story about DSLs.")
        }

    // pass normal lambda using 'it'
    val sb1 =
        buildString {
            it.append("This is a story about DSLs.")
        }

    // pass lambda with receiver: omit 'it'
    val sb2 =
        buildStringClean {
            append("This is a story about DSLs.")
        }

    val dslMag =
        magazine("Java Magazine", 2021, 4) {
            article {
                title { +"Kotlin DSLs" }
                content { +"This is a story about DSLs." }
            }
        }

    println(dslMag)
}

private fun Magazine.article(): Articl {
    return Articl(this)
}

class Articl(val magazine: Magazine) {
    fun title(s: String): Articl {
        magazine.title("<title>$s</title>")
        return this
    }

    fun content(s: String): Magazine {
        magazine.append("<content>$s</content>")
        return magazine
    }

}

fun magazine(name: String, year: Int, nr: Int, content: Magazine.() -> ARTICLE): Magazine {
    val magazine = Magazine(name, year, nr)
    magazine.content = magazine.content().toString()
    return magazine
}

fun buildString(action: (StringBuilder) -> Unit): String {
    val sb = StringBuilder()
    action(sb)
    return sb.toString()
}

fun buildStringClean(action: Magazine.() -> Unit): Magazine {
    val magazine = Magazine("Java Magazine", 2021, 4)
    magazine.action()
    return magazine
}

data class Magazine(val name: String, val year: Int, val nr: Int, var content: String = "")

fun Magazine.append(s: String) {
    this.content += s
}

fun Magazine.title(s: String) {
    this.content += "<title>$s</title>"
}

// DSL grammar ---------------

abstract class Element {
    protected val children = mutableListOf<Element>()

    protected fun <T : Element> initTag(child: T, body: T.() -> Unit) {
        child.body()
        children.add(child)
    }

    abstract fun render(builder: StringBuilder)

}

class Text(val text: String) : Element() {
    override fun render(builder: StringBuilder) {
        builder.append(text)
    }
}

@DslMarker
annotation class MagazineMarker

@MagazineMarker
abstract class Tag(private val name: String) : Element() {

    override fun render(builder: StringBuilder) {
        builder.append("<$name>")
        children.forEach { it.render(builder) }
        builder.append("</$name>")
    }

    operator fun String.unaryPlus() {
        children.add(Text(this))
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder)
        return builder.toString()
    }

}

class ARTICLE : Tag("article") {
    fun title(body: TITLE.() -> Unit) = initTag(TITLE(), body)
    fun content(body: CONTENT.() -> Unit) = initTag(CONTENT(), body)
}

class TITLE : Tag("title") {
    fun text(s: String) = initTag(Text(s), {})
}

class CONTENT : Tag("content")

// root function
fun article(init: ARTICLE.() -> Unit) = ARTICLE().apply(init)
