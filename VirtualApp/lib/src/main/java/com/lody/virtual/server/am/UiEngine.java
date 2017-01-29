package com.lody.virtual.server.am;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.lody.virtual.helper.utils.collection.SparseArray;
import com.lody.virtual.server.interfaces.IUiObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

class UiEngine {

    private RemoteCallbackList<IUiObserver> observers = new RemoteCallbackList<>();

    private SparseArray<Map<String, Integer>> appMarker = new SparseArray<>();

    void addObserver(IUiObserver observer) {
        observers.register(observer);
    }

    void removeObserver(IUiObserver observer) {
        observers.unregister(observer);
    }

    void enterActivity(int userId, String packageName) {
        Map<String, Integer> appCounter = appMarker.get(userId);
        if (appCounter == null) {
            appCounter = new HashMap<>();
            appMarker.put(userId, appCounter);
        }
        Integer count = appCounter.get(packageName);
        if (count == null || count == 0) {
            final int N = observers.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    observers.getBroadcastItem(i).enterAppUI(userId, packageName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            observers.finishBroadcast();
        }
        if (count == null) {
            appCounter.put(packageName, 1);
        } else {
            appCounter.put(packageName, count + 1);
        }
    }

    void exitActivity(int userId, String packageName) {
        Map<String, Integer> appCounter = appMarker.get(userId);
        if (appCounter != null) {
            Integer count = appCounter.get(packageName);
            if (count != null) {
                if (count <= 0) {
                    count = 1;
                }
                count = count - 1;
                if (count == 0) {
                    final int N = observers.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            observers.getBroadcastItem(i).exitAppUI(userId, packageName);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    observers.finishBroadcast();
                }
                appCounter.put(packageName, count);
            }
        }
    }

    void appDead(int userId, String packageName) {
        Map<String, Integer> appCounter = appMarker.get(userId);
        if (appCounter != null) {
            appCounter.remove(packageName);
            final int N = observers.beginBroadcast();
            for (int i = 0; i < N; i++) {
                try {
                    observers.getBroadcastItem(i).exitAppUI(userId, packageName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            observers.finishBroadcast();
        }
    }
}

