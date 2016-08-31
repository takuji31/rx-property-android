package jp.keita.kagurazaka.rxproperty.sample.basics

import jp.keita.kagurazaka.rxproperty.ReadOnlyRxProperty
import jp.keita.kagurazaka.rxproperty.RxCommand
import jp.keita.kagurazaka.rxproperty.RxProperty
import jp.keita.kagurazaka.rxproperty.toRxCommand
import jp.keita.kagurazaka.rxproperty.toRxProperty

class KotlinBasicsViewModel : BasicsViewModel() {
    override val input: RxProperty<String> = RxProperty("")
            .setValidator({ if (it.isNullOrEmpty()) "Text must not be empty!" else null }, false)
            .asManaged()

    override val output: ReadOnlyRxProperty<String> = input.asObservable()
            .map { it?.toUpperCase() ?: "" }
            .toRxProperty()
            .asManaged()

    override val command: RxCommand<Void> = input.onHasErrorsChanged()
            .map { it -> !it }
            .skip(1)
            .toRxCommand<Void>(false)
            .asManaged()

    init {
        command.asObservable().subscribe {
            input.set("clicked!")
        }.asManaged()
    }
}
