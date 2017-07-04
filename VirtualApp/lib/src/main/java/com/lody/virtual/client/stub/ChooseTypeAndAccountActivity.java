package com.lody.virtual.client.stub;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorDescription;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.lody.virtual.R;
import com.lody.virtual.client.ipc.VAccountManager;
import com.lody.virtual.helper.utils.VLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChooseTypeAndAccountActivity extends Activity
        implements AccountManagerCallback<Bundle> {
    private static final String TAG = "AccountChooser";

    /**
     * A Parcelable ArrayList of Account objects that limits the choosable accounts to those
     * in this list, if this parameter is supplied.
     */
    public static final String EXTRA_ALLOWABLE_ACCOUNTS_ARRAYLIST = "allowableAccounts";

    /**
     * A Parcelable ArrayList of String objects that limits the accounts to choose to those
     * that match the types in this list, if this parameter is supplied. This list is also
     * used to filter the allowable account types if add account is selected.
     */
    public static final String EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY = "allowableAccountTypes";

    /**
     * This is passed as the addAccountOptions parameter in AccountManager.addAccount()
     * if it is called.
     */
    public static final String EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE = "addAccountOptions";

    /**
     * This is passed as the requiredFeatures parameter in AccountManager.addAccount()
     * if it is called.
     */
    public static final String EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY =
            "addAccountRequiredFeatures";

    /**
     * This is passed as the authTokenType string in AccountManager.addAccount()
     * if it is called.
     */
    public static final String EXTRA_ADD_ACCOUNT_AUTH_TOKEN_TYPE_STRING = "authTokenType";

    /**
     * If set then the specified account is already "selected".
     */
    public static final String EXTRA_SELECTED_ACCOUNT = "selectedAccount";

    /**
     * Deprecated. Providing this extra to {@link ChooseTypeAndAccountActivity}
     * will have no effect.
     */
    @Deprecated
    public static final String EXTRA_ALWAYS_PROMPT_FOR_ACCOUNT =
            "alwaysPromptForAccount";

    /**
     * If set then this string willb e used as the description rather than
     * the default.
     */
    public static final String EXTRA_DESCRIPTION_TEXT_OVERRIDE =
            "descriptionTextOverride";

    public static final int REQUEST_NULL = 0;
    public static final int REQUEST_CHOOSE_TYPE = 1;
    public static final int REQUEST_ADD_ACCOUNT = 2;

    private static final String KEY_INSTANCE_STATE_PENDING_REQUEST = "pendingRequest";
    private static final String KEY_INSTANCE_STATE_EXISTING_ACCOUNTS = "existingAccounts";
    private static final String KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME = "selectedAccountName";
    private static final String KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT = "selectedAddAccount";
    private static final String KEY_INSTANCE_STATE_ACCOUNT_LIST = "accountList";
    public static final String KEY_USER_ID = "userId";

    private static final int SELECTED_ITEM_NONE = -1;

    private Set<Account> mSetOfAllowableAccounts;
    private Set<String> mSetOfRelevantAccountTypes;
    private String mSelectedAccountName = null;
    private boolean mSelectedAddNewAccount = false;
    private String mDescriptionOverride;

    private ArrayList<Account> mAccounts;
    private int mPendingRequest = REQUEST_NULL;
    private Parcelable[] mExistingAccounts = null;
    private int mSelectedItemIndex;
    private Button mOkButton;
    private int mCallingUserId;
    private boolean mDontShowPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // save some items we use frequently
        final Intent intent = getIntent();

        if (savedInstanceState != null) {
            mPendingRequest = savedInstanceState.getInt(KEY_INSTANCE_STATE_PENDING_REQUEST);
            mExistingAccounts =
                    savedInstanceState.getParcelableArray(KEY_INSTANCE_STATE_EXISTING_ACCOUNTS);

            // Makes sure that any user selection is preserved across orientation changes.
            mSelectedAccountName = savedInstanceState.getString(
                    KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME);

            mSelectedAddNewAccount = savedInstanceState.getBoolean(
                    KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, false);
            mAccounts = savedInstanceState.getParcelableArrayList(KEY_INSTANCE_STATE_ACCOUNT_LIST);
            mCallingUserId = savedInstanceState.getInt(KEY_USER_ID);
        } else {
            mPendingRequest = REQUEST_NULL;
            mExistingAccounts = null;
            mCallingUserId = intent.getIntExtra(KEY_USER_ID, -1);
            // If the selected account as specified in the intent matches one in the list we will
            // show is as pre-selected.
            Account selectedAccount = intent.getParcelableExtra(EXTRA_SELECTED_ACCOUNT);
            if (selectedAccount != null) {
                mSelectedAccountName = selectedAccount.name;
            }
        }
        VLog.v(TAG, "selected account name is " + mSelectedAccountName);

        mSetOfAllowableAccounts = getAllowableAccountSet(intent);
        mSetOfRelevantAccountTypes = getReleventAccountTypes(intent);
        mDescriptionOverride = intent.getStringExtra(EXTRA_DESCRIPTION_TEXT_OVERRIDE);

        mAccounts = getAcceptableAccountChoices(VAccountManager.get());

        if (mDontShowPicker) {
            super.onCreate(savedInstanceState);
            return;
        }

        // In cases where the activity does not need to show an account picker, cut the chase
        // and return the result directly. Eg:
        // Single account -> select it directly
        // No account -> launch add account activity directly
        if (mPendingRequest == REQUEST_NULL) {
            // If there are no relevant accounts and only one relevant account type go directly to
            // add account. Otherwise let the user choose.
            if (mAccounts.isEmpty()) {
                setNonLabelThemeAndCallSuperCreate(savedInstanceState);
                if (mSetOfRelevantAccountTypes.size() == 1) {
                    runAddAccountForAuthenticator(mSetOfRelevantAccountTypes.iterator().next());
                } else {
                    startChooseAccountTypeActivity();
                }
            }
        }

        String[] listItems = getListOfDisplayableOptions(mAccounts);
        mSelectedItemIndex = getItemIndexToSelect(
                mAccounts, mSelectedAccountName, mSelectedAddNewAccount);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_type_and_account);
        overrideDescriptionIfSupplied(mDescriptionOverride);
        populateUIAccountList(listItems);

        // Only enable "OK" button if something has been selected.
        mOkButton = (Button) findViewById(android.R.id.button2);
        mOkButton.setEnabled(mSelectedItemIndex != SELECTED_ITEM_NONE);
    }

    @Override
    protected void onDestroy() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.onDestroy()");
        }
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_INSTANCE_STATE_PENDING_REQUEST, mPendingRequest);
        if (mPendingRequest == REQUEST_ADD_ACCOUNT) {
            outState.putParcelableArray(KEY_INSTANCE_STATE_EXISTING_ACCOUNTS, mExistingAccounts);
        }
        if (mSelectedItemIndex != SELECTED_ITEM_NONE) {
            if (mSelectedItemIndex == mAccounts.size()) {
                outState.putBoolean(KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, true);
            } else {
                outState.putBoolean(KEY_INSTANCE_STATE_SELECTED_ADD_ACCOUNT, false);
                outState.putString(KEY_INSTANCE_STATE_SELECTED_ACCOUNT_NAME,
                        mAccounts.get(mSelectedItemIndex).name);
            }
        }
        outState.putParcelableArrayList(KEY_INSTANCE_STATE_ACCOUNT_LIST, mAccounts);
    }

    public void onCancelButtonClicked(View view) {
        onBackPressed();
    }

    public void onOkButtonClicked(View view) {
        if (mSelectedItemIndex == mAccounts.size()) {
            // Selected "Add New Account" option
            startChooseAccountTypeActivity();
        } else if (mSelectedItemIndex != SELECTED_ITEM_NONE) {
            onAccountSelected(mAccounts.get(mSelectedItemIndex));
        }
    }

    // Called when the choose account type activity (for adding an account) returns.
    // If it was a success read the account and set it in the result. In all cases
    // return the result and finish this activity.
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            if (data != null && data.getExtras() != null) data.getExtras().keySet();
            Bundle extras = data != null ? data.getExtras() : null;
            Log.v(TAG, "ChooseTypeAndAccountActivity.onActivityResult(reqCode=" + requestCode
                    + ", resCode=" + resultCode + ", extras=" + extras + ")");
        }

        // we got our result, so clear the fact that we had a pending request
        mPendingRequest = REQUEST_NULL;

        if (resultCode == RESULT_CANCELED) {
            // if canceling out of addAccount and the original state caused us to skip this,
            // finish this activity
            if (mAccounts.isEmpty()) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
            return;
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHOOSE_TYPE) {
                if (data != null) {
                    String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                    if (accountType != null) {
                        runAddAccountForAuthenticator(accountType);
                        return;
                    }
                }
                Log.d(TAG, "ChooseTypeAndAccountActivity.onActivityResult: unable to find account "
                        + "type, pretending the request was canceled");
            } else if (requestCode == REQUEST_ADD_ACCOUNT) {
                String accountName = null;
                String accountType = null;

                if (data != null) {
                    accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                }

                if (accountName == null || accountType == null) {
                    Account[] currentAccounts = VAccountManager.get().getAccounts(mCallingUserId, null);
                    Set<Account> preExistingAccounts = new HashSet<>();
                    for (Parcelable accountParcel : mExistingAccounts) {
                        preExistingAccounts.add((Account) accountParcel);
                    }
                    for (Account account : currentAccounts) {
                        if (!preExistingAccounts.contains(account)) {
                            accountName = account.name;
                            accountType = account.type;
                            break;
                        }
                    }
                }

                if (accountName != null || accountType != null) {
                    setResultAndFinish(accountName, accountType);
                    return;
                }
            }
            Log.d(TAG, "ChooseTypeAndAccountActivity.onActivityResult: unable to find added "
                    + "account, pretending the request was canceled");
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "ChooseTypeAndAccountActivity.onActivityResult: canceled");
        }
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    protected void runAddAccountForAuthenticator(String type) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "runAddAccountForAuthenticator: " + type);
        }
        final Bundle options = getIntent().getBundleExtra(
                ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE);
        final String[] requiredFeatures = getIntent().getStringArrayExtra(
                ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY);
        final String authTokenType = getIntent().getStringExtra(
                ChooseTypeAndAccountActivity.EXTRA_ADD_ACCOUNT_AUTH_TOKEN_TYPE_STRING);
        VAccountManager.get().addAccount(mCallingUserId, type, authTokenType, requiredFeatures,
                options, null /* activity */, this /* callback */, null /* Handler */);
    }

    @Override
    public void run(final AccountManagerFuture<Bundle> accountManagerFuture) {
        try {
            final Bundle accountManagerResult = accountManagerFuture.getResult();
            final Intent intent = accountManagerResult.getParcelable(
                    AccountManager.KEY_INTENT);
            if (intent != null) {
                mPendingRequest = REQUEST_ADD_ACCOUNT;
                mExistingAccounts = VAccountManager.get().getAccounts(mCallingUserId, null);
                intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, REQUEST_ADD_ACCOUNT);
                return;
            }
        } catch (OperationCanceledException e) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        } catch (IOException e) {
        } catch (AuthenticatorException e) {
        }
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ERROR_MESSAGE, "error communicating with server");
        setResult(Activity.RESULT_OK, new Intent().putExtras(bundle));
        finish();
    }

    /**
     * The default activity theme shows label at the top. Set a theme which does
     * not show label, which effectively makes the activity invisible. Note that
     * no content is being set. If something gets set, using this theme may be
     * useless.
     */
    private void setNonLabelThemeAndCallSuperCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTheme(android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        } else {
            setTheme(android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        }
        super.onCreate(savedInstanceState);
    }

    private void onAccountSelected(Account account) {
        Log.d(TAG, "selected account " + account);
        setResultAndFinish(account.name, account.type);
    }

    private void setResultAndFinish(final String accountName, final String accountType) {
        Bundle bundle = new Bundle();
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, accountName);
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        setResult(Activity.RESULT_OK, new Intent().putExtras(bundle));
        VLog.v(TAG, "ChooseTypeAndAccountActivity.setResultAndFinish: "
                + "selected account " + accountName + ", " + accountType);
        finish();
    }

    private void startChooseAccountTypeActivity() {
        VLog.v(TAG, "ChooseAccountTypeActivity.startChooseAccountTypeActivity()");
        final Intent intent = new Intent(this, ChooseAccountTypeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY,
                getIntent().getStringArrayExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY));
        intent.putExtra(EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE,
                getIntent().getBundleExtra(EXTRA_ADD_ACCOUNT_OPTIONS_BUNDLE));
        intent.putExtra(EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY,
                getIntent().getStringArrayExtra(EXTRA_ADD_ACCOUNT_REQUIRED_FEATURES_STRING_ARRAY));
        intent.putExtra(EXTRA_ADD_ACCOUNT_AUTH_TOKEN_TYPE_STRING,
                getIntent().getStringExtra(EXTRA_ADD_ACCOUNT_AUTH_TOKEN_TYPE_STRING));
        startActivityForResult(intent, REQUEST_CHOOSE_TYPE);
        mPendingRequest = REQUEST_CHOOSE_TYPE;
    }

    /**
     * @return a value between 0 (inclusive) and accounts.size() (inclusive) or SELECTED_ITEM_NONE.
     * An index value of accounts.size() indicates 'Add account' option.
     */
    private int getItemIndexToSelect(ArrayList<Account> accounts, String selectedAccountName,
                                     boolean selectedAddNewAccount) {
        // If "Add account" option was previously selected by user, preserve it across
        // orientation changes.
        if (selectedAddNewAccount) {
            return accounts.size();
        }
        // search for the selected account name if present
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).name.equals(selectedAccountName)) {
                return i;
            }
        }
        // no account selected.
        return SELECTED_ITEM_NONE;
    }

    private String[] getListOfDisplayableOptions(ArrayList<Account> accounts) {
        // List of options includes all accounts found together with "Add new account" as the
        // last item in the list.
        String[] listItems = new String[accounts.size() + 1];
        for (int i = 0; i < accounts.size(); i++) {
            listItems[i] = accounts.get(i).name;
        }
        listItems[accounts.size()] = getResources().getString(
                R.string.add_account_button_label);
        return listItems;
    }

    /**
     * Create a list of Account objects for each account that is acceptable. Filter out
     * accounts that don't match the allowable types, if provided, or that don't match the
     * allowable accounts, if provided.
     */
    private ArrayList<Account> getAcceptableAccountChoices(VAccountManager accountManager) {
        final Account[] accounts = accountManager.getAccounts(mCallingUserId, null);
        ArrayList<Account> accountsToPopulate = new ArrayList<>(accounts.length);
        for (Account account : accounts) {
            if (mSetOfAllowableAccounts != null && !mSetOfAllowableAccounts.contains(account)) {
                continue;
            }
            if (mSetOfRelevantAccountTypes != null
                    && !mSetOfRelevantAccountTypes.contains(account.type)) {
                continue;
            }
            accountsToPopulate.add(account);
        }
        return accountsToPopulate;
    }

    /**
     * Return a set of account types specified by the intent as well as supported by the
     * AccountManager.
     */
    private Set<String> getReleventAccountTypes(final Intent intent) {
        // An account type is relevant iff it is allowed by the caller and supported by the account
        // manager.
        Set<String> setOfRelevantAccountTypes;
        final String[] allowedAccountTypes =
                intent.getStringArrayExtra(EXTRA_ALLOWABLE_ACCOUNT_TYPES_STRING_ARRAY);
        AuthenticatorDescription[] descs = VAccountManager.get().getAuthenticatorTypes();
        Set<String> supportedAccountTypes = new HashSet<String>(descs.length);
        for (AuthenticatorDescription desc : descs) {
            supportedAccountTypes.add(desc.type);
        }
        if (allowedAccountTypes != null) {
            setOfRelevantAccountTypes = new HashSet<>();
            Collections.addAll(setOfRelevantAccountTypes, allowedAccountTypes);
            setOfRelevantAccountTypes.retainAll(supportedAccountTypes);
        } else {
            setOfRelevantAccountTypes = supportedAccountTypes;
        }
        return setOfRelevantAccountTypes;
    }

    /**
     * Returns a set of whitelisted accounts given by the intent or null if none specified by the
     * intent.
     */
    private Set<Account> getAllowableAccountSet(final Intent intent) {
        Set<Account> setOfAllowableAccounts = null;
        final ArrayList<Parcelable> validAccounts =
                intent.getParcelableArrayListExtra(EXTRA_ALLOWABLE_ACCOUNTS_ARRAYLIST);
        if (validAccounts != null) {
            setOfAllowableAccounts = new HashSet<>(validAccounts.size());
            for (Parcelable parcelable : validAccounts) {
                setOfAllowableAccounts.add((Account) parcelable);
            }
        }
        return setOfAllowableAccounts;
    }

    /**
     * Overrides the description text view for the picker activity if specified by the intent.
     * If not specified then makes the description invisible.
     */
    private void overrideDescriptionIfSupplied(String descriptionOverride) {
        TextView descriptionView = (TextView) findViewById(R.id.description);
        if (!TextUtils.isEmpty(descriptionOverride)) {
            descriptionView.setText(descriptionOverride);
        } else {
            descriptionView.setVisibility(View.GONE);
        }
    }

    /**
     * Populates the UI ListView with the given list of items and selects an item
     * based on {@code mSelectedItemIndex} member variable.
     */
    private void populateUIAccountList(String[] listItems) {
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, listItems));
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setItemsCanFocus(false);
        list.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                        mSelectedItemIndex = position;
                        mOkButton.setEnabled(true);
                    }
                });
        if (mSelectedItemIndex != SELECTED_ITEM_NONE) {
            list.setItemChecked(mSelectedItemIndex, true);
            VLog.v(TAG, "List item " + mSelectedItemIndex + " should be selected");
        }
    }
}
