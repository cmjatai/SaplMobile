package br.leg.interlegis.saplmobile.sapl.support

import android.content.Context
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import br.leg.interlegis.saplmobile.sapl.db.entities.SaplEntity
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.declaredMemberProperties


class Utils {

    companion object {
        fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED.equals(state)
        }

        fun pathname(pathDir: String, relative_pathfile: String) =
                String.format("%s/%s", pathDir, relative_pathfile).replace("//", "/")
    }


}
