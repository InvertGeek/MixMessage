package com.donut.mixfile.server.core.routes.api.webdav.objects


class XmlBuilder(private val name: String) {
    private val children = mutableListOf<XmlBuilder>()
    private val attributes = mutableMapOf<String, String>()
    private var text: String? = null
    var xmlns: String? = null
        set(value) {
            field = value
            attributes["xmlns"] = value ?: return
        }

    operator fun String.invoke(block: XmlBuilder.() -> Unit): XmlBuilder {
        val child = XmlBuilder(this).apply(block)
        children.add(child)
        return child
    }

    operator fun String.unaryMinus() {
        text = this
    }

    fun attribute(name: String, value: Any) {
        attributes[name] = value.toString()
    }

    private fun String.encodeXml(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    override fun toString(): String = buildString {
        toString(this, 0)
    }

    private fun toString(sb: StringBuilder, level: Int) {
        sb.append("  ".repeat(level))
        sb.append("<$name")

        attributes.forEach { (k, v) ->
            sb.append(" $k=\"${v.encodeXml()}\"")
        }

        when {
            text != null && children.isEmpty() -> {
                sb.append(">${text?.encodeXml()}</$name>\n")
            }

            children.isNotEmpty() -> {
                sb.append(">\n")
                children.forEach { it.toString(sb, level + 1) }
                sb.append("  ".repeat(level))
                sb.append("</$name>\n")
            }

            else -> {
                sb.append("/>\n")
            }
        }
    }
}

fun xml(name: String, block: XmlBuilder.() -> Unit): XmlBuilder =
    XmlBuilder(name).apply(block)

