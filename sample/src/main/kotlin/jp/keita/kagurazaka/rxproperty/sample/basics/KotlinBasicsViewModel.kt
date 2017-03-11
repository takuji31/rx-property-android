package jp.keita.kagurazaka.rxproperty.sample.basics

import jp.keita.kagurazaka.rxproperty.Nothing
import jp.keita.kagurazaka.rxproperty.ReadOnlyRxProperty
import jp.keita.kagurazaka.rxproperty.RxCommand
import jp.keita.kagurazaka.rxproperty.RxProperty
import jp.keita.kagurazaka.rxproperty.toReadOnlyRxProperty
import jp.keita.kagurazaka.rxproperty.toRxCommand

class KotlinBasicsViewModel : BasicsViewModel() {
    override val input: RxProperty<String> = RxProperty("")
            .setValidator { if (it.isNullOrEmpty()) "Text must not be empty!" else null }
            .asManaged()

    override val output: ReadOnlyRxProperty<String> = input
            .map { it?.toUpperCase() ?: "" }
            .toReadOnlyRxProperty()
            .asManaged()

    override val command: RxCommand<Nothing> = input.onHasErrorsChanged()
            .map { it -> !it }
            .toRxCommand<Nothing>(false)
            .asManaged()

    init {
        command.subscribe { input.set("clicked!") }.asManaged()
    }
}
