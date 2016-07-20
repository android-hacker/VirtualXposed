package android.accounts;

import android.os.Bundle;

/**
 * The interface used to return responses for asynchronous calls to the {@link IAccountManager}
 */
interface IAccountManagerResponse {
    void onResult(in Bundle value);
    void onError(int errorCode, String errorMessage);
}
