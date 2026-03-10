package dev.enco.modernleaderboards.repository

import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap

object CharRepository {
    val char2PixelMap = Char2IntOpenHashMap().apply {
        defaultReturnValue(5)
        fun set(chars: String, width: Int) {
            for (c in chars) put(c, width)
        }
        set("!.,i:;|", 1)
        set("l'", 2)
        set("tI[](){}", 3)
        set("fk\"* ", 4)
        set("abcdeghjmnoprstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ0123456789#$%/^&_=`~?<>", 5)
        set("@~MW", 6)
        set("ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡʏᴢ", 5)
    }
    fun setCharsWidth(chars: String, width: Int) { for (c in chars) char2PixelMap.put(c, width) }
    fun add(c: Char, i : Int) = char2PixelMap.put(c, i)
    fun get(c: Char): Int = char2PixelMap.get(c)
}