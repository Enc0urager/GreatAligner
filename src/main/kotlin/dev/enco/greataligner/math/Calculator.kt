package dev.enco.greataligner.math

import dev.enco.greataligner.repository.CharRepository
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

object Calculator {
    val cache = Object2IntOpenHashMap<String>()

    fun getStringWidth(text: String?, cacheResult : Boolean): Int {
        if (text.isNullOrEmpty()) return 0

        if (cacheResult) {
            val cached = cache.getOrDefault(text, -1)
            if (cached != -1) return cached
        }

        var width = 0; var bold = false; var i = 0; val len = text.length

        while (i < len) {
            val c = text[i]

            if ((c == '§' || c == '&') && i + 1 < len) {
                when (text[i + 1].lowercaseChar()) {
                    'l' -> bold = true
                    'r', in '0'..'9', in 'a'..'f' -> bold = false
                }
                i += 2
                continue
            }

            val w = CharRepository.get(c)
            width += w + 1 + if (bold) 1 else 0; i++
        }

        if (cacheResult) cache[text] = width
        return width
    }
}