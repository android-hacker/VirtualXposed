package com.lody.virtual.client.stub;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lody.virtual.R;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VAccountManager;
import com.lody.virtual.helper.utils.VLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @hide
 */
public class ChooseAccountTypeActivity extends Activity {
    private static final String TAG = "AccountChooser";

    private HashMap<String, AuthInfo> mTypeToAuthenticatorInfo = new HashMap<String, AuthInfo>();
    private ArrayList<AuthInfo> mAuthenticatorInfosToDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read the validAccountTypes, if present, and add them to the setOfAllowableAccountTypes
        Set<String> setOfAllowableAccountTypes = null;
        String[] validAccountTypes = getIntent().getStringArrayExtra(
                ChooseTypeAndAccountActivity.EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY);
        if (validAccountTypes != null) {
            setOfAllowableAccountTypes = new HashSet<>(validAccountTypes.length);
            Collections.addAll(setOfAllowableAccountTypes, validAccountTypes);
        }

        // create a map of account authenticators
        buildTypeToAuthDescriptionMap();

        // Create a list of authenticators that are allowable. Filter out those that
        // don't match the allowable account types, if provided.
        mAuthenticatorInfosToDisplay = new ArrayList<>(mTypeToAuthenticatorInfo.size());
        for (Map.Entry<String, AuthInfo> entry: mTypeToAuthenticatorInfo.entrySet()) {
            final String type = entry.getKey();
            final AuthInfo info = entry.getValue();
            if (setOfAllowableAccountTypes != null
                    && !setOfAllowableAccountTypes.contains(type)) {
                continue;
            }
            mAuthenticatorInfosToDisplay.add(info);
        }

        if (mAuthenticatorInfosToDisplay.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "no allowable account types");
            setResult(Activity.RESULT_OK, new Intent().putExtras(bundle));
            finish();
            return;
        }

        if (mAuthenticatorInfosToDisplay.size() == 1) {
            setResultAndFinish(mAuthenticatorInfosToDisplay.get(0).desc.type);
            return;
        }

        setContentView(R.layout.choose_account_type);
        // Setup the list
        ListView list = (ListView) findViewById(android.R.id.list);
        // Use an existing ListAdapter that will map an array of strings to TextViews
        list.setAdapter(new AccountArrayAdapter(this,
                android.R.layout.simple_list_item_1, mAuthenticatorInfosToDisplay));
        list.setChoiceMode(ListView.CHOICE_MODE_NONE);
        list.setTextFilterEnabled(false);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                setResultAndFinish(mAuthenticatorInfosToDisplay.get(position).desc.type);
            }
        });
    }

    private void setResultAndFinish(final String type) {
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, type);
        setResult(Activity.RESULT_OK, new Intent().putExtras(bundle));
        VLog.v(TAG, "ChooseAccountTypeActivity.setResultAndFinish: "
                + "selected account type " + type);
        finish();
    }

    private void buildTypeToAuthDescriptionMap() {
        for(AuthenticatorDescription desc : VAccountManager.get().getAuthenticatorTypes()) {
            String name = null;
            Drawable icon = null;
            try {
                Resources res = VirtualCore.get().getResources(desc.packageName);
                icon = res.getDrawable(desc.iconId);
                final CharSequence sequence = res.getText(desc.labelId);
                name = sequence.toString();
                name = sequence.toString();
            } catch (Resources.NotFoundException e) {
                // Nothing we can do much here, just log
                VLog.w(TAG, "No icon resource for account type " + desc.type);
            }
            AuthInfo authInfo = new AuthInfo(desc, name, icon);
            mTypeToAuthenticatorInfo.put(desc.type, authInfo);
        }
    }

    private static class AuthInfo {
        final AuthenticatorDescription desc;
        final String name;
        final Drawable drawable;

        AuthInfo(AuthenticatorDescription desc, String name, Drawable drawable) {
            this.desc = desc;
            this.name = name;
            this.drawable = drawable;
        }
    }

    private static class ViewHolder {
        ImageView icon;
        TextView text;
    }

    private static class AccountArrayAdapter extends ArrayAdapter<AuthInfo> {
        private LayoutInflater mLayoutInflater;
        private ArrayList<AuthInfo> mInfos;

        AccountArrayAdapter(Context context, int textViewResourceId,
                            ArrayList<AuthInfo> infos) {
            super(context, textViewResourceId, infos);
            mInfos = infos;
            mLayoutInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.choose_account_row, null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.account_row_text);
                holder.icon = (ImageView) convertView.findViewById(R.id.account_row_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(mInfos.get(position).name);
            holder.icon.setImageDrawable(mInfos.get(position).drawable);

            return convertView;
        }
    }
}
