package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface

abstract class JsonApiBaseAbstract: JsonApiInterface {

    var context: Context? = null

}