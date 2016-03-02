package jp.keita.kagurazaka.rxproperty.sample;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.keita.kagurazaka.rxproperty.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  private ViewModel viewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Switchable!
    viewModel = new JavaViewModel();
    // viewModel = new KotlinViewModel();

    ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    binding.setViewModel(viewModel);
  }

  @Override
  protected void onDestroy() {
    viewModel.unsubscribe();
    super.onDestroy();
  }
}
