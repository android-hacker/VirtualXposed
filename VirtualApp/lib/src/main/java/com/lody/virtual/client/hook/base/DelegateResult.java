package com.lody.virtual.client.hook.base;


public class DelegateResult<T> {
    private T value;

    public DelegateResult(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}