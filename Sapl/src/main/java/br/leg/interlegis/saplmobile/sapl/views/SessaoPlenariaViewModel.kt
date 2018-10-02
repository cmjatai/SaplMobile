package br.leg.interlegis.saplmobile.sapl.views

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import br.leg.interlegis.saplmobile.sapl.SaplApplication
import br.leg.interlegis.saplmobile.sapl.db.entities.SessaoPlenaria

class SessaoPlenariaViewModel: ViewModel() {
    var sessoes: LiveData<List<SessaoPlenaria>>? = SaplApplication.sessoesPlenarias
}