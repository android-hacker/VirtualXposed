package com.lody.virtual.service.accounts;

import android.accounts.AuthenticatorDescription;
import android.os.Handler;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * An interface to the Authenticator specialization of RegisteredServicesCache. The use of
 * this interface by the VAccountManagerService makes it easier to unit test it.
 * @hide
 */
public interface IAccountAuthenticatorCache {
    /**
     * Accessor for the {@link RegisteredServicesCache.ServiceInfo} that
     * matched the specified {@link AuthenticatorDescription} or null
     * if none match.
     * @param type the authenticator type to return
     * @return the {@link RegisteredServicesCache.ServiceInfo} that
     * matches the account type or null if none is present
     */
    RegisteredServicesCache.ServiceInfo<AuthenticatorDescription> getServiceInfo(
            AuthenticatorDescription type, int userId);

    /**
     * @return A copy of a Collection of all the current Authenticators.
     */
    Collection<RegisteredServicesCache.ServiceInfo<AuthenticatorDescription>> getAllServices(
            int userId);

    /**
     * Dumps the state of the cache. See
     * {@link android.os.Binder#dump(FileDescriptor, PrintWriter, String[])}
     */
    void dump(FileDescriptor fd, PrintWriter fout, String[] args, int userId);

    /**
     * Sets a listener that will be notified whenever the authenticator set changes
     * @param listener the listener to notify, or null
     * @param handler the {@link Handler} on which the notification will be posted. If null
     * the notification will be posted on the main thread.
     */
    void setListener(RegisteredServicesCacheListener<AuthenticatorDescription> listener,
                     Handler handler);

    void invalidateCache(int userId);
}
