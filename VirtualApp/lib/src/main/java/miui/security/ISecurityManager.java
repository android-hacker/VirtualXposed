package miui.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;


/**
 *
 * MIUI Security Service
 *
 * We need to fuck it.
 *
 *
 */
public interface ISecurityManager extends IInterface {

    public static abstract class Stub extends Binder implements ISecurityManager {

        public static final String DESCRIPTOR = "miui.security.ISecurityManager";

        private static class Proxy implements ISecurityManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }
        }

        public static ISecurityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecurityManager)) {
                return new Proxy(obj);
            }
            return (ISecurityManager) iin;
        }
    }

}