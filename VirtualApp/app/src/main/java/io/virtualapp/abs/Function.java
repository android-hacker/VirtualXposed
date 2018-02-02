package io.virtualapp.abs;

/**
 * author: weishu on 18/2/3.
 */
public interface Function<T, R> {
    R apply(T r);
}
