package jp.keita.kagurazaka.rxproperty.sample.basics;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.keita.kagurazaka.rxproperty.sample.R;
import jp.keita.kagurazaka.rxproperty.sample.databinding.ActivityBasicsBinding;

public class BasicsActivity extends AppCompatActivity {
    private BasicsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Switchable!
        viewModel = new JavaBasicsViewModel();
        // viewModel = new KotlinBasicsViewModel();

        ActivityBasicsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_basics);
        binding.setViewModel(viewModel);
    }

    @Override
    protected void onDestroy() {
        viewModel.dispose();
        super.onDestroy();
    }
}
