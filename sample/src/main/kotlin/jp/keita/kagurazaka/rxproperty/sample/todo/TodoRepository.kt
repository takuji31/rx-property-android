package jp.keita.kagurazaka.rxproperty.sample.todo

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject

object TodoRepository {
    val onChanged: Observable<Unit>
        get() = changeEmitter.asObservable().observeOn(AndroidSchedulers.mainThread())

    val all: List<TodoItem>
        get() = list

    val active: List<TodoItem>
        get() = list.filter { !it.isDone }

    val done: List<TodoItem>
        get() = list.filter { it.isDone }

    private val list = arrayListOf<TodoItem>()
    private val changeEmitter = PublishSubject.create<Unit>().toSerialized()

    fun store(item: TodoItem) {
        list.add(item)
        changeEmitter.onNext(Unit)
    }

    fun update(item: TodoItem) {
        val index = list.indexOf(item)
        if (index >= 0) {
            list[index] = item
            changeEmitter.onNext(Unit)
        }
    }

    fun deleteDone() {
        list.removeAll { it.isDone }
        changeEmitter.onNext(Unit)
    }

    fun clear() {
        list.clear()
        changeEmitter.onNext(Unit)
    }
}
