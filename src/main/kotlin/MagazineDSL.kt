fun main() {

    // The classic way:
    val jm = Magazine("Java Magazine", 2021, 4)
    jm.append("<article>")
    jm.append(" <title>Kotlin DSLs</title>")
    jm.append(" <content>This is a story about DSLs.</content>")
    jm.append("</article>")
    //         ...
    println(jm)

    // The DSL way:
    val dslJm =
        magazine("Java Magazine", 2021, 4) {
            article {
                title { innerText("Kotlin DSLs") }
                content { +"This is a story about DSLs." } // + instead of innerText()
            }
        }

    println(dslJm)
}

// The domain class ---------------
data class Magazine(val name: String, val year: Int, val nr: Int, var content: String = "") {
    fun append(s: String) {
        this.content += s
    }
}

// builder for magazine to start with ---------------
fun magazine(name: String, year: Int, nr: Int, body: Magazine.() -> ARTICLE): Magazine {
    val magazine = Magazine(name, year, nr)
    magazine.content = magazine.body().toString()
    return magazine
}

// top level function for root element article ---------------
fun article(body: ARTICLE.() -> Unit): ARTICLE {
    val article = ARTICLE()
    article.body()
    return article
    // or shorter equivalent: ARTICLE().apply(init)
}

// The DSL grammar ---------------

abstract class Element {
    abstract fun render(builder: StringBuilder)
}

class Text(val text: String) : Element() {
    override fun render(builder: StringBuilder) {
        builder.append(text)
    }
}

abstract class Tag(private val name: String) : Element() {
    protected val tree = mutableListOf<Element>()

    override fun render(builder: StringBuilder) {
        builder.append("<$name>")
        tree.forEach { it.render(builder) }
        builder.append("</$name>")
    }

    protected fun <T : Element> initElement(child: T, body: T.() -> Unit = {}) {
        child.body()
        tree.add(child)
    }

    // For now, every tag can have text content:
    fun innerText(s: String) {
        initElement(Text(s))
    }

    // Trick: instead of calling text(), to override the unary plus operator;
    // this improves readability of the dsl.
    operator fun String.unaryPlus() {
        initElement(Text(this))
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder)
        return builder.toString()
    }
}

// Available TAGs:

class ARTICLE : Tag("article") {
    fun title(body: TITLE.() -> Unit) {
        val child = TITLE()
        child.body()
        tree.add(child)
        // or: initElement(TITLE(), body)
    }

    fun content(body: CONTENT.() -> Unit) {
        initElement(CONTENT(), body)
    }
}

class TITLE : Tag("title")

class CONTENT : Tag("content")


