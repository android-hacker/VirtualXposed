/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.lody.virtual.server.am;

import java.util.HashMap;
import java.util.WeakHashMap;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.SparseArray;

/**
 * TODO: This should be better integrated into the system so it doesn't need
 * special calls from the activity manager to clear it.
 */
public final class AttributeCache {
	private static AttributeCache sInstance = null;

	private final Context mContext;
	private final WeakHashMap<String, Package> mPackages = new WeakHashMap<String, Package>();
	private final Configuration mConfiguration = new Configuration();

	public AttributeCache(Context context) {
		mContext = context;
	}

	public static void init(Context context) {
		if (sInstance == null) {
			sInstance = new AttributeCache(context);
		}
	}

	public static AttributeCache instance() {
		return sInstance;
	}

	public void removePackage(String packageName) {
		synchronized (this) {
			mPackages.remove(packageName);
		}
	}

	public void updateConfiguration(Configuration config) {
		synchronized (this) {
			int changes = mConfiguration.updateFrom(config);
			if ((changes & ~(ActivityInfo.CONFIG_FONT_SCALE | ActivityInfo.CONFIG_KEYBOARD_HIDDEN
					| ActivityInfo.CONFIG_ORIENTATION)) != 0) {
				// The configurations being masked out are ones that commonly
				// change so we don't want flushing the cache... all others
				// will flush the cache.
				mPackages.clear();
			}
		}
	}

	public Entry get(String packageName, int resId, int[] styleable) {
		synchronized (this) {
			Package pkg = mPackages.get(packageName);
			HashMap<int[], Entry> map = null;
			Entry ent = null;
			if (pkg != null) {
				map = pkg.mMap.get(resId);
				if (map != null) {
					ent = map.get(styleable);
					if (ent != null) {
						return ent;
					}
				}
			} else {
				Context context;
				try {
					context = mContext.createPackageContext(packageName,
							Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
					if (context == null) {
						return null;
					}
				} catch (PackageManager.NameNotFoundException e) {
					return null;
				}
				pkg = new Package(context);
				mPackages.put(packageName, pkg);
			}

			if (map == null) {
				map = new HashMap<int[], Entry>();
				pkg.mMap.put(resId, map);
			}

			try {
				ent = new Entry(pkg.context, pkg.context.obtainStyledAttributes(resId, styleable));
				map.put(styleable, ent);
			} catch (Resources.NotFoundException e) {
				return null;
			}

			return ent;
		}
	}

	public final static class Package {
		public final Context context;
		private final SparseArray<HashMap<int[], Entry>> mMap = new SparseArray<HashMap<int[], Entry>>();

		public Package(Context c) {
			context = c;
		}
	}

	public final static class Entry {
		public final Context context;
		public final TypedArray array;

		public Entry(Context c, TypedArray ta) {
			context = c;
			array = ta;
		}
	}
}
