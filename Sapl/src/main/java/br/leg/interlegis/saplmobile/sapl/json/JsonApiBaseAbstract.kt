package br.leg.interlegis.saplmobile.sapl.json

import android.content.Context
import br.leg.interlegis.saplmobile.sapl.json.interfaces.JsonApiInterface
import br.leg.interlegis.saplmobile.sapl.json.interfaces.SaplRetrofitService

abstract class JsonApiBaseAbstract: JsonApiInterface {

    var servico: SaplRetrofitService? = null
    var context: Context? = null

}