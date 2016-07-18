package com.lody.virtual.service.account;

import android.accounts.AuthenticatorDescription;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.RegisteredServicesCacheListener;
import android.os.Handler;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * An interface to the Authenticator specialization of RegisteredServicesCache. The use of
 * this interface by the AccountManagerService makes it easier to unit test it.
 * @hide
 */
public interface IAccountAuthenticatorCache {
    /**
     * Accessor for the {@link RegisteredServicesCache.ServiceInfo} that
     * matched the specified {@link android.accounts.AuthenticatorDescription} or null
     * if none match.
     * @param type the authenticator type to return
     * @return the {@link RegisteredServicesCache.ServiceInfo} that
     * matches the account type or null if none is present
     */
    RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> getServiceInfo(
            AuthenticatorDescription type);

    /**
     * @return A copy of a Collection of all the current Authenticators.
     */
    Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> getAllServices();

    /**
     * Dumps the state of the cache. See
     * {@link android.os.Binder#dump(FileDescriptor, PrintWriter, String[])}
     */
    void dump(FileDescriptor fd, PrintWriter fout, String[] args);

    /**
     * Sets a listener that will be notified whenever the authenticator set changes
     * @param listener the listener to notify, or null
     * @param handler the {@link Handler} on which the notification will be posted. If null
     * the notification will be posted on the main thread.
     */
    void setListener(RegisteredServicesCacheListener<AuthenticatorDescription> listener,
                     Handler handler);
}