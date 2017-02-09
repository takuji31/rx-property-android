package jp.keita.kagurazaka.rxproperty.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import jp.keita.kagurazaka.rxproperty.RxProperty;
import jp.keita.kagurazaka.rxproperty.internal.Helper;

public class RxPropertyErrorObserver<T> implements Disposable {
    private RxProperty<T> property;
    private TestObserver<List<String>> errorsObserver;
    private TestObserver<String> summarizedErrorObserver;
    private TestObserver<Boolean> hasErrorsObserver;
    private AtomicBoolean isDisposed = new AtomicBoolean(false);

    public RxPropertyErrorObserver(RxProperty<T> property) {
        this.property = property;
        errorsObserver = property.onErrorsChanged().test();
        summarizedErrorObserver = property.onSummarizedErrorChanged().test();
        hasErrorsObserver = property.onHasErrorsChanged().test();
    }

    @SafeVarargs
    public final RxPropertyErrorObserver<T> assertErrors(List<String>... values) {
        errorsObserver.assertSubscribed()
                .assertValues(values)
                .assertNoErrors();
        return this;
    }

    public RxPropertyErrorObserver<T> assertErrorsCount(int count) {
        errorsObserver.assertValueCount(count);
        return this;
    }

    public RxPropertyErrorObserver<T> assertNoErrors() {
        errorsObserver.assertNoValues();
        return this;
    }

    public RxPropertyErrorObserver<T> assertSummarizedErrors(String... values) {
        summarizedErrorObserver.assertSubscribed()
                .assertValues(values)
                .assertNoErrors();
        return this;
    }

    public RxPropertyErrorObserver<T> assertSummarizedErrorsCount(int count) {
        summarizedErrorObserver.assertValueCount(count);
        return this;
    }

    public RxPropertyErrorObserver<T> assertNoSummarizedErrors() {
        summarizedErrorObserver.assertNoValues();
        return this;
    }

    public RxPropertyErrorObserver<T> assertHasErrors(Boolean... values) {
        hasErrorsObserver.assertSubscribed()
                .assertValues(values)
                .assertNoErrors();
        return this;
    }

    public RxPropertyErrorObserver<T> assertHasErrorsCount(int count) {
        hasErrorsObserver.assertValueCount(count);
        return this;
    }

    public RxPropertyErrorObserver<T> assertNoHasErrors() {
        hasErrorsObserver.assertNoValues();
        return this;
    }

    public RxPropertyErrorObserver<T> assertLatestErrors(String... values) {
        List<String> errors = property.getErrorMessages();
        if (errors.size() != values.length) {
            fail("Value count differs; Expected: " + values.length + " " + Arrays.toString(values)
                    + ", Actual: " + errors.size() + " " + errors);
        }

        for (int i = 0; i < errors.size(); ++i) {
            String a = errors.get(i);
            String b = values[i];
            if (!Helper.compare(a, b)) {
                fail("Values at position " + i + " differ; Expected: " + a + ", Actual: " + b);
            }
        }

        return this;
    }

    public RxPropertyErrorObserver<T> assertLatestSummarizedError(String value) {
        String message = property.getSummarizedErrorMessage();
        if (!Helper.compare(value, message)) {
            fail("Value differs; Expected: " + value + ", Actual: " + message);
        }
        return this;
    }

    public RxPropertyErrorObserver<T> assertLatestHasErrors(boolean value) {
        boolean hasErrors = property.hasErrors();
        if (value != hasErrors) {
            fail("Value differs; Expected: " + value + ", Actual: " + hasErrors);
        }
        return this;
    }

    public RxPropertyErrorObserver<T> assertComplete() {
        errorsObserver.assertComplete();
        summarizedErrorObserver.assertComplete();
        hasErrorsObserver.assertComplete();
        return this;
    }

    public RxPropertyErrorObserver<T> assertNotComplete() {
        errorsObserver.assertNotComplete();
        summarizedErrorObserver.assertNotComplete();
        hasErrorsObserver.assertNotComplete();
        return this;
    }

    @Override
    public void dispose() {
        if (isDisposed.compareAndSet(false, true)) {
            errorsObserver.dispose();
            summarizedErrorObserver.dispose();
            hasErrorsObserver.dispose();
            property = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed.get();
    }

    private void fail(String message) {
        throw new AssertionError(message);
    }
}
