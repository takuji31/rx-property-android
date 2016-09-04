package jp.keita.kagurazaka.rxproperty.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jakewharton.rxbinding.view.RxView;

import jp.keita.kagurazaka.rxproperty.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new MainViewModel(this);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // You can bind trigger observable instead of using "rxCommandOnClick" on layout xml.
        viewModel.goToTodoCommand.bindTrigger(RxView.clicks(binding.buttonGoToTodo));

        binding.setViewModel(viewModel);
    }

    @Override
    protected void onDestroy() {
        viewModel.unsubscribe();
        super.onDestroy();
    }
}
