package com.lody.virtual.server.accounts;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

public class VAccount implements Parcelable {
    public static final Parcelable.Creator<VAccount> CREATOR = new Parcelable.Creator<VAccount>() {
        @Override
        public VAccount createFromParcel(Parcel source) {
            return new VAccount(source);
        }

        @Override
        public VAccount[] newArray(int size) {
            return new VAccount[size];
        }
    };
    public int userId;
    public String name;
    public String previousName;
    public String type;
    public String password;
    public long lastAuthenticatedTime;
    public Map<String, String> authTokens;
    public Map<String, String> userDatas;


    public VAccount(int userId, Account account) {
        this.userId = userId;
        name = account.name;
        type = account.type;
        authTokens = new HashMap<>();
        userDatas = new HashMap<>();
    }

    public VAccount(Parcel in) {
        userId = in.readInt();
        name = in.readString();
        previousName = in.readString();
        type = in.readString();
        password = in.readString();
        lastAuthenticatedTime = in.readLong();
        int authTokensSize = in.readInt();
        authTokens = new HashMap<>(authTokensSize);
        for (int i = 0; i < authTokensSize; i++) {
            String key = in.readString();
            String value = in.readString();
            authTokens.put(key, value);
        }
        int userDatasSize = in.readInt();
        userDatas = new HashMap<>(userDatasSize);
        for (int i = 0; i < userDatasSize; i++) {
            String key = in.readString();
            String value = in.readString();
            userDatas.put(key, value);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(name);
        dest.writeString(previousName);
        dest.writeString(type);
        dest.writeString(password);
        dest.writeLong(lastAuthenticatedTime);
        dest.writeInt(authTokens.size());
        for (Map.Entry<String, String> entry : authTokens.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
        dest.writeInt(userDatas.size());
        for (Map.Entry<String, String> entry : userDatas.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }
}

