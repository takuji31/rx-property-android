package jp.keita.kagurazaka.rxproperty.sample.basics

import jp.keita.kagurazaka.rxproperty.*
import jp.keita.kagurazaka.rxproperty.Nothing

class KotlinBasicsViewModel : BasicsViewModel() {
    override val input: RxProperty<String> = RxProperty("")
            .setValidator { if (it.isEmpty()) "Text must not be empty!" else null }
            .asManaged()

    override val output: ReadOnlyRxProperty<String> = input
            .map(String::toUpperCase)
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
