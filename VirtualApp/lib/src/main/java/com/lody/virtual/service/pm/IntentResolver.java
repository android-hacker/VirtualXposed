/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lody.virtual.service.pm;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.FastImmutableArraySet;
import android.util.Log;
import android.util.LogPrinter;
import android.util.PrintWriterPrinter;
import android.util.Printer;
import android.util.Slog;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@hide}
 */
public abstract class IntentResolver<F extends IntentFilter, R> {
    final private static String TAG = "IntentResolver";
    final private static boolean DEBUG = false;
    final private static boolean localLOGV = DEBUG;

    public void addFilter(F f) {
        if (localLOGV) {
            Slog.v(TAG, "Adding filter: " + f);
            f.dump(new LogPrinter(Log.VERBOSE, TAG, Log.LOG_ID_SYSTEM), "      ");
            Slog.v(TAG, "    Building Lookup Maps:");
        }

        mFilters.add(f);
        int numS = register_intent_filter(f, f.schemesIterator(),
                mSchemeToFilter, "      Scheme: ");
        int numT = register_mime_types(f, "      Type: ");
        if (numS == 0 && numT == 0) {
            register_intent_filter(f, f.actionsIterator(),
                    mActionToFilter, "      Action: ");
        }
        if (numT != 0) {
            register_intent_filter(f, f.actionsIterator(),
                    mTypedActionToFilter, "      TypedAction: ");
        }
    }

    public void removeFilter(F f) {
        removeFilterInternal(f);
        mFilters.remove(f);
    }

    void removeFilterInternal(F f) {
        if (localLOGV) {
            Slog.v(TAG, "Removing filter: " + f);
            f.dump(new LogPrinter(Log.VERBOSE, TAG, Log.LOG_ID_SYSTEM), "      ");
            Slog.v(TAG, "    Cleaning Lookup Maps:");
        }

        int numS = unregister_intent_filter(f, f.schemesIterator(),
                mSchemeToFilter, "      Scheme: ");
        int numT = unregister_mime_types(f, "      Type: ");
        if (numS == 0 && numT == 0) {
            unregister_intent_filter(f, f.actionsIterator(),
                    mActionToFilter, "      Action: ");
        }
        if (numT != 0) {
            unregister_intent_filter(f, f.actionsIterator(),
                    mTypedActionToFilter, "      TypedAction: ");
        }
    }

    boolean dumpMap(PrintWriter out, String titlePrefix, String title,
            String prefix, Map<String, ArrayList<F>> map, String packageName,
            boolean printFilter) {
        String eprefix = prefix + "  ";
        String fprefix = prefix + "    ";
        boolean printedSomething = false;
        Printer printer = null;
        for (Map.Entry<String, ArrayList<F>> e : map.entrySet()) {
            ArrayList<F> a = e.getValue();
            final int N = a.size();
            boolean printedHeader = false;
            for (int i=0; i<N; i++) {
                F filter = a.get(i);
                if (packageName != null && !packageName.equals(packageForFilter(filter))) {
                    continue;
                }
                if (title != null) {
                    out.print(titlePrefix); out.println(title);
                    title = null;
                }
                if (!printedHeader) {
                    out.print(eprefix); out.print(e.getKey()); out.println(":");
                    printedHeader = true;
                }
                printedSomething = true;
                dumpFilter(out, fprefix, filter);
                if (printFilter) {
                    if (printer == null) {
                        printer = new PrintWriterPrinter(out);
                    }
                    filter.dump(printer, fprefix + "  ");
                }
            }
        }
        return printedSomething;
    }

    public boolean dump(PrintWriter out, String title, String prefix, String packageName,
            boolean printFilter) {
        String innerPrefix = prefix + "  ";
        String sepPrefix = "\n" + prefix;
        String curPrefix = title + "\n" + prefix;
        if (dumpMap(out, curPrefix, "Full MIME Types:", innerPrefix,
                mTypeToFilter, packageName, printFilter)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Base MIME Types:", innerPrefix,
                mBaseTypeToFilter, packageName, printFilter)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Wild MIME Types:", innerPrefix,
                mWildTypeToFilter, packageName, printFilter)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Schemes:", innerPrefix,
                mSchemeToFilter, packageName, printFilter)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "Non-Data Actions:", innerPrefix,
                mActionToFilter, packageName, printFilter)) {
            curPrefix = sepPrefix;
        }
        if (dumpMap(out, curPrefix, "MIME Typed Actions:", innerPrefix,
                mTypedActionToFilter, packageName, printFilter)) {
            curPrefix = sepPrefix;
        }
        return curPrefix == sepPrefix;
    }

    private class IteratorWrapper implements Iterator<F> {
        private final Iterator<F> mI;
        private F mCur;

        IteratorWrapper(Iterator<F> it) {
            mI = it;
        }

        public boolean hasNext() {
            return mI.hasNext();
        }

        public F next() {
            return (mCur = mI.next());
        }

        public void remove() {
            if (mCur != null) {
                removeFilterInternal(mCur);
            }
            mI.remove();
        }

    }

    /**
     * Returns an iterator allowing filters to be removed.
     */
    public Iterator<F> filterIterator() {
        return new IteratorWrapper(mFilters.iterator());
    }

    /**
     * Returns a read-only set of the filters.
     */
    public Set<F> filterSet() {
        return Collections.unmodifiableSet(mFilters);
    }

    public List<R> queryIntentFromList(Intent intent, String resolvedType, 
            boolean defaultOnly, ArrayList<ArrayList<F>> listCut) {
        ArrayList<R> resultList = new ArrayList<R>();

        final boolean debug = localLOGV ||
                ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);

        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        final String scheme = intent.getScheme();
        int N = listCut.size();
        for (int i = 0; i < N; ++i) {
            buildResolveList(intent, categories, debug, defaultOnly,
                             resolvedType, scheme, listCut.get(i), resultList);
        }
        sortResults(resultList);
        return resultList;
    }

    public List<R> queryIntent(Intent intent, String resolvedType, boolean defaultOnly) {
        String scheme = intent.getScheme();

        ArrayList<R> finalList = new ArrayList<R>();

        final boolean debug = localLOGV ||
                ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);

        if (debug) Slog.v(
            TAG, "Resolving type " + resolvedType + " scheme " + scheme
            + " of intent " + intent);

        ArrayList<F> firstTypeCut = null;
        ArrayList<F> secondTypeCut = null;
        ArrayList<F> thirdTypeCut = null;
        ArrayList<F> schemeCut = null;

        // If the intent includes a MIME type, then we want to collect all of
        // the filters that match that MIME type.
        if (resolvedType != null) {
            int slashpos = resolvedType.indexOf('/');
            if (slashpos > 0) {
                final String baseType = resolvedType.substring(0, slashpos);
                if (!baseType.equals("*")) {
                    if (resolvedType.length() != slashpos+2
                            || resolvedType.charAt(slashpos+1) != '*') {
                        // Not a wild card, so we can just look for all filters that
                        // completely match or wildcards whose base type matches.
                        firstTypeCut = mTypeToFilter.get(resolvedType);
                        if (debug) Slog.v(TAG, "First type cut: " + firstTypeCut);
                        secondTypeCut = mWildTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "Second type cut: " + secondTypeCut);
                    } else {
                        // We can match anything with our base type.
                        firstTypeCut = mBaseTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "First type cut: " + firstTypeCut);
                        secondTypeCut = mWildTypeToFilter.get(baseType);
                        if (debug) Slog.v(TAG, "Second type cut: " + secondTypeCut);
                    }
                    // Any */* types always apply, but we only need to do this
                    // if the intent type was not already */*.
                    thirdTypeCut = mWildTypeToFilter.get("*");
                    if (debug) Slog.v(TAG, "Third type cut: " + thirdTypeCut);
                } else if (intent.getAction() != null) {
                    // The intent specified any type ({@literal *}/*).  This
                    // can be a whole heck of a lot of things, so as a first
                    // cut let's use the action instead.
                    firstTypeCut = mTypedActionToFilter.get(intent.getAction());
                    if (debug) Slog.v(TAG, "Typed Action list: " + firstTypeCut);
                }
            }
        }

        // If the intent includes a data URI, then we want to collect all of
        // the filters that match its scheme (we will further refine matches
        // on the authority and path by directly matching each resulting filter).
        if (scheme != null) {
            schemeCut = mSchemeToFilter.get(scheme);
            if (debug) Slog.v(TAG, "Scheme list: " + schemeCut);
        }

        // If the intent does not specify any data -- either a MIME type or
        // a URI -- then we will only be looking for matches against empty
        // data.
        if (resolvedType == null && scheme == null && intent.getAction() != null) {
            firstTypeCut = mActionToFilter.get(intent.getAction());
            if (debug) Slog.v(TAG, "Action list: " + firstTypeCut);
        }

        FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
        if (firstTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, firstTypeCut, finalList);
        }
        if (secondTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, secondTypeCut, finalList);
        }
        if (thirdTypeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, thirdTypeCut, finalList);
        }
        if (schemeCut != null) {
            buildResolveList(intent, categories, debug, defaultOnly,
                    resolvedType, scheme, schemeCut, finalList);
        }
        sortResults(finalList);

        if (debug) {
            Slog.v(TAG, "Final result list:");
            for (R r : finalList) {
                Slog.v(TAG, "  " + r);
            }
        }
        return finalList;
    }

    /**
     * Control whether the given filter is allowed to go into the result
     * list.  Mainly intended to prevent adding multiple filters for the
     * same target object.
     */
    protected boolean allowFilterResult(F filter, List<R> dest) {
        return true;
    }

    /**
     * Returns whether the object associated with the given filter is
     * "stopped," that is whether it should not be included in the result
     * if the intent requests to excluded stopped objects.
     */
    protected boolean isFilterStopped(F filter) {
        return false;
    }

    /**
     * Return the package that owns this filter.  This must be implemented to
     * provide correct filtering of Intents that have specified a package name
     * they are to be delivered to.
     */
    protected abstract String packageForFilter(F filter);
    
    @SuppressWarnings("unchecked")
    protected R newResult(F filter, int match) {
        return (R)filter;
    }

    @SuppressWarnings("unchecked")
    protected void sortResults(List<R> results) {
        Collections.sort(results, mResolvePrioritySorter);
    }

    protected void dumpFilter(PrintWriter out, String prefix, F filter) {
        out.print(prefix); out.println(filter);
    }

    private int register_mime_types(F filter, String prefix) {
        final Iterator<String> i = filter.typesIterator();
        if (i == null) {
            return 0;
        }

        int num = 0;
        while (i.hasNext()) {
            String name = i.next();
            num++;
            if (localLOGV) Slog.v(TAG, prefix + name);
            String baseName = name;
            final int slashpos = name.indexOf('/');
            if (slashpos > 0) {
                baseName = name.substring(0, slashpos).intern();
            } else {
                name = name + "/*";
            }

            ArrayList<F> array = mTypeToFilter.get(name);
            if (array == null) {
                //Slog.v(TAG, "Creating new array for " + name);
                array = new ArrayList<F>();
                mTypeToFilter.put(name, array);
            }
            array.add(filter);

            if (slashpos > 0) {
                array = mBaseTypeToFilter.get(baseName);
                if (array == null) {
                    //Slog.v(TAG, "Creating new array for " + name);
                    array = new ArrayList<F>();
                    mBaseTypeToFilter.put(baseName, array);
                }
                array.add(filter);
            } else {
                array = mWildTypeToFilter.get(baseName);
                if (array == null) {
                    //Slog.v(TAG, "Creating new array for " + name);
                    array = new ArrayList<F>();
                    mWildTypeToFilter.put(baseName, array);
                }
                array.add(filter);
            }
        }

        return num;
    }

    private int unregister_mime_types(F filter, String prefix) {
        final Iterator<String> i = filter.typesIterator();
        if (i == null) {
            return 0;
        }

        int num = 0;
        while (i.hasNext()) {
            String name = i.next();
            num++;
            if (localLOGV) Slog.v(TAG, prefix + name);
            String baseName = name;
            final int slashpos = name.indexOf('/');
            if (slashpos > 0) {
                baseName = name.substring(0, slashpos).intern();
            } else {
                name = name + "/*";
            }

            if (!remove_all_objects(mTypeToFilter.get(name), filter)) {
                mTypeToFilter.remove(name);
            }

            if (slashpos > 0) {
                if (!remove_all_objects(mBaseTypeToFilter.get(baseName), filter)) {
                    mBaseTypeToFilter.remove(baseName);
                }
            } else {
                if (!remove_all_objects(mWildTypeToFilter.get(baseName), filter)) {
                    mWildTypeToFilter.remove(baseName);
                }
            }
        }
        return num;
    }

    private final int register_intent_filter(F filter, Iterator<String> i,
            HashMap<String, ArrayList<F>> dest, String prefix) {
        if (i == null) {
            return 0;
        }

        int num = 0;
        while (i.hasNext()) {
            String name = i.next();
            num++;
            if (localLOGV) Slog.v(TAG, prefix + name);
            ArrayList<F> array = dest.get(name);
            if (array == null) {
                //Slog.v(TAG, "Creating new array for " + name);
                array = new ArrayList<F>();
                dest.put(name, array);
            }
            array.add(filter);
        }
        return num;
    }

    private final int unregister_intent_filter(F filter, Iterator<String> i,
            HashMap<String, ArrayList<F>> dest, String prefix) {
        if (i == null) {
            return 0;
        }

        int num = 0;
        while (i.hasNext()) {
            String name = i.next();
            num++;
            if (localLOGV) Slog.v(TAG, prefix + name);
            if (!remove_all_objects(dest.get(name), filter)) {
                dest.remove(name);
            }
        }
        return num;
    }

    private final boolean remove_all_objects(List<F> list, Object object) {
        if (list != null) {
            int N = list.size();
            for (int idx=0; idx<N; idx++) {
                if (list.get(idx) == object) {
                    list.remove(idx);
                    idx--;
                    N--;
                }
            }
            return N > 0;
        }
        return false;
    }

    private static FastImmutableArraySet<String> getFastIntentCategories(Intent intent) {
        final Set<String> categories = intent.getCategories();
        if (categories == null) {
            return null;
        }
        return new FastImmutableArraySet<String>(categories.toArray(new String[categories.size()]));
    }

    private void buildResolveList(Intent intent, FastImmutableArraySet<String> categories,
            boolean debug, boolean defaultOnly,
            String resolvedType, String scheme, List<F> src, List<R> dest) {
        final String action = intent.getAction();
        final Uri data = intent.getData();
        final String packageName = intent.getPackage();

        final boolean excludingStopped = intent.isExcludingStopped();

        final int N = src != null ? src.size() : 0;
        boolean hasNonDefaults = false;
        int i;
        for (i=0; i<N; i++) {
            F filter = src.get(i);
            int match;
            if (debug) Slog.v(TAG, "Matching against filter " + filter);

            if (excludingStopped && isFilterStopped(filter)) {
                if (debug) {
                    Slog.v(TAG, "  Filter's target is stopped; skipping");
                }
                continue;
            }

            // Is delivery being limited to filters owned by a particular package?
            if (packageName != null && !packageName.equals(packageForFilter(filter))) {
                if (debug) {
                    Slog.v(TAG, "  Filter is not from package " + packageName + "; skipping");
                }
                continue;
            }

            // Do we already have this one?
            if (!allowFilterResult(filter, dest)) {
                if (debug) {
                    Slog.v(TAG, "  Filter's target already added");
                }
                continue;
            }

            match = filter.match(action, resolvedType, scheme, data, categories, TAG);
            if (match >= 0) {
                if (debug) Slog.v(TAG, "  Filter matched!  match=0x" +
                        Integer.toHexString(match));
                if (!defaultOnly || filter.hasCategory(Intent.CATEGORY_DEFAULT)) {
                    final R oneResult = newResult(filter, match);
                    if (oneResult != null) {
                        dest.add(oneResult);
                    }
                } else {
                    hasNonDefaults = true;
                }
            } else {
                if (debug) {
                    String reason;
                    switch (match) {
                        case IntentFilter.NO_MATCH_ACTION: reason = "action"; break;
                        case IntentFilter.NO_MATCH_CATEGORY: reason = "category"; break;
                        case IntentFilter.NO_MATCH_DATA: reason = "data"; break;
                        case IntentFilter.NO_MATCH_TYPE: reason = "type"; break;
                        default: reason = "unknown reason"; break;
                    }
                    Slog.v(TAG, "  Filter did not match: " + reason);
                }
            }
        }

        if (dest.size() == 0 && hasNonDefaults) {
            Slog.w(TAG, "resolveIntent failed: found match, but none with Intent.CATEGORY_DEFAULT");
        }
    }

    // Sorts a List of IntentFilter objects into descending priority order.
    @SuppressWarnings("rawtypes")
    private static final Comparator mResolvePrioritySorter = new Comparator() {
        public int compare(Object o1, Object o2) {
            final int q1 = ((IntentFilter) o1).getPriority();
            final int q2 = ((IntentFilter) o2).getPriority();
            return (q1 > q2) ? -1 : ((q1 < q2) ? 1 : 0);
        }
    };

    /**
     * All filters that have been registered.
     */
    private final HashSet<F> mFilters = new HashSet<F>();

    /**
     * All of the MIME types that have been registered, such as "image/jpeg",
     * "image/*", or "{@literal *}/*".
     */
    private final HashMap<String, ArrayList<F>> mTypeToFilter
            = new HashMap<String, ArrayList<F>>();

    /**
     * The base names of all of all fully qualified MIME types that have been
     * registered, such as "image" or "*".  Wild card MIME types such as
     * "image/*" will not be here.
     */
    private final HashMap<String, ArrayList<F>> mBaseTypeToFilter
            = new HashMap<String, ArrayList<F>>();

    /**
     * The base names of all of the MIME types with a sub-type wildcard that
     * have been registered.  For example, a filter with "image/*" will be
     * included here as "image" but one with "image/jpeg" will not be
     * included here.  This also includes the "*" for the "{@literal *}/*"
     * MIME type.
     */
    private final HashMap<String, ArrayList<F>> mWildTypeToFilter
            = new HashMap<String, ArrayList<F>>();

    /**
     * All of the URI schemes (such as http) that have been registered.
     */
    private final HashMap<String, ArrayList<F>> mSchemeToFilter
            = new HashMap<String, ArrayList<F>>();

    /**
     * All of the actions that have been registered, but only those that did
     * not specify data.
     */
    private final HashMap<String, ArrayList<F>> mActionToFilter
            = new HashMap<String, ArrayList<F>>();

    /**
     * All of the actions that have been registered and specified a MIME type.
     */
    private final HashMap<String, ArrayList<F>> mTypedActionToFilter
            = new HashMap<String, ArrayList<F>>();
}

