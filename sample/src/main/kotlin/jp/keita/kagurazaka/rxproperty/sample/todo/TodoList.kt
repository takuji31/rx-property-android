package jp.keita.kagurazaka.rxproperty.sample.todo

import android.databinding.ObservableArrayList

class TodoList : ObservableArrayList<TodoItemViewModel>() {
    fun replace(list: List<TodoItem>) {
        clear()
        addAll(list.map { TodoItemViewModel(it) })
    }

    override fun clear() {
        forEach { it.unsubscribe() }
        super.clear()
    }
}
