package jp.keita.kagurazaka.rxproperty;

import android.databinding.Observable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.EnumSet;

import rx.Subscription;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RxPropertyTest {

  private TestSubscriber<String> testSubscriber;

  @Before
  public void setUp() throws Exception {
    testSubscriber = new TestSubscriber<>();
  }

  @After
  public void tearDown() throws Exception {
    testSubscriber = null;
  }

  @Test
  public void constructWithoutArguments() {
    RxProperty<String> property = new RxProperty<>();
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);

    subscription.unsubscribe();
  }

  @Test
  public void constructWithInitialValue() {
    RxProperty<String> property = new RxProperty<>("RxProperty");
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is("RxProperty"));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue("RxProperty");

    subscription.unsubscribe();
  }

  @Test
  public void constructWithModeEmpty() {
    RxProperty<String> property = new RxProperty<>(EnumSet.noneOf(RxProperty.Mode.class));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertNoValues();

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    subscription.unsubscribe();
  }

  @Test
  public void constructWithModeNone() {
    RxProperty<String> property = new RxProperty<>(EnumSet.of(RxProperty.Mode.NONE));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertNoValues();

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    subscription.unsubscribe();
  }

  @Test
  public void constructWithModeNoneAndDistinctUntilChanged() {
    RxProperty<String> property = new RxProperty<>(
        EnumSet.of(RxProperty.Mode.NONE, RxProperty.Mode.DISTINCT_UNTIL_CHANGED));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertNoValues();

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    subscription.unsubscribe();
  }

  @Test
  public void constructWithModeNoneAndRaiseLatestValueOnSubscribe() {
    RxProperty<String> property = new RxProperty<>(
        EnumSet.of(RxProperty.Mode.NONE, RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertNoValues();

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    subscription.unsubscribe();
  }

  @Test
  public void constructWithModeAll() {
    RxProperty<String> property = new RxProperty<>(EnumSet.allOf(RxProperty.Mode.class));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertNoValues();

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    subscription.unsubscribe();
  }

  @Test
  public void constructWithModeDistinctUntilChanged() {
    RxProperty<String> property = new RxProperty<>(
        EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertNoValues();

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertNoValues();

    subscription.unsubscribe();
  }

  @Test
  public void constructWithModeRaiseLatestValueOnSubscribe() {
    RxProperty<String> property = new RxProperty<>(
        EnumSet.of(RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(2);
    testSubscriber.assertValues(null, null);

    subscription.unsubscribe();
  }

  @Test
  public void constructWithModeDistinctUntilChangedAndRaiseLatestValueOnSubscribe() {
    RxProperty<String> property = new RxProperty<>(
        EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED,
                   RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    property.setValue(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);

    subscription.unsubscribe();
  }

  @Test
  public void constructWithInitialValueAndMode() {
    RxProperty<String> property = new RxProperty<>(
        "RxProperty", EnumSet.of(RxProperty.Mode.DISTINCT_UNTIL_CHANGED));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is("RxProperty"));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertNoValues();

    property.setValue("Changed");

    assertThat(property.getValue(), is("Changed"));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue("Changed");

    subscription.unsubscribe();
  }

  @Test
  public void constructFromObservable() {
    PublishSubject<String> subject = PublishSubject.create();
    RxProperty<String> property = new RxProperty<>(subject.asObservable());
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    subject.onNext(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);

    subject.onNext("RxProperty");

    assertThat(property.getValue(), is("RxProperty"));
    testSubscriber.assertValueCount(2);
    testSubscriber.assertValues(null, "RxProperty");

    subscription.unsubscribe();

    assertTrue(!property.isUnsubscribed());
    property.unsubscribe();
    assertTrue(property.isUnsubscribed());
  }

  @Test
  public void constructFromObservableWithInitialValue() {
    PublishSubject<String> subject = PublishSubject.create();
    RxProperty<String> property = new RxProperty<>(subject.asObservable(), "RxProperty");
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is("RxProperty"));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue("RxProperty");

    subject.onNext("RxProperty");

    assertThat(property.getValue(), is("RxProperty"));
    testSubscriber.assertValueCount(1);

    subject.onNext("Change");

    assertThat(property.getValue(), is("Change"));
    testSubscriber.assertValueCount(2);
    testSubscriber.assertValues("RxProperty", "Change");

    subscription.unsubscribe();

    assertTrue(!property.isUnsubscribed());
    property.unsubscribe();
    assertTrue(property.isUnsubscribed());
  }

  @Test
  public void constructFromObservableWithMode() {
    PublishSubject<String> subject = PublishSubject.create();
    RxProperty<String> property = new RxProperty<>(
        subject.asObservable(), EnumSet.of(RxProperty.Mode.NONE));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertNoValues();

    subject.onNext(null);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    subscription.unsubscribe();

    assertTrue(!property.isUnsubscribed());
    property.unsubscribe();
    assertTrue(property.isUnsubscribed());
  }

  @Test
  public void constructFromObservableWithInitialValueAndMode() {
    PublishSubject<String> subject = PublishSubject.create();
    RxProperty<String> property = new RxProperty<>(
        subject.asObservable(),
        "RxProperty",
        EnumSet.of(RxProperty.Mode.RAISE_LATEST_VALUE_ON_SUBSCRIBE));
    Subscription subscription = property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is("RxProperty"));
    testSubscriber.assertValueCount(1);

    subject.onNext("RxProperty");

    assertThat(property.getValue(), is("RxProperty"));
    testSubscriber.assertValueCount(2);
    testSubscriber.assertValues("RxProperty", "RxProperty");

    subscription.unsubscribe();

    assertTrue(!property.isUnsubscribed());
    property.unsubscribe();
    assertTrue(property.isUnsubscribed());
  }

  @Test
  public void setValueAsViewNotifyToObserversButNotView() {
    RxProperty<String> property = new RxProperty<>("RxProperty");
    Subscription subscription = property.asObservable().subscribe(testSubscriber);
    Observable.OnPropertyChangedCallback mock
        = Mockito.mock(Observable.OnPropertyChangedCallback.class);
    property.getGet().addOnPropertyChangedCallback(mock);

    assertThat(property.getValue(), is("RxProperty"));
    assertTrue(property.isUnsubscribed());
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue("RxProperty");

    property.setValueAsView("Change");

    testSubscriber.assertValueCount(2);
    testSubscriber.assertValues("RxProperty", "Change");
    Mockito.verify(mock, Mockito.never())
        .onPropertyChanged(Mockito.<Observable>any(), Mockito.anyInt());

    property.setValue("Change");

    testSubscriber.assertValueCount(2);
    Mockito.verify(mock, Mockito.never())
        .onPropertyChanged(Mockito.<Observable>any(), Mockito.anyInt());

    property.setValue("RxProperty");

    testSubscriber.assertValueCount(3);
    testSubscriber.assertValues("RxProperty", "Change", "RxProperty");
    Mockito.verify(mock).onPropertyChanged(Mockito.<Observable>any(), Mockito.anyInt());

    subscription.unsubscribe();
    property.getGet().removeOnPropertyChangedCallback(mock);
  }

  @Test
  public void propagateWhenSourceEmitError() {
    PublishSubject<String> subject = PublishSubject.create();
    RxProperty<String> property = new RxProperty<>(subject.asObservable());
    property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    testSubscriber.assertNoErrors();
    Throwable error = new IllegalStateException();
    subject.onError(error);
    testSubscriber.assertError(error);
  }

  @Test
  public void propagateWhenSourceEmitOnComplete() {
    PublishSubject<String> subject = PublishSubject.create();
    RxProperty<String> property = new RxProperty<>(subject.asObservable());
    property.asObservable().subscribe(testSubscriber);

    assertThat(property.getValue(), is((String) null));
    testSubscriber.assertValueCount(1);
    testSubscriber.assertValue(null);

    testSubscriber.assertNotCompleted();
    subject.onCompleted();
    testSubscriber.assertCompleted();
  }
}
