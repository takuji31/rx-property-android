package jp.keita.kagurazaka.rxproperty.sample.basics

import jp.keita.kagurazaka.rxproperty.NoParameter
import jp.keita.kagurazaka.rxproperty.ReadOnlyRxProperty
import jp.keita.kagurazaka.rxproperty.RxCommand
import jp.keita.kagurazaka.rxproperty.RxProperty
import jp.keita.kagurazaka.rxproperty.sample.ViewModelBase

abstract class BasicsViewModel : ViewModelBase() {
    abstract val input: RxProperty<String>
    abstract val output: ReadOnlyRxProperty<String>
    abstract val command: RxCommand<NoParameter>
}
