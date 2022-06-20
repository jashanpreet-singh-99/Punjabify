package com.ck.dev.punjabify.observers;

import java.util.Observable;

public class HomeObservableObject extends Observable {

    private static HomeObservableObject instance = new HomeObservableObject();

    public HomeObservableObject() {
    }

    public static HomeObservableObject getInstance() {
        return instance;
    }

    public  void  updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }

}
