// IUiObserver.aidl
package com.lody.virtual.server.interfaces;

interface IUiObserver {
    void enterAppUI(int userId, in String packageName);
    void exitAppUI(int userId, in String packageName);
}
