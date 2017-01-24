// IUiObserver.aidl
package com.lody.virtual.service.interfaces;

interface IUiObserver {
    void enterAppUI(in String packageName);
    void exitAppUI(in String packageName);
}
