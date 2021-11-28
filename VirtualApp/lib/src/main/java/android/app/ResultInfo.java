package android.app;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author LittleAngry
 * @date 2021/11/28.
 */

public class ResultInfo implements Parcelable {
    protected ResultInfo(Parcel in) {
    }

    public static final Creator<ResultInfo> CREATOR = new Creator<ResultInfo>() {
        @Override
        public ResultInfo createFromParcel(Parcel in) {
            return new ResultInfo(in);
        }

        @Override
        public ResultInfo[] newArray(int size) {
            return new ResultInfo[size];
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
