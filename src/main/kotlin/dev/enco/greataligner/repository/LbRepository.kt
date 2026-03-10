package dev.enco.greataligner.repository

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

object LbRepository {
    val lbPixels = Object2IntOpenHashMap<String>()
    fun setDefaultReturnValue(width: Int) = lbPixels.defaultReturnValue(width)
    fun get(lb : String) : Int = lbPixels.getInt(lb)
    fun put(lb : String, value : Int) = lbPixels.put(lb, value)
}