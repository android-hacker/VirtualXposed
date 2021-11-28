package android.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author LittleAngry
 * @date 2021/11/28.
 */
public class PictureInPictureUiState implements Parcelable {

    protected PictureInPictureUiState(Parcel in) {
    }

    public static final Creator<PictureInPictureUiState> CREATOR = new Creator<PictureInPictureUiState>() {
        @Override
        public PictureInPictureUiState createFromParcel(Parcel in) {
            return new PictureInPictureUiState(in);
        }

        @Override
        public PictureInPictureUiState[] newArray(int size) {
            return new PictureInPictureUiState[size];
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
