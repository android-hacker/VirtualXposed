package com.lody.virtual.client.stub;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lody.virtual.R;
import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ResolverActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "ResolverActivity";
    private static final boolean DEBUG = false;
    protected Bundle mOptions;
    protected String mResultWho;
    protected int mRequestCode;
    private int mLaunchedFromUid;
    private ResolveListAdapter mAdapter;
    private PackageManager mPm;
    private boolean mAlwaysUseOption;
    private boolean mShowExtended;
    private ListView mListView;
    private Button mAlwaysButton;
    private Button mOnceButton;
    private int mIconDpi;
    private int mIconSize;
    private int mMaxColumns;
    private int mLastSelected = ListView.INVALID_POSITION;
    private AlertDialog dialog;
    private boolean mRegistered;

    private Intent makeMyIntent() {
        Intent intent = new Intent(getIntent());
        intent.setComponent(null);
        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        return intent;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final int titleResource;
        final Intent intent = makeMyIntent();
        final Set<String> categories = intent.getCategories();
        if (Intent.ACTION_MAIN.equals(intent.getAction())
                && categories != null
                && categories.size() == 1
                && categories.contains(Intent.CATEGORY_HOME)) {
            titleResource = R.string.choose;
        } else {
            titleResource = R.string.choose;
        }

        onCreate(savedInstanceState, intent, getResources().getText(titleResource),
                null, null, true, VUserHandle.getCallingUserId());
    }

    protected void onCreate(Bundle savedInstanceState, Intent intent,
                            CharSequence title, Intent[] initialIntents, List<ResolveInfo> rList,
                            boolean alwaysUseOption, int userid) {
        super.onCreate(savedInstanceState);
        mLaunchedFromUid = userid;
        mPm = getPackageManager();
        mAlwaysUseOption = alwaysUseOption;
        mMaxColumns = getResources().getInteger(R.integer.config_maxResolverActivityColumns);

        mRegistered = true;

        final ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mIconDpi = am.getLauncherLargeIconDensity();
        mIconSize = am.getLauncherLargeIconSize();

        mAdapter = new ResolveListAdapter(this, intent, initialIntents, rList,
                mLaunchedFromUid);
        int count = mAdapter.getCount();
        if (Build.VERSION.SDK_INT >= 17) {
            if (mLaunchedFromUid < 0) {
                // Gulp!
                finish();
                return;
            }
        }

        if (count == 1) {
            startSelected(0, false);
            mRegistered = false;
            finish();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        if (count > 1) {
            mListView = new ListView(this);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
            mListView.setOnItemLongClickListener(new ItemLongClickListener());
            builder.setView(mListView);
            if (alwaysUseOption) {
                mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            }
        } else {
            builder.setMessage(R.string.noApplications);
        }
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog = builder.show();
//
//        if (alwaysUseOption) {
//            final ViewGroup buttonLayout = (ViewGroup) findViewById(R.id.button_bar);
//            if (buttonLayout != null) {
//                buttonLayout.setVisibility(View.VISIBLE);
//                mAlwaysButton = (Button) buttonLayout.findViewById(R.id.button_always);
//                mOnceButton = (Button) buttonLayout.findViewById(R.id.button_once);
//            } else {
//                mAlwaysUseOption = false;
//            }
//            // Set the initial highlight if there was a preferred or last used choice
//            final int initialHighlight = mAdapter.getInitialHighlight();
//            if (initialHighlight >= 0) {
//                mListView.setItemChecked(initialHighlight, true);
//                onItemClick(null, null, initialHighlight, 0); // Other entries are not used
//            }
//        }
    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    Drawable getIcon(Resources res, int resId) {
        Drawable result;
        try {
            result = res.getDrawableForDensity(resId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            result = null;
        }

        return result;
    }

    Drawable loadIconForResolveInfo(ResolveInfo ri) {
        Drawable dr;
        try {
            if (ri.resolvePackageName != null && ri.icon != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon);
                if (dr != null) {
                    return dr;
                }
            }
            final int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            VLog.e(TAG, "Couldn't find resources for package\n" + VLog.getStackTraceString(e));
        }
        return ri.loadIcon(mPm);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!mRegistered) {
            mRegistered = true;
        }
        mAdapter.handlePackagesChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRegistered) {
            mRegistered = false;
        }
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            // This resolver is in the unusual situation where it has been
            // launched at the top of a new task.  We don't let it be added
            // to the recent tasks shown to the user, and we need to make sure
            // that each time we are launched we get the correct launching
            // uid (not re-using the same resolver from an old launching uid),
            // so we will now finish ourself since being no longer visible,
            // the user probably can't get back to us.
            if (!isChangingConfigurations()) {
                finish();
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mAlwaysUseOption) {
            final int checkedPos = mListView.getCheckedItemPosition();
            final boolean enabled = checkedPos != ListView.INVALID_POSITION;
            mLastSelected = checkedPos;
            mAlwaysButton.setEnabled(enabled);
            mOnceButton.setEnabled(enabled);
            if (enabled) {
                mListView.setSelection(checkedPos);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final int checkedPos = mListView.getCheckedItemPosition();
        final boolean hasValidSelection = checkedPos != ListView.INVALID_POSITION;
        if (mAlwaysUseOption && (!hasValidSelection || mLastSelected != checkedPos)) {
            mAlwaysButton.setEnabled(hasValidSelection);
            mOnceButton.setEnabled(hasValidSelection);
            if (hasValidSelection) {
                mListView.smoothScrollToPosition(checkedPos);
            }
            mLastSelected = checkedPos;
        } else {
            startSelected(position, false);
        }
    }

    void startSelected(int which, boolean always) {
        if (isFinishing()) {
            return;
        }
        ResolveInfo ri = mAdapter.resolveInfoForPosition(which);
        Intent intent = mAdapter.intentForPosition(which);
        onIntentSelected(ri, intent, always);
        finish();
    }

    protected void onIntentSelected(ResolveInfo ri, Intent intent, boolean alwaysCheck) {
        if (mAlwaysUseOption && mAdapter.mOrigResolveList != null) {
            // Build a reasonable intent filter, based on what matched.
            IntentFilter filter = new IntentFilter();

            if (intent.getAction() != null) {
                filter.addAction(intent.getAction());
            }
            Set<String> categories = intent.getCategories();
            if (categories != null) {
                for (String cat : categories) {
                    filter.addCategory(cat);
                }
            }
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            int cat = ri.match & IntentFilter.MATCH_CATEGORY_MASK;
            Uri data = intent.getData();
            if (cat == IntentFilter.MATCH_CATEGORY_TYPE) {
                String mimeType = intent.resolveType(this);
                if (mimeType != null) {
                    try {
                        filter.addDataType(mimeType);
                    } catch (IntentFilter.MalformedMimeTypeException e) {
                        VLog.w("ResolverActivity", "mimeType\n" + VLog.getStackTraceString(e));
                        filter = null;
                    }
                }
            }
            if (data != null && data.getScheme() != null) {
                // We need the data specification if there was no type,
                // OR if the scheme is not one of our magical "file:"
                // or "content:" schemes (see IntentFilter for the reason).
                if (cat != IntentFilter.MATCH_CATEGORY_TYPE
                        || (!"file".equals(data.getScheme())
                        && !"content".equals(data.getScheme()))) {
                    filter.addDataScheme(data.getScheme());

                    if (Build.VERSION.SDK_INT >= 19) {
                        // Look through the resolved filter to determine which part
                        // of it matched the original Intent.
                        Iterator<PatternMatcher> pIt = ri.filter.schemeSpecificPartsIterator();
                        if (pIt != null) {
                            String ssp = data.getSchemeSpecificPart();
                            while (ssp != null && pIt.hasNext()) {
                                PatternMatcher p = pIt.next();
                                if (p.match(ssp)) {
                                    filter.addDataSchemeSpecificPart(p.getPath(), p.getType());
                                    break;
                                }
                            }
                        }
                        Iterator<IntentFilter.AuthorityEntry> aIt = ri.filter.authoritiesIterator();
                        if (aIt != null) {
                            while (aIt.hasNext()) {
                                IntentFilter.AuthorityEntry a = aIt.next();
                                if (a.match(data) >= 0) {
                                    int port = a.getPort();
                                    filter.addDataAuthority(a.getHost(),
                                            port >= 0 ? Integer.toString(port) : null);
                                    break;
                                }
                            }
                        }
                        pIt = ri.filter.pathsIterator();
                        if (pIt != null) {
                            String path = data.getPath();
                            while (path != null && pIt.hasNext()) {
                                PatternMatcher p = pIt.next();
                                if (p.match(path)) {
                                    filter.addDataPath(p.getPath(), p.getType());
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (filter != null) {
                final int N = mAdapter.mOrigResolveList.size();
                ComponentName[] set = new ComponentName[N];
                int bestMatch = 0;
                for (int i = 0; i < N; i++) {
                    ResolveInfo r = mAdapter.mOrigResolveList.get(i);
                    set[i] = new ComponentName(r.activityInfo.packageName,
                            r.activityInfo.name);
                    if (r.match > bestMatch) bestMatch = r.match;
                }
                if (alwaysCheck) {
                    getPackageManager().addPreferredActivity(filter, bestMatch, set,
                            intent.getComponent());
                } else {
                    try {
                        Reflect.on(VClientImpl.get().getCurrentApplication().getPackageManager()).call("setLastChosenActivity",
                                intent,
                                intent.resolveTypeIfNeeded(getContentResolver()),
                                PackageManager.MATCH_DEFAULT_ONLY,
                                filter, bestMatch, intent.getComponent());
                    } catch (Exception re) {
                        VLog.d(TAG, "Error calling setLastChosenActivity\n" + VLog.getStackTraceString(re));
                    }
                }
            }
        }

        if (intent != null) {
            ActivityInfo info = VirtualCore.get().resolveActivityInfo(intent, mLaunchedFromUid);
            if (info == null) {
                //外面的
                startActivity(intent);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    startActivity(intent);//, mRequestCode, mOptions);
//                }else{
//                    startActivity(intent);//, mRequestCode);
//                }
            } else {
                VActivityManager.get().startActivity(intent, info, null, mOptions, mResultWho, mRequestCode, mLaunchedFromUid);
            }

        }
    }

    void showAppDetails(ResolveInfo ri) {
        Intent in = new Intent().setAction("android.settings.APPLICATION_DETAILS_SETTINGS")
                .setData(Uri.fromParts("package", ri.activityInfo.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivity(in);
    }

    static class ViewHolder {
        public TextView text;
        public TextView text2;
        public ImageView icon;

        public ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.text1);
            text2 = (TextView) view.findViewById(R.id.text2);
            icon = (ImageView) view.findViewById(R.id.icon);
        }
    }

    private final class DisplayResolveInfo {
        ResolveInfo ri;
        CharSequence displayLabel;
        Drawable displayIcon;
        CharSequence extendedInfo;
        Intent origIntent;

        DisplayResolveInfo(ResolveInfo pri, CharSequence pLabel,
                           CharSequence pInfo, Intent pOrigIntent) {
            ri = pri;
            displayLabel = pLabel;
            extendedInfo = pInfo;
            origIntent = pOrigIntent;
        }
    }

    private final class ResolveListAdapter extends BaseAdapter {
        private final Intent[] mInitialIntents;
        private final List<ResolveInfo> mBaseResolveList;
        private final Intent mIntent;
        private final int mLaunchedFromUid;
        private final LayoutInflater mInflater;
        List<DisplayResolveInfo> mList;
        List<ResolveInfo> mOrigResolveList;
        private ResolveInfo mLastChosen;
        private int mInitialHighlight = -1;

        public ResolveListAdapter(Context context, Intent intent,
                                  Intent[] initialIntents, List<ResolveInfo> rList, int launchedFromUid) {
            mIntent = new Intent(intent);
            mInitialIntents = initialIntents;
            mBaseResolveList = rList;
            mLaunchedFromUid = launchedFromUid;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mList = new ArrayList<DisplayResolveInfo>();
            rebuildList();
        }

        public void handlePackagesChanged() {
            final int oldItemCount = getCount();
            rebuildList();
            notifyDataSetChanged();
            final int newItemCount = getCount();
            if (newItemCount == 0) {
                // We no longer have any items...  just finish the activity.
                finish();
            }
        }

        public int getInitialHighlight() {
            return mInitialHighlight;
        }

        private void rebuildList() {
            List<ResolveInfo> currentResolveList;

//            try {
//                mLastChosen = AppGlobals.getPackageManager().getLastChosenActivity(
//                        mIntent, mIntent.resolveTypeIfNeeded(getContentResolver()),
//                        PackageManager.MATCH_DEFAULT_ONLY);
//            } catch (RemoteException re) {
//                Log.d(TAG, "Error calling setLastChosenActivity\n" + re);
//            }

            mList.clear();
            if (mBaseResolveList != null) {
                currentResolveList = mBaseResolveList;
                mOrigResolveList = null;
            } else {
                currentResolveList = mOrigResolveList = mPm.queryIntentActivities(
                        mIntent, PackageManager.MATCH_DEFAULT_ONLY
                                | (mAlwaysUseOption ? PackageManager.GET_RESOLVED_FILTER : 0));
                // Filter out any activities that the launched uid does not
                // have permission for.  We don't do this when we have an explicit
                // list of resolved activities, because that only happens when
                // we are being subclassed, so we can safely launch whatever
                // they gave us.
//                if (currentResolveList != null) {
//                    for (int i = currentResolveList.size() - 1; i >= 0; i--) {
//                        ActivityInfo ai = currentResolveList.get(i).activityInfo;
//                        int granted = ActivityManager.checkComponentPermission(
//                                ai.permission, mLaunchedFromUid,
//                                ai.applicationInfo.uid, ai.exported);
//                        if (granted != PackageManager.PERMISSION_GRANTED) {
//                            // Access not allowed!
//                            if (mOrigResolveList == currentResolveList) {
//                                mOrigResolveList = new ArrayList<ResolveInfo>(mOrigResolveList);
//                            }
//                            currentResolveList.remove(i);
//                        }
//                    }
//                }
            }
            int N;
            if ((currentResolveList != null) && ((N = currentResolveList.size()) > 0)) {
                // Only display the first matches that are either of equal
                // priority or have asked to be default options.
                ResolveInfo r0 = currentResolveList.get(0);
                for (int i = 1; i < N; i++) {
                    ResolveInfo ri = currentResolveList.get(i);
                    if (DEBUG) VLog.v(
                            "ResolveListActivity",
                            r0.activityInfo.name + "=" +
                                    r0.priority + "/" + r0.isDefault + " vs " +
                                    ri.activityInfo.name + "=" +
                                    ri.priority + "/" + ri.isDefault);
                    if (r0.priority != ri.priority ||
                            r0.isDefault != ri.isDefault) {
                        while (i < N) {
                            if (mOrigResolveList == currentResolveList) {
                                mOrigResolveList = new ArrayList<ResolveInfo>(mOrigResolveList);
                            }
                            currentResolveList.remove(i);
                            N--;
                        }
                    }
                }
                if (N > 1) {
                    ResolveInfo.DisplayNameComparator rComparator =
                            new ResolveInfo.DisplayNameComparator(mPm);
                    Collections.sort(currentResolveList, rComparator);
                }
                // First put the initial items at the top.
                if (mInitialIntents != null) {
                    for (int i = 0; i < mInitialIntents.length; i++) {
                        Intent ii = mInitialIntents[i];
                        if (ii == null) {
                            continue;
                        }
                        ActivityInfo ai = ii.resolveActivityInfo(
                                getPackageManager(), 0);
                        if (ai == null) {
                            VLog.w("ResolverActivity", "No activity found for "
                                    + ii);
                            continue;
                        }
                        ResolveInfo ri = new ResolveInfo();
                        ri.activityInfo = ai;
                        if (ii instanceof LabeledIntent) {
                            LabeledIntent li = (LabeledIntent) ii;
                            ri.resolvePackageName = li.getSourcePackage();
                            ri.labelRes = li.getLabelResource();
                            ri.nonLocalizedLabel = li.getNonLocalizedLabel();
                            ri.icon = li.getIconResource();
                        }
                        mList.add(new DisplayResolveInfo(ri,
                                ri.loadLabel(getPackageManager()), null, ii));
                    }
                }

                // Check for applications with same name and use application name or
                // package name if necessary
                r0 = currentResolveList.get(0);
                int start = 0;
                CharSequence r0Label = r0.loadLabel(mPm);
                mShowExtended = false;
                for (int i = 1; i < N; i++) {
                    if (r0Label == null) {
                        r0Label = r0.activityInfo.packageName;
                    }
                    ResolveInfo ri = currentResolveList.get(i);
                    CharSequence riLabel = ri.loadLabel(mPm);
                    if (riLabel == null) {
                        riLabel = ri.activityInfo.packageName;
                    }
                    if (riLabel.equals(r0Label)) {
                        continue;
                    }
                    processGroup(currentResolveList, start, (i - 1), r0, r0Label);
                    r0 = ri;
                    r0Label = riLabel;
                    start = i;
                }
                // Process last group
                processGroup(currentResolveList, start, (N - 1), r0, r0Label);
            }
        }

        private void processGroup(List<ResolveInfo> rList, int start, int end, ResolveInfo ro,
                                  CharSequence roLabel) {
            // Process labels from start to i
            int num = end - start + 1;
            if (num == 1) {
                if (mLastChosen != null
                        && mLastChosen.activityInfo.packageName.equals(
                        ro.activityInfo.packageName)
                        && mLastChosen.activityInfo.name.equals(ro.activityInfo.name)) {
                    mInitialHighlight = mList.size();
                }
                // No duplicate labels. Use label for entry at start
                mList.add(new DisplayResolveInfo(ro, roLabel, null, null));
            } else {
                mShowExtended = true;
                boolean usePkg = false;
                CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(mPm);
                if (startApp == null) {
                    usePkg = true;
                }
                if (!usePkg) {
                    // Use HashSet to track duplicates
                    HashSet<CharSequence> duplicates =
                            new HashSet<CharSequence>();
                    duplicates.add(startApp);
                    for (int j = start + 1; j <= end; j++) {
                        ResolveInfo jRi = rList.get(j);
                        CharSequence jApp = jRi.activityInfo.applicationInfo.loadLabel(mPm);
                        if ((jApp == null) || (duplicates.contains(jApp))) {
                            usePkg = true;
                            break;
                        } else {
                            duplicates.add(jApp);
                        }
                    }
                    // Clear HashSet for later use
                    duplicates.clear();
                }
                for (int k = start; k <= end; k++) {
                    ResolveInfo add = rList.get(k);
                    if (mLastChosen != null
                            && mLastChosen.activityInfo.packageName.equals(
                            add.activityInfo.packageName)
                            && mLastChosen.activityInfo.name.equals(add.activityInfo.name)) {
                        mInitialHighlight = mList.size();
                    }
                    if (usePkg) {
                        // Use application name for all entries from start to end-1
                        mList.add(new DisplayResolveInfo(add, roLabel,
                                add.activityInfo.packageName, null));
                    } else {
                        // Use package name for all entries from start to end-1
                        mList.add(new DisplayResolveInfo(add, roLabel,
                                add.activityInfo.applicationInfo.loadLabel(mPm), null));
                    }
                }
            }
        }

        public ResolveInfo resolveInfoForPosition(int position) {
            return mList.get(position).ri;
        }

        public Intent intentForPosition(int position) {
            DisplayResolveInfo dri = mList.get(position);

            Intent intent = new Intent(dri.origIntent != null
                    ? dri.origIntent : mIntent);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                    | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            ActivityInfo ai = dri.ri.activityInfo;
            intent.setComponent(new ComponentName(
                    ai.applicationInfo.packageName, ai.name));
            return intent;
        }

        public int getCount() {
            return mList.size();
        }

        public Object getItem(int position) {
            return mList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.resolve_list_item, parent, false);

                final ViewHolder holder = new ViewHolder(view);
                view.setTag(holder);

                // Fix the icon size even if we have different sized resources
                ViewGroup.LayoutParams lp = holder.icon.getLayoutParams();
                lp.width = lp.height = mIconSize;
            } else {
                view = convertView;
            }
            bindView(view, mList.get(position));
            return view;
        }

        private final void bindView(View view, DisplayResolveInfo info) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            holder.text.setText(info.displayLabel);
            if (mShowExtended) {
                holder.text2.setVisibility(View.VISIBLE);
                holder.text2.setText(info.extendedInfo);
            } else {
                holder.text2.setVisibility(View.GONE);
            }
            if (info.displayIcon == null) {
                new LoadIconTask().execute(info);
            }
            holder.icon.setImageDrawable(info.displayIcon);
        }
    }

    class ItemLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            ResolveInfo ri = mAdapter.resolveInfoForPosition(position);
            showAppDetails(ri);
            return true;
        }

    }

    class LoadIconTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        @Override
        protected DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            final DisplayResolveInfo info = params[0];
            if (info.displayIcon == null) {
                info.displayIcon = loadIconForResolveInfo(info.ri);
            }
            return info;
        }

        @Override
        protected void onPostExecute(DisplayResolveInfo info) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
