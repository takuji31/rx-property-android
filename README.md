# RxProperty Android

Bindable and observable property for Android Data Binding

This library is an Android port of [ReactiveProperty](https://github.com/runceel/ReactiveProperty).

![Demo](images/demo.gif)

## Usage

First, declare a view model class.

```java
public class ViewModel {
  public final RxProperty<String> input;
  public final ReadOnlyRxProperty<String> output;
  public final RxCommand<View.OnClickListener> command;

  public JavaViewModel() {
    input = new RxProperty<>("");

    output = new RxProperty<>(
        input.asObservable()
            .map(it -> it == null ? "" : it.toUpperCase())
    );

    command = new RxCommand<>(
        output.asObservable()
            .map(it -> !TextUtils.isEmpty(it)),
        view -> { input.setValue("clicked"); }
    );
  }
```

Next, write a layout XML to bind the view model.

```xml
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <data>
        <variable name="viewModel" type="ViewModel" />
    </data>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <EditText
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="text"
            android:text="@{viewModel.input.get}"
            app:text="@{viewModel.input.set}" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="@{viewModel.output.get}" />

        <Button
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:enabled="@{viewModel.command.enabled}"
            android:onClickListener="@{viewModel.command.exec}"
            android:text="Is not empty?" />
    </LinearLayout>
</layout>
```

Finally, execute data binding in your activity.

```java
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    binding.setViewModel(new ViewModel());
  }

```

YouÅfre done!

```
Important Note: These snippets skips resource management/error handling for simplicity.

```

## Kotlin Support

There are some useful extension methods in `rx-property-kotlin'.

```kotlin
class ViewModel {
    val input = RxProperty("")

    val output = input.asObservable()
            .map { it?.toUpperCase() ?: "" }
            .toRxProperty()

    val command = output.asObservable()
            .map { !it.isNullOrEmpty() }
            .toRxCommand(View.OnClickListener { input.value = "clicked!" })
}
```

## License

    The MIT License (MIT)

    Copyright (c) 2016 Keita Kagurazaka

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
