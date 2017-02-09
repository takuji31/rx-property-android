package jp.keita.kagurazaka.rxproperty.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import io.reactivex.functions.Cancellable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import jp.keita.kagurazaka.rxproperty.Nothing;
import jp.keita.kagurazaka.rxproperty.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new MainViewModel(this);
        final ActivityMainBinding binding
                = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // You can bind trigger observable instead of using "rxCommandOnClick" on layout xml.
        final Button goToTodoButton = binding.buttonGoToTodo;
        final Subject<Nothing> emitter = PublishSubject.create();
        goToTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emitter.onNext(Nothing.INSTANCE);
            }
        });
        viewModel.goToTodoCommand.bindTrigger(emitter);
        viewModel.goToTodoCommand.setCancellable(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                goToTodoButton.setOnClickListener(null);
            }
        });

        binding.setViewModel(viewModel);
    }

    @Override
    protected void onDestroy() {
        viewModel.dispose();
        super.onDestroy();
    }
}
