package br.leg.interlegis.saplmobile.sapl.json.sessao_plenaria

import br.leg.interlegis.saplmobile.sapl.json.Pagination
import br.leg.interlegis.saplmobile.sapl.json.sessao_plenaria.SessaoPlenariaResult

class SessaoPlenariaResponse {

    var pagination: Pagination? = null
    var results: List<SessaoPlenariaResult>? = null

}