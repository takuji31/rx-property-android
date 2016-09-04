package jp.keita.kagurazaka.rxproperty.sample.todo

import java.util.*

class TodoItem(var isDone: Boolean, var title: String) {
    val id: String = UUID.randomUUID().toString().toUpperCase()

    override fun equals(other: Any?) = when (other) {
        is TodoItem -> id == other.id
        else -> false
    }

    override fun hashCode() = 31 * id.hashCode()
}
