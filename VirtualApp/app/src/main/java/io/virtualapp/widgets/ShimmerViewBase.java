package io.virtualapp.widgets;

public interface ShimmerViewBase {

    float getGradientX();

    void setGradientX(float gradientX);

    boolean isShimmering();

    void setShimmering(boolean isShimmering);

    boolean isSetUp();

    void setAnimationSetupCallback(ShimmerViewHelper.AnimationSetupCallback callback);

    int getPrimaryColor();

    void setPrimaryColor(int primaryColor);

    int getReflectionColor();

    void setReflectionColor(int reflectionColor);
}