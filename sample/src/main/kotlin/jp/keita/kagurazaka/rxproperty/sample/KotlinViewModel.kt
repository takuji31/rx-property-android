package jp.keita.kagurazaka.rxproperty.sample

import android.view.View
import jp.keita.kagurazaka.rxproperty.RxProperty
import jp.keita.kagurazaka.rxproperty.toRxCommand
import jp.keita.kagurazaka.rxproperty.toRxProperty

class KotlinViewModel : ViewModel() {
    override val input = RxProperty("").asManaged()

    override val output = input.asObservable()
            .map { it?.toUpperCase() ?: "" }
            .toRxProperty()
            .asManaged()

    override val command = output.asObservable()
            .map { !it.isNullOrEmpty() }
            .toRxCommand(View.OnClickListener {
                input.value = "clicked!"
            })
            .asManaged()
}
