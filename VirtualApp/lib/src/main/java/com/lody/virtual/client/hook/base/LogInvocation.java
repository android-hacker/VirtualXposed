package com.lody.virtual.client.hook.base;

import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Add this annotation to a {@link MethodProxy} or a {@link MethodInvocationStub} to
 * log all the calls and their arguments.
 *
 * Obviously, this is only useful for debugging.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LogInvocation {
    public Condition value() default Condition.ALWAYS;

    static enum Condition {
        /** Never logs anything */
        NEVER {
            public int getLogLevel(boolean isHooked, boolean isError) {
                return -1;
            }
        },
        /**
         * Logs every call.
         * Mostly useful for debugging.
         */
        ALWAYS {
            public int getLogLevel(boolean isHooked, boolean isError) {
                return isError ? Log.WARN : Log.INFO;
            }
        },
        /**
         * Logs only calls that exited with error
         * A reasonable tradeoff between noise and getting relevant information
         */
        ON_ERROR {
            public int getLogLevel(boolean isHooked, boolean isError) {
                return isError ? Log.WARN : -1;
            }
        },

        /**
         *  Log only calls that haven't been hooked
         *  It only makes sense on a MethodInvocationProxy, and is useful to pinpoint missing methods
         */
        NOT_HOOKED {
            public int getLogLevel(boolean isHooked, boolean isError) {
                return isHooked ? -1 : isError ? Log.WARN : Log.INFO;
            }
        };

        public abstract int getLogLevel(boolean isHooked, boolean isError);
    };
};
