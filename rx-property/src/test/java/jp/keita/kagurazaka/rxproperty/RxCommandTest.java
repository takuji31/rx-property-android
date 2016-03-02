package jp.keita.kagurazaka.rxproperty;

import android.databinding.Observable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class RxCommandTest {
  interface TestListener {
    void call(String str);
  }

  @Mock
  private TestListener mockListener;
  @Mock
  private Observable.OnPropertyChangedCallback mockPropertyChanged;
  private PublishSubject<Boolean> subject;
  private TestSubscriber<Throwable> testSubscriber;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    subject = PublishSubject.create();
    testSubscriber = new TestSubscriber<>();
  }

  @After
  public void tearDown() throws Exception {
    mockListener = null;
    mockPropertyChanged = null;
    subject = null;
    testSubscriber = null;
  }

  @Test
  public void constructWithoutArguments() {
    RxCommand<TestListener> command = new RxCommand<>();
    command.observeErrors().subscribe(testSubscriber);
    command.getEnabled().addOnPropertyChangedCallback(mockPropertyChanged);

    assertTrue(command.canExecute());
    assertTrue(command.isUnsubscribed());
    assertThat(command.getExec(), is((TestListener) null));
    testSubscriber.assertNoValues();

    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    command.getEnabled().removeOnPropertyChangedCallback(mockPropertyChanged);
  }

  @Test
  public void constructFromSource() {
    RxCommand<TestListener> command = new RxCommand<>(subject.asObservable());
    command.observeErrors().subscribe(testSubscriber);
    command.getEnabled().addOnPropertyChangedCallback(mockPropertyChanged);

    assertTrue(command.canExecute());
    assertThat(command.getExec(), is((TestListener) null));
    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    subject.onNext(true);

    assertTrue(command.canExecute());
    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    subject.onNext(false);

    assertTrue(!command.canExecute());
    verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    assertTrue(!command.isUnsubscribed());
    command.unsubscribe();
    assertTrue(command.isUnsubscribed());

    testSubscriber.assertNoValues();

    command.getEnabled().removeOnPropertyChangedCallback(mockPropertyChanged);
  }

  @Test
  public void constructFromSourceWithInitialValue() {
    RxCommand<TestListener> command = new RxCommand<>(subject.asObservable(), false);
    command.observeErrors().subscribe(testSubscriber);
    command.getEnabled().addOnPropertyChangedCallback(mockPropertyChanged);

    assertTrue(!command.canExecute());
    assertThat(command.getExec(), is((TestListener) null));
    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    subject.onNext(false);

    assertTrue(!command.canExecute());
    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    subject.onNext(true);

    assertTrue(command.canExecute());
    verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    assertTrue(!command.isUnsubscribed());
    command.unsubscribe();
    assertTrue(command.isUnsubscribed());

    testSubscriber.assertNoValues();

    command.getEnabled().removeOnPropertyChangedCallback(mockPropertyChanged);
  }

  @Test
  public void constructWithCommand() {
    RxCommand<TestListener> command = new RxCommand<>(mockListener);
    command.observeErrors().subscribe(testSubscriber);
    command.getEnabled().addOnPropertyChangedCallback(mockPropertyChanged);

    assertTrue(command.canExecute());
    assertTrue(command.isUnsubscribed());

    verify(mockListener, never()).call(anyString());
    command.getExec().call("RxProperty");
    verify(mockListener).call("RxProperty");

    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());
    testSubscriber.assertNoValues();

    command.getEnabled().removeOnPropertyChangedCallback(mockPropertyChanged);
  }

  @Test
  public void constructFromSourceWithCommand() {
    RxCommand<TestListener> command = new RxCommand<>(subject.asObservable(), mockListener);
    command.observeErrors().subscribe(testSubscriber);
    command.getEnabled().addOnPropertyChangedCallback(mockPropertyChanged);

    assertTrue(command.canExecute());
    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    subject.onNext(true);

    assertTrue(command.canExecute());
    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    subject.onNext(false);

    assertTrue(!command.canExecute());
    verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    verify(mockListener, never()).call(anyString());
    command.getExec().call("RxProperty");
    verify(mockListener).call("RxProperty");

    assertTrue(!command.isUnsubscribed());
    command.unsubscribe();
    assertTrue(command.isUnsubscribed());

    testSubscriber.assertNoValues();

    command.getEnabled().removeOnPropertyChangedCallback(mockPropertyChanged);
  }

  @Test
  public void constructFromSourceWithInitialValueAndCommand() {
    RxCommand<TestListener> command = new RxCommand<>(subject.asObservable(), false, mockListener);
    command.observeErrors().subscribe(testSubscriber);
    command.getEnabled().addOnPropertyChangedCallback(mockPropertyChanged);

    assertTrue(!command.canExecute());
    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    subject.onNext(false);

    assertTrue(!command.canExecute());
    verify(mockPropertyChanged, never()).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    subject.onNext(true);

    assertTrue(command.canExecute());
    verify(mockPropertyChanged).onPropertyChanged(Mockito.<Observable>any(), anyInt());

    verify(mockListener, never()).call(anyString());
    command.getExec().call("RxProperty");
    verify(mockListener).call("RxProperty");

    assertTrue(!command.isUnsubscribed());
    command.unsubscribe();
    assertTrue(command.isUnsubscribed());

    testSubscriber.assertNoValues();

    command.getEnabled().removeOnPropertyChangedCallback(mockPropertyChanged);
  }
}
