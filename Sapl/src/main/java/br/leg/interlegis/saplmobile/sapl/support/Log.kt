package br.leg.interlegis.saplmobile.sapl.support

object Log {
    internal val LOG = true

    fun i(tag: String, string: String) {
        if (LOG) android.util.Log.i(tag, string)
    }

    fun e(tag: String, string: String) {
        if (LOG) android.util.Log.e(tag, string)
    }

    fun d(tag: String, string: String) {
        if (LOG) android.util.Log.d(tag, string)
    }

    fun v(tag: String, string: String) {
        if (LOG) android.util.Log.v(tag, string)
    }

    fun w(tag: String, string: String) {
        if (LOG) android.util.Log.w(tag, string)
    }
}