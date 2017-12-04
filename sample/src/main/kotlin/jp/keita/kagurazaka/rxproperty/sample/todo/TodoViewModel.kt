package jp.keita.kagurazaka.rxproperty.sample.todo

import jp.keita.kagurazaka.rxproperty.Nothing
import jp.keita.kagurazaka.rxproperty.RxCommand
import jp.keita.kagurazaka.rxproperty.RxProperty
import jp.keita.kagurazaka.rxproperty.sample.BR
import jp.keita.kagurazaka.rxproperty.sample.R
import jp.keita.kagurazaka.rxproperty.sample.ViewModelBase
import jp.keita.kagurazaka.rxproperty.toRxCommand
import me.tatarka.bindingcollectionadapter2.ItemBinding

class TodoViewModel : ViewModelBase() {
    val todoList: TodoList = TodoList()
    val todoListItem: ItemBinding<TodoItemViewModel> = ItemBinding.of(BR.todoListItemVM, R.layout.item_todo)

    val viewModeIndex: RxProperty<Int?> = RxProperty<Int?>(0).asManaged()

    val inputTodoItem: RxProperty<TodoItemViewModel>
            = RxProperty(TodoItemViewModel()).asManaged()

    val addCommand: RxCommand<Nothing> = inputTodoItem
            .switchMap { it.onHasErrorChanged }
            .map { !it }
            .toRxCommand<Nothing>(false)
            .asManaged()

    val deleteDoneCommand: RxCommand<Any> = RxCommand()

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

        viewModeIndex.filter { it != null }
                .map { it!! }
                .subscribe { updateTodoList(it) }
                .asManaged()

        addCommand.subscribe {
            inputTodoItem.get()?.let {
                TodoRepository.store(it.model)
                it.dispose()
                inputTodoItem.set(TodoItemViewModel())
            }
        }.asManaged()

        deleteDoneCommand.subscribe {
            TodoRepository.deleteDone()
        }.asManaged()
    }

    override fun dispose() {
        TodoRepository.clear()
        super.dispose()
    }
}
