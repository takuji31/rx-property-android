package jp.keita.kagurazaka.rxproperty.util;

import android.databinding.BaseObservable;

public final class Person extends BaseObservable {
    public static final int ID_FIRST_NAME = 1;
    public static final int ID_LAST_NAME = 2;

    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        notifyPropertyChanged(ID_FIRST_NAME);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        notifyPropertyChanged(ID_LAST_NAME);
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
