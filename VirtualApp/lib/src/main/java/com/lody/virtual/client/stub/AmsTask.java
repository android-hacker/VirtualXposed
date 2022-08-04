package com.lody.virtual.client.stub;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.IAccountManagerResponse;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.helper.utils.VLog;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.accounts.AccountManager.ERROR_CODE_BAD_ARGUMENTS;
import static android.accounts.AccountManager.ERROR_CODE_CANCELED;
import static android.accounts.AccountManager.ERROR_CODE_INVALID_RESPONSE;
import static android.accounts.AccountManager.ERROR_CODE_NETWORK_ERROR;
import static android.accounts.AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION;
import static android.accounts.AccountManager.KEY_INTENT;
import static com.lody.virtual.helper.compat.AccountManagerCompat.ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE;
import static com.lody.virtual.helper.compat.AccountManagerCompat.ERROR_CODE_USER_RESTRICTED;

public abstract class AmsTask extends FutureTask<Bundle> implements AccountManagerFuture<Bundle> {
    protected final IAccountManagerResponse mResponse;
    final Handler mHandler;
    final AccountManagerCallback<Bundle> mCallback;
    final Activity mActivity;

    public AmsTask(Activity activity, Handler handler, AccountManagerCallback<Bundle> callback) {
        super(new Callable<Bundle>() {
            @Override
            public Bundle call() throws Exception {
                throw new IllegalStateException("this should never be called");
            }
        });

        mHandler = handler;
        mCallback = callback;
        mActivity = activity;
        mResponse = new Response();
    }

    public final AccountManagerFuture<Bundle> start() {
        try {
            doWork();
        } catch (RemoteException e) {
            setException(e);
        }
        return this;
    }

    @Override
    protected void set(Bundle bundle) {
        // TODO: somehow a null is being set as the result of the Future. Log this
        // case to help debug where this is occurring. When this bug is fixed this
        // condition statement should be removed.
        if (bundle == null) {
            VLog.e("AccountManager", "the bundle must not be null", new Exception());

        }
        super.set(bundle);
    }

    public abstract void doWork() throws RemoteException;

    private Bundle internalGetResult(Long timeout, TimeUnit unit)
            throws OperationCanceledException, IOException, AuthenticatorException {
        try {
            if (timeout == null) {
                return get();
            } else {
                return get(timeout, unit);
            }
        } catch (CancellationException e) {
            throw new OperationCanceledException();
        } catch (TimeoutException e) {
            // fall through and cancel
        } catch (InterruptedException e) {
            // fall through and cancel
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof UnsupportedOperationException) {
                throw new AuthenticatorException(cause);
            } else if (cause instanceof AuthenticatorException) {
                throw (AuthenticatorException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw new IllegalStateException(cause);
            }
        } finally {
            cancel(true /* interrupt if running */);
        }
        throw new OperationCanceledException();
    }

    @Override
    public Bundle getResult()
            throws OperationCanceledException, IOException, AuthenticatorException {
        return internalGetResult(null, null);
    }

    @Override
    public Bundle getResult(long timeout, TimeUnit unit)
            throws OperationCanceledException, IOException, AuthenticatorException {
        return internalGetResult(timeout, unit);
    }

    @Override
    protected void done() {
        if (mCallback != null) {
            postToHandler(mHandler, mCallback, this);
        }
    }

    /**
     * Handles the responses from the AccountManager
     */
    private class Response extends IAccountManagerResponse.Stub {
        @Override
        public void onResult(Bundle bundle) {
            Intent intent = bundle.getParcelable(KEY_INTENT);
            if (intent != null && mActivity != null) {
                // since the user provided an Activity we will silently start intents
                // that we see
                mActivity.startActivity(intent);
                // leave the Future running to wait for the real response to this request
            } else if (bundle.getBoolean("retry")) {
                try {
                    doWork();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            } else {
                set(bundle);
            }
        }

        @Override
        public void onError(int code, String message) {
            if (code == ERROR_CODE_CANCELED || code == ERROR_CODE_USER_RESTRICTED
                    || code == ERROR_CODE_MANAGEMENT_DISABLED_FOR_ACCOUNT_TYPE) {
                // the authenticator indicated that this request was canceled or we were
                // forbidden to fulfill; cancel now
                cancel(true /* mayInterruptIfRunning */);
                return;
            }
            setException(convertErrorToException(code, message));
        }
    }

    private Exception convertErrorToException(int code, String message) {
        if (code == ERROR_CODE_NETWORK_ERROR) {
            return new IOException(message);
        }

        if (code == ERROR_CODE_UNSUPPORTED_OPERATION) {
            return new UnsupportedOperationException(message);
        }

        if (code == ERROR_CODE_INVALID_RESPONSE) {
            return new AuthenticatorException(message);
        }

        if (code == ERROR_CODE_BAD_ARGUMENTS) {
            return new IllegalArgumentException(message);
        }

        return new AuthenticatorException(message);
    }

    private void postToHandler(Handler handler, final AccountManagerCallback<Bundle> callback,
                               final AccountManagerFuture<Bundle> future) {
        handler = handler == null ? VirtualRuntime.getUIHandler() : handler;
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.run(future);
            }
        });
    }
}