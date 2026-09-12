package com.hrhousing.app.util

/** Replaces `{token}` placeholders in an editable template with values from [tokens]. */
object TemplateEngine {
    fun render(template: String, tokens: Map<String, String>): String {
        var result = template
        for ((key, value) in tokens) {
            result = result.replace("{$key}", value)
        }
        return result
    }
}
