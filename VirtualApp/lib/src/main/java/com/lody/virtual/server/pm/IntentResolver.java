package com.lody.virtual.server.pm;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;

import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.server.pm.parser.VPackage;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class IntentResolver<F extends VPackage.IntentInfo, R extends Object> {

	private static final String TAG = "IntentResolver";

	// Sorts a List of IntentFilter objects into descending priority order.
	@SuppressWarnings("rawtypes")
	private static final Comparator sResolvePrioritySorter = new Comparator() {
		public int compare(Object o1, Object o2) {
			int q1;
			int q2;
			if (o1 instanceof IntentFilter) {
				q1 = ((IntentFilter) o1).getPriority();
				q2 = ((IntentFilter) o2).getPriority();
			} else if (o1 instanceof ResolveInfo) {
				ResolveInfo r1 = (ResolveInfo) o1;
				ResolveInfo r2 = (ResolveInfo) o2;
				q1 = r1.filter == null ? 0 : r1.filter.getPriority();
				q2 = r2.filter == null ? 0 : r2.filter.getPriority();
			} else {
				return 0;
			}
			return (q1 > q2) ? -1 : ((q1 < q2) ? 1 : 0);
		}
	};
	/**
	 * All filters that have been registered.
	 */
	private HashSet<F> mFilters = new HashSet<F>();
	/**
	 * All of the MIME types that have been registered, such as "image/jpeg",
	 * "image/*", or "{@literal *}/*".
	 */
	private HashMap<String, F[]> mTypeToFilter = new HashMap<String, F[]>();
	/**
	 * The base names of all of all fully qualified MIME types that have been
	 * registered, such as "image" or "*". Wild card MIME types such as
	 * "image/*" will not be here.
	 */
	private HashMap<String, F[]> mBaseTypeToFilter = new HashMap<String, F[]>();
	/**
	 * The base names of all of the MIME types with a sub-type wildcard that
	 * have been registered. For example, a filter with "image/*" will be
	 * included here as "image" but one with "image/jpeg" will not be included
	 * here. This also includes the "*" for the "{@literal *}/*" MIME type.
	 */
	private HashMap<String, F[]> mWildTypeToFilter = new HashMap<String, F[]>();
	/**
	 * All of the URI schemes (such as http) that have been registered.
	 */
	private HashMap<String, F[]> mSchemeToFilter = new HashMap<String, F[]>();
	/**
	 * All of the actions that have been registered, but only those that did not
	 * specify data.
	 */
	private HashMap<String, F[]> mActionToFilter = new HashMap<String, F[]>();
	/**
	 * All of the actions that have been registered and specified a MIME type.
	 */
	private HashMap<String, F[]> mTypedActionToFilter = new HashMap<String, F[]>();

	private static FastImmutableArraySet<String> getFastIntentCategories(Intent intent) {
		final Set<String> categories = intent.getCategories();
		if (categories == null) {
			return null;
		}
		return new FastImmutableArraySet<String>(categories.toArray(new String[categories.size()]));
	}

	public void addFilter(F f) {

		mFilters.add(f);
		int numS = register_intent_filter(f, f.filter.schemesIterator(), mSchemeToFilter, "      Scheme: ");
		int numT = register_mime_types(f, "      Type: ");
		if (numS == 0 && numT == 0) {
			register_intent_filter(f, f.filter.actionsIterator(), mActionToFilter, "      Action: ");
		}
		if (numT != 0) {
			register_intent_filter(f, f.filter.actionsIterator(), mTypedActionToFilter, "      TypedAction: ");
		}
	}

	private boolean filterEquals(IntentFilter f1, IntentFilter f2) {
		int s1 = f1.countActions();
		int s2 = f2.countActions();
		if (s1 != s2) {
			return false;
		}
		for (int i = 0; i < s1; i++) {
			if (!f2.hasAction(f1.getAction(i))) {
				return false;
			}
		}
		s1 = f1.countCategories();
		s2 = f2.countCategories();
		if (s1 != s2) {
			return false;
		}
		for (int i = 0; i < s1; i++) {
			if (!f2.hasCategory(f1.getCategory(i))) {
				return false;
			}
		}
		s1 = f1.countDataTypes();
		s2 = f2.countDataTypes();
		if (s1 != s2) {
			return false;
		}
		s1 = f1.countDataSchemes();
		s2 = f2.countDataSchemes();
		if (s1 != s2) {
			return false;
		}
		for (int i = 0; i < s1; i++) {
			if (!f2.hasDataScheme(f1.getDataScheme(i))) {
				return false;
			}
		}
		s1 = f1.countDataAuthorities();
		s2 = f2.countDataAuthorities();
		if (s1 != s2) {
			return false;
		}
		s1 = f1.countDataPaths();
		s2 = f2.countDataPaths();
		if (s1 != s2) {
			return false;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			s1 = f1.countDataSchemeSpecificParts();
			s2 = f2.countDataSchemeSpecificParts();
			if (s1 != s2) {
				return false;
			}
		}
		return true;
	}

	private ArrayList<F> collectFilters(F[] array, IntentFilter matching) {
		ArrayList<F> res = null;
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				F cur = array[i];
				if (cur == null) {
					break;
				}
				if (filterEquals(cur.filter, matching)) {
					if (res == null) {
						res = new ArrayList<>();
					}
					res.add(cur);
				}
			}
		}
		return res;
	}

	public ArrayList<F> findFilters(IntentFilter matching) {
		if (matching.countDataSchemes() == 1) {
			// Fast case.
			return collectFilters(mSchemeToFilter.get(matching.getDataScheme(0)), matching);
		} else if (matching.countDataTypes() != 0 && matching.countActions() == 1) {
			// Another fast case.
			return collectFilters(mTypedActionToFilter.get(matching.getAction(0)), matching);
		} else if (matching.countDataTypes() == 0 && matching.countDataSchemes() == 0 && matching.countActions() == 1) {
			// Last fast case.
			return collectFilters(mActionToFilter.get(matching.getAction(0)), matching);
		} else {
			ArrayList<F> res = null;
			for (F cur : mFilters) {
				if (filterEquals(cur.filter, matching)) {
					if (res == null) {
						res = new ArrayList<>();
					}
					res.add(cur);
				}
			}
			return res;
		}
	}

	public void removeFilter(F f) {
		removeFilterInternal(f);
		mFilters.remove(f);
	}

	void removeFilterInternal(F f) {

		int numS = unregister_intent_filter(f, f.filter.schemesIterator(), mSchemeToFilter, "      Scheme: ");
		int numT = unregister_mime_types(f, "      Type: ");
		if (numS == 0 && numT == 0) {
			unregister_intent_filter(f, f.filter.actionsIterator(), mActionToFilter, "      Action: ");
		}
		if (numT != 0) {
			unregister_intent_filter(f, f.filter.actionsIterator(), mTypedActionToFilter, "      TypedAction: ");
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

	public List<R> queryIntentFromList(Intent intent, String resolvedType, boolean defaultOnly,
			ArrayList<F[]> listCut, int userId) {
		ArrayList<R> resultList = new ArrayList<R>();
		FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
		final String scheme = intent.getScheme();
		int N = listCut.size();
		for (int i = 0; i < N; ++i) {
			buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, listCut.get(i), resultList, userId);
		}
		sortResults(resultList);
		return resultList;
	}

	public List<R> queryIntent(Intent intent, String resolvedType, boolean defaultOnly, int userId) {
		String scheme = intent.getScheme();

		ArrayList<R> finalList = new ArrayList<R>();
		F[] firstTypeCut = null;
		F[] secondTypeCut = null;
		F[] thirdTypeCut = null;
		F[] schemeCut = null;

		// If the intent includes a MIME type, then we want to collect all of
		// the filters that match that MIME type.
		if (resolvedType != null) {
			int slashpos = resolvedType.indexOf('/');
			if (slashpos > 0) {
				final String baseType = resolvedType.substring(0, slashpos);
				if (!baseType.equals("*")) {
					if (resolvedType.length() != slashpos + 2 || resolvedType.charAt(slashpos + 1) != '*') {
						// Not a wild card, so we can just look for all filters
						// that
						// completely match or wildcards whose base type
						// matches.
						firstTypeCut = mTypeToFilter.get(resolvedType);
						secondTypeCut = mWildTypeToFilter.get(baseType);
					} else {
						// We can match anything with our base type.
						firstTypeCut = mBaseTypeToFilter.get(baseType);
						secondTypeCut = mWildTypeToFilter.get(baseType);
					}
					// Any */* types always apply, but we only need to do this
					// if the intent type was not already */*.
					thirdTypeCut = mWildTypeToFilter.get("*");
				} else if (intent.getAction() != null) {
					// The intent specified any type ({@literal *}/*). This
					// can be a whole heck of a lot of things, so as a first
					// cut let's use the action instead.
					firstTypeCut = mTypedActionToFilter.get(intent.getAction());
				}
			}
		}

		// If the intent includes a data URI, then we want to collect all of
		// the filters that match its scheme (we will further refine matches
		// on the authority and path by directly matching each resulting
		// filter).
		if (scheme != null) {
			schemeCut = mSchemeToFilter.get(scheme);
		}

		// If the intent does not specify any data -- either a MIME type or
		// a URI -- then we will only be looking for matches against empty
		// data.
		if (resolvedType == null && scheme == null && intent.getAction() != null) {
			firstTypeCut = mActionToFilter.get(intent.getAction());
		}

		FastImmutableArraySet<String> categories = getFastIntentCategories(intent);
		if (firstTypeCut != null) {
			buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, firstTypeCut, finalList, userId);
		}
		if (secondTypeCut != null) {
			buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, secondTypeCut, finalList, userId);
		}
		if (thirdTypeCut != null) {
			buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, thirdTypeCut, finalList, userId);
		}
		if (schemeCut != null) {
			buildResolveList(intent, categories, defaultOnly, resolvedType, scheme, schemeCut, finalList, userId);
		}
		sortResults(finalList);
		return finalList;
	}

	/**
	 * Control whether the given filter is allowed to go into the result list.
	 * Mainly intended to prevent adding multiple filters for the same target
	 * object.
	 */
	protected boolean allowFilterResult(F filter, List<R> dest) {
		return true;
	}

	/**
	 * Returns whether the object associated with the given filter is "stopped",
	 * that is whether it should not be included in the result if the intent
	 * requests to excluded stopped objects.
	 */
	protected boolean isFilterStopped(F filter) {
		return false;
	}

	/**
	 * Returns whether this filter is owned by this package. This must be
	 * implemented to provide correct filtering of Intents that have specified a
	 * package name they are to be delivered to.
	 */
	protected abstract boolean isPackageForFilter(String packageName, F filter);

	protected abstract F[] newArray(int size);

	@SuppressWarnings("unchecked")
	protected R newResult(F filter, int match, int userId) {
		return (R) filter;
	}

	@SuppressWarnings("unchecked")
	protected void sortResults(List<R> results) {
		Collections.sort(results, sResolvePrioritySorter);
	}

	protected void dumpFilter(PrintWriter out, String prefix, F filter) {
		out.print(prefix);
		out.println(filter);
	}

	protected Object filterToLabel(F filter) {
		return "IntentFilter";
	}

	protected void dumpFilterLabel(PrintWriter out, String prefix, Object label, int count) {
		out.print(prefix);
		out.print(label);
		out.print(": ");
		out.println(count);
	}

	private void addFilter(HashMap<String, F[]> map, String name, F filter) {
		F[] array = map.get(name);
		if (array == null) {
			array = newArray(2);
			map.put(name, array);
			array[0] = filter;
		} else {
			final int N = array.length;
			int i = N;
			while (i > 0 && array[i - 1] == null) {
				i--;
			}
			if (i < N) {
				array[i] = filter;
			} else {
				F[] newa = newArray((N * 3) / 2);
				System.arraycopy(array, 0, newa, 0, N);
				newa[N] = filter;
				map.put(name, newa);
			}
		}
	}

	private int register_mime_types(F filter, String prefix) {
		final Iterator<String> i = filter.filter.typesIterator();
		if (i == null) {
			return 0;
		}

		int num = 0;
		while (i.hasNext()) {
			String name = i.next();
			num++;
			String baseName = name;
			final int slashpos = name.indexOf('/');
			if (slashpos > 0) {
				baseName = name.substring(0, slashpos).intern();
			} else {
				name = name + "/*";
			}

			addFilter(mTypeToFilter, name, filter);

			if (slashpos > 0) {
				addFilter(mBaseTypeToFilter, baseName, filter);
			} else {
				addFilter(mWildTypeToFilter, baseName, filter);
			}
		}

		return num;
	}

	private int unregister_mime_types(F filter, String prefix) {
		final Iterator<String> i = filter.filter.typesIterator();
		if (i == null) {
			return 0;
		}

		int num = 0;
		while (i.hasNext()) {
			String name = i.next();
			num++;
			String baseName = name;
			final int slashpos = name.indexOf('/');
			if (slashpos > 0) {
				baseName = name.substring(0, slashpos).intern();
			} else {
				name = name + "/*";
			}

			remove_all_objects(mTypeToFilter, name, filter);

			if (slashpos > 0) {
				remove_all_objects(mBaseTypeToFilter, baseName, filter);
			} else {
				remove_all_objects(mWildTypeToFilter, baseName, filter);
			}
		}
		return num;
	}

	private int register_intent_filter(F filter, Iterator<String> i, HashMap<String, F[]> dest, String prefix) {
		if (i == null) {
			return 0;
		}

		int num = 0;
		while (i.hasNext()) {
			String name = i.next();
			num++;
			addFilter(dest, name, filter);
		}
		return num;
	}

	private int unregister_intent_filter(F filter, Iterator<String> i, HashMap<String, F[]> dest, String prefix) {
		if (i == null) {
			return 0;
		}

		int num = 0;
		while (i.hasNext()) {
			String name = i.next();
			num++;
			remove_all_objects(dest, name, filter);
		}
		return num;
	}

	private void remove_all_objects(HashMap<String, F[]> map, String name, Object object) {
		F[] array = map.get(name);
		if (array != null) {
			int LAST = array.length - 1;
			while (LAST >= 0 && array[LAST] == null) {
				LAST--;
			}
			for (int idx = LAST; idx >= 0; idx--) {
				if (array[idx] == object) {
					final int remain = LAST - idx;
					if (remain > 0) {
						System.arraycopy(array, idx + 1, array, idx, remain);
					}
					array[LAST] = null;
					LAST--;
				}
			}
			if (LAST < 0) {
				map.remove(name);
			} else if (LAST < (array.length / 2)) {
				F[] newa = newArray(LAST + 2);
				System.arraycopy(array, 0, newa, 0, LAST + 1);
				map.put(name, newa);
			}
		}
	}

	private void buildResolveList(Intent intent, FastImmutableArraySet<String> categories,
								  boolean defaultOnly, String resolvedType, String scheme, F[] src, List<R> dest, int userId) {
		final String action = intent.getAction();
		final Uri data = intent.getData();
		final String packageName = intent.getPackage();

		final int N = src != null ? src.length : 0;
		boolean hasNonDefaults = false;
		int i;
		F filter;
		for (i = 0; i < N && (filter = src[i]) != null; i++) {
			int match;

			// Is delivery being limited to filters owned by a particular
			// package?
			if (packageName != null && !isPackageForFilter(packageName, filter)) {
				continue;
			}
			// Do we already have this one?
			if (!allowFilterResult(filter, dest)) {
				continue;
			}

			match = filter.filter.match(action, resolvedType, scheme, data, categories, TAG);
			if (match >= 0) {
				if (!defaultOnly || filter.filter.hasCategory(Intent.CATEGORY_DEFAULT)) {
					final R oneResult = newResult(filter, match, userId);
					if (oneResult != null) {
						dest.add(oneResult);
					}
				} else {
					hasNonDefaults = true;
				}
			}
		}

		if (hasNonDefaults) {
			if (dest.size() == 0) {
				VLog.w(TAG, "resolveIntent failed: found match, but none with CATEGORY_DEFAULT");
			} else if (dest.size() > 1) {
				VLog.w(TAG, "resolveIntent: multiple matches, only some with CATEGORY_DEFAULT");
			}
		}
	}

	private class IteratorWrapper implements Iterator<F> {
		private Iterator<F> mI;
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
}
