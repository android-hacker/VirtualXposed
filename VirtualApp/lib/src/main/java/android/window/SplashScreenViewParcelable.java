package android.window;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author LittleAngry
 * @date 2021/11/28.
 */
public class SplashScreenViewParcelable implements Parcelable {

    protected SplashScreenViewParcelable(Parcel in) {
    }

    public static final Creator<SplashScreenViewParcelable> CREATOR = new Creator<SplashScreenViewParcelable>() {
        @Override
        public SplashScreenViewParcelable createFromParcel(Parcel in) {
            return new SplashScreenViewParcelable(in);
        }

        @Override
        public SplashScreenViewParcelable[] newArray(int size) {
            return new SplashScreenViewParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
