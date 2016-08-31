package jp.keita.kagurazaka.rxproperty.util;

import android.databinding.Observable;

import org.junit.rules.ExternalResource;
import org.mockito.Mockito;

import java.util.List;

import jp.keita.kagurazaka.rxproperty.RxProperty;
import rx.functions.Action0;
import rx.observers.TestSubscriber;
import rx.subscriptions.CompositeSubscription;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class RxPropertyTestRule<T> extends ExternalResource {
    private RxProperty<T> property;
    private TestSubscriber<T> valueSubscriber;
    private TestSubscriber<List<String>> errorMessagesSubscriber;
    private TestSubscriber<String> summarizedErrorMessageSubscriber;
    private TestSubscriber<Boolean> hasErrorsSubscriber;
    private Observable.OnPropertyChangedCallback viewObserver;
    private CompositeSubscription subscriptions;

    public TestSubscriber<T> getValueSubscriber() {
        return valueSubscriber;
    }

    public TestSubscriber<List<String>> getErrorMessagesSubscriber() {
        return errorMessagesSubscriber;
    }

    public TestSubscriber<String> getSummarizedErrorMessageSubscriber() {
        return summarizedErrorMessageSubscriber;
    }

    public TestSubscriber<Boolean> getHasErrorsSubscriber() {
        return hasErrorsSubscriber;
    }

    @Override
    protected void before() {
        valueSubscriber = new TestSubscriber<>();
        errorMessagesSubscriber = new TestSubscriber<>();
        summarizedErrorMessageSubscriber = new TestSubscriber<>();
        hasErrorsSubscriber = new TestSubscriber<>();
        viewObserver = Mockito.mock(Observable.OnPropertyChangedCallback.class);
        subscriptions = new CompositeSubscription();
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void after() {
        if (property != null) {
            assertFalse(property.isUnsubscribed());
            property.unsubscribe();
            assertTrue(property.isUnsubscribed());
        }

        subscriptions.unsubscribe();
        valueSubscriber = null;
        errorMessagesSubscriber = null;
        summarizedErrorMessageSubscriber = null;
        hasErrorsSubscriber = null;
        viewObserver = null;
    }

    @SuppressWarnings("deprecation")
    public void setUp(final RxProperty<T> property) {
        this.property = property;
        subscriptions.add(this.property.asObservable().subscribe(valueSubscriber));
        subscriptions.add(this.property.onErrorsChanged().subscribe(errorMessagesSubscriber));
        subscriptions.add(this.property.onSummarizedErrorChanged().subscribe(summarizedErrorMessageSubscriber));
        subscriptions.add(this.property.onHasErrorsChanged().subscribe(hasErrorsSubscriber));
        this.property.getValue().addOnPropertyChangedCallback(viewObserver);
        this.property.setUnbindView(new Action0() {
            @Override
            public void call() {
                property.getValue().removeOnPropertyChangedCallback(viewObserver);
            }
        });
    }

    public void assertPropertyValueIs(final T value) {
        if (value == null) {
            assertNull(property.get());
        } else {
            assertThat(property.get(), is(value));
        }
    }

    public void assertPropertyHasNoErrors() {
        assertFalse(property.hasErrors());
        assertThat(property.getSummarizedErrorMessage(), is((String) null));
    }

    public void assertErrorMessagesIs(final List<String> messages) {
        assertNotNull(messages);
        assertTrue(property.hasErrors());
        assertThat(property.getErrorMessages(), is(messages));
    }

    public void assertSummarizedErrorMessageIs(final String message) {
        assertNotNull(message);
        assertTrue(property.hasErrors());
        assertThat(property.getSummarizedErrorMessage(), is(message));
    }

    public void assertViewNotificationCountIs(int times) {
        Mockito.verify(viewObserver, Mockito.times(times))
                .onPropertyChanged(Mockito.<Observable>any(), Mockito.anyInt());
    }
}
