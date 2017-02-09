package jp.keita.kagurazaka.rxproperty.sample.todo

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import jp.keita.kagurazaka.rxproperty.Nothing

object TodoRepository {
    val onChanged: Observable<Nothing>
        get() = changeEmitter.observeOn(AndroidSchedulers.mainThread())

    val all: List<TodoItem>
        get() = list

    val active: List<TodoItem>
        get() = list.filter { !it.isDone }

    val done: List<TodoItem>
        get() = list.filter { it.isDone }

    private val list = arrayListOf<TodoItem>()
    private val changeEmitter = PublishSubject.create<Nothing>().toSerialized()

    fun store(item: TodoItem) {
        list.add(item)
        changeEmitter.onNext(Nothing.INSTANCE)
    }

    fun update(item: TodoItem) {
        val index = list.indexOf(item)
        if (index >= 0) {
            list[index] = item
            changeEmitter.onNext(Nothing.INSTANCE)
        }
    }

    fun deleteDone() {
        list.removeAll { it.isDone }
        changeEmitter.onNext(Nothing.INSTANCE)
    }

    fun clear() {
        list.clear()
        changeEmitter.onNext(Nothing.INSTANCE)
    }
}
