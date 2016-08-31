package jp.keita.kagurazaka.rxproperty.sample.todo

import jp.keita.kagurazaka.rxproperty.RxCommand
import jp.keita.kagurazaka.rxproperty.RxProperty
import jp.keita.kagurazaka.rxproperty.sample.BR
import jp.keita.kagurazaka.rxproperty.sample.R
import jp.keita.kagurazaka.rxproperty.sample.ViewModelBase
import jp.keita.kagurazaka.rxproperty.toRxCommand
import me.tatarka.bindingcollectionadapter.ItemView

class TodoViewModel : ViewModelBase() {
    val todoList: TodoList = TodoList()
    val todoListItem: ItemView = ItemView.of(BR.todoListItemVM, R.layout.item_todo)

    val viewModeIndex: RxProperty<Int?> = RxProperty<Int?>(0).asManaged()

    val inputTodoItem: RxProperty<TodoItemViewModel>
            = RxProperty(TodoItemViewModel()).asManaged()

    val addCommand: RxCommand<Void> = inputTodoItem.asObservable()
            .switchMap { it.onHasErrorChanged }
            .map { !it }
            .toRxCommand<Void>(false)
            .asManaged()

    val deleteDoneCommand: RxCommand<Void> = RxCommand()

    init {
        val updateTodoList: (Int) -> Unit = {
            val list = when (it) {
                0 -> TodoRepository.all
                1 -> TodoRepository.active
                2 -> TodoRepository.done
                else -> throw IllegalStateException()
            }
            todoList.replace(list)
        }

        TodoRepository.onChanged
                .subscribe { viewModeIndex.get()?.let(updateTodoList) } // Not smart :(
                .asManaged()

        viewModeIndex.asObservable()
                .filter { it != null }
                .map { it!! }
                .subscribe { updateTodoList(it) }
                .asManaged()

        addCommand.asObservable()
                .subscribe {
                    val current = inputTodoItem.get()
                    TodoRepository.store(current.model)
                    current.unsubscribe()
                    inputTodoItem.set(TodoItemViewModel())
                }.asManaged()

        deleteDoneCommand.asObservable()
                .subscribe {
                    TodoRepository.deleteDone()
                }.asManaged()
    }

    override fun unsubscribe() {
        TodoRepository.clear()
        super.unsubscribe()
    }
}
