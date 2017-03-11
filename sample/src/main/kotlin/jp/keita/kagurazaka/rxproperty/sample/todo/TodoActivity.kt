package jp.keita.kagurazaka.rxproperty.sample.todo

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.jakewharton.rxbinding2.view.RxMenuItem
import jp.keita.kagurazaka.rxproperty.sample.BR
import jp.keita.kagurazaka.rxproperty.sample.R
import jp.keita.kagurazaka.rxproperty.sample.databinding.ActivityTodoBinding

class TodoActivity : AppCompatActivity() {
    private lateinit var viewModel: TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = TodoViewModel()
        val binding = DataBindingUtil.setContentView<ActivityTodoBinding>(this, R.layout.activity_todo)
        binding.setVariable(BR.todoVM, viewModel)
    }

    override fun onDestroy() {
        viewModel.dispose()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_todo, menu)
        viewModel.deleteDoneCommand.bindTrigger(RxMenuItem.clicks(menu.findItem(R.id.clear_done)))
        return true
    }
}
