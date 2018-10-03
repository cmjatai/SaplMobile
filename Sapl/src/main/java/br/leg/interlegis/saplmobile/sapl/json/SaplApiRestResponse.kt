package br.leg.interlegis.saplmobile.sapl.json

import br.leg.interlegis.saplmobile.sapl.json.Pagination
import com.google.gson.JsonObject

class SaplApiRestResponse {

    var pagination: Pagination? = null
    var results: List<JsonObject>? = null
    var deleted: IntArray? = null

}