package io.virtualapp.abs;

/**
 * @author Lody
 */
public interface BaseView<T> {
    void setPresenter(T presenter);

    void destroy();
}
