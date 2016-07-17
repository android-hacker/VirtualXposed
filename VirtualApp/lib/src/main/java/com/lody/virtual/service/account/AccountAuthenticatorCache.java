package com.lody.virtual.service.account;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.accounts.IAccountAuthenticator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.XmlSerializerAndParser;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * A cache of services that export the {@link IAccountAuthenticator} interface. This cache
 * is built by interrogating the {@link PackageManager} and is updated as packages are added,
 * removed and changed. The authenticators are referred to by their account type and
 * are made available via the {@link RegisteredServicesCache#getServiceInfo} method.
 */
/* package private */ class AccountAuthenticatorCache
        extends RegisteredServicesCache<AuthenticatorDescription>
        implements IAccountAuthenticatorCache {
    private static final MySerializer sSerializer = new MySerializer();

    public AccountAuthenticatorCache(Context context) {
        super(context, AccountManager.ACTION_AUTHENTICATOR_INTENT,
                AccountManager.AUTHENTICATOR_META_DATA_NAME,
                AccountManager.AUTHENTICATOR_ATTRIBUTES_NAME, sSerializer);
    }

    public AuthenticatorDescription parseServiceAttributes(Resources res,
            String packageName, AttributeSet attrs) {
        TypedArray sa = res.obtainAttributes(attrs,
                com.android.internal.R.styleable.AccountAuthenticator);
        try {
            final String accountType =
                    sa.getString(com.android.internal.R.styleable.AccountAuthenticator_accountType);
            final int labelId = sa.getResourceId(
                    com.android.internal.R.styleable.AccountAuthenticator_label, 0);
            final int iconId = sa.getResourceId(
                    com.android.internal.R.styleable.AccountAuthenticator_icon, 0);
            final int smallIconId = sa.getResourceId(
                    com.android.internal.R.styleable.AccountAuthenticator_smallIcon, 0);
            final int prefId = sa.getResourceId(
                    com.android.internal.R.styleable.AccountAuthenticator_accountPreferences, 0);
            final boolean customTokens = sa.getBoolean(
                    com.android.internal.R.styleable.AccountAuthenticator_customTokens, false);
            if (TextUtils.isEmpty(accountType)) {
                return null;
            }
            return new AuthenticatorDescription(accountType, packageName, labelId, iconId,
                    smallIconId, prefId, customTokens);
        } finally {
            sa.recycle();
        }
    }

    private static class MySerializer implements XmlSerializerAndParser<AuthenticatorDescription> {
        public void writeAsXml(AuthenticatorDescription item, XmlSerializer out)
                throws IOException {
            out.attribute(null, "type", item.type);
        }

        public AuthenticatorDescription createFromXml(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            return AuthenticatorDescription.newKey(parser.getAttributeValue(null, "type"));
        }
    }
}
