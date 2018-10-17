package br.leg.interlegis.saplmobile.sapl.support

import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import java.io.InputStream


class Utils {

    companion object {
        fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED.equals(state)
        }
    }


}
