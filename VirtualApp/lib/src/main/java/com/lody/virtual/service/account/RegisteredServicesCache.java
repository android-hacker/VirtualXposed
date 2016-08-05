package com.lody.virtual.service.account;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import com.android.internal.os.AtomicFile;
import com.android.internal.util.FastXmlSerializer;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.XmlSerializerAndParser;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

/**
 * A cache of registered services. This cache is built by interrogating the
 * {@link PackageManager} and is updated as packages are added, removed and
 * changed. The services are referred to by type V and are made available via
 * the {@link #getServiceInfo} method.
 * 
 * @hide
 */
public abstract class RegisteredServicesCache<V> {
	private static final String TAG = "PackageManager";

	public final Context mContext;
	private final String mInterfaceName;
	private final String mMetaDataName;
	private final String mAttributesName;
	private final XmlSerializerAndParser<V> mSerializerAndParser;
	private final AtomicReference<BroadcastReceiver> mReceiver;

	private final Object mServicesLock = new Object();
	/**
	 * This file contains the list of known services. We would like to maintain
	 * this forever so we store it as an XML file.
	 */
	private final AtomicFile mPersistentServicesFile;
	// synchronized on mServicesLock
	private HashMap<V, Integer> mPersistentServices;
	// synchronized on mServicesLock
	private Map<V, ServiceInfo<V>> mServices;
	// synchronized on mServicesLock
	private boolean mPersistentServicesFileDidNotExist;
	// the listener and handler are synchronized on "this" and must be updated
	// together
	private RegisteredServicesCacheListener<V> mListener;
	private Handler mHandler;

	public RegisteredServicesCache(Context context, String interfaceName, String metaDataName, String attributeName,
			XmlSerializerAndParser<V> serializerAndParser) {
		mContext = context;
		mInterfaceName = interfaceName;
		mMetaDataName = metaDataName;
		mAttributesName = attributeName;
		mSerializerAndParser = serializerAndParser;

		File dataDir = mContext.getDir("data", Context.MODE_PRIVATE);
		File systemDir = new File(dataDir, "system");
		File syncDir = new File(systemDir, "registered_services");
		mPersistentServicesFile = new AtomicFile(new File(syncDir, interfaceName + ".xml"));
		if (!syncDir.exists()) {
			syncDir.mkdirs();
		}

		generateServicesMap();

		final BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context1, Intent intent) {
				generateServicesMap();
			}
		};
		mReceiver = new AtomicReference<BroadcastReceiver>(receiver);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addDataScheme("package");
		mContext.registerReceiver(receiver, intentFilter);
		// Register for events related to sdcard installation.
		IntentFilter sdFilter = new IntentFilter();
		sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		mContext.registerReceiver(receiver, sdFilter);
	}

	public void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
		Map<V, ServiceInfo<V>> services;
		synchronized (mServicesLock) {
			services = mServices;
		}
		fout.println("RegisteredServicesCache: " + services.size() + " services");
		for (ServiceInfo info : services.values()) {
			fout.println("  " + info);
		}
	}

	public RegisteredServicesCacheListener<V> getListener() {
		synchronized (this) {
			return mListener;
		}
	}

	public void setListener(RegisteredServicesCacheListener<V> listener, Handler handler) {
		if (handler == null) {
			handler = new Handler(mContext.getMainLooper());
		}
		synchronized (this) {
			mHandler = handler;
			mListener = listener;
		}
	}

	private void notifyListener(final V type, final boolean removed) {
		Log.d(TAG, "notifyListener: " + type + " is " + (removed ? "removed" : "added"));
		RegisteredServicesCacheListener<V> listener;
		Handler handler;
		synchronized (this) {
			listener = mListener;
			handler = mHandler;
		}
		if (listener == null) {
			return;
		}

		final RegisteredServicesCacheListener<V> listener2 = listener;
		handler.post(new Runnable() {
			public void run() {
				listener2.onServiceChanged(type, removed);
			}
		});
	}

	/**
	 * Accessor for the registered authenticators.
	 *
	 * @param type
	 *            the account type of the authenticator
	 * @return the AuthenticatorInfo that matches the account type or null if
	 *         none is present
	 */
	public ServiceInfo<V> getServiceInfo(V type) {
		synchronized (mServicesLock) {
			return mServices.get(type);
		}
	}

	/**
	 * @return a collection of {@link RegisteredServicesCache.ServiceInfo}
	 *         objects for all registered authenticators.
	 */
	public Collection<ServiceInfo<V>> getAllServices() {
		synchronized (mServicesLock) {
			return Collections.unmodifiableCollection(mServices.values());
		}
	}

	/**
	 * Stops the monitoring of package additions, removals and changes.
	 */
	public void close() {
		final BroadcastReceiver receiver = mReceiver.getAndSet(null);
		if (receiver != null) {
			mContext.unregisterReceiver(receiver);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (mReceiver.get() != null) {
			Log.e(TAG, "RegisteredServicesCache finalized without being closed");
		}
		close();
		super.finalize();
	}

	private boolean inSystemImage(int callerUid) {
		String[] packages = mContext.getPackageManager().getPackagesForUid(callerUid);
		for (String name : packages) {
			try {
				PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(name, 0 /* flags */);
				if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
					return true;
				}
			} catch (PackageManager.NameNotFoundException e) {
				return false;
			}
		}
		return false;
	}

	void generateServicesMap() {
		PackageManager pm = mContext.getPackageManager();
		ArrayList<ServiceInfo<V>> serviceInfos = new ArrayList<ServiceInfo<V>>();
		List<ResolveInfo> resolveInfos = pm.queryIntentServices(new Intent(mInterfaceName),
				PackageManager.GET_META_DATA);

		for (ResolveInfo resolveInfo : resolveInfos) {
			try {
				ServiceInfo<V> info = parseServiceInfo(resolveInfo);
				if (info == null) {
					Log.w(TAG, "Unable to load service info " + resolveInfo.toString());
					continue;
				}
				serviceInfos.add(info);
			} catch (XmlPullParserException e) {
				Log.w(TAG, "Unable to load service info " + resolveInfo.toString(), e);
			} catch (IOException e) {
				Log.w(TAG, "Unable to load service info " + resolveInfo.toString(), e);
			}
		}

		synchronized (mServicesLock) {
			if (mPersistentServices == null) {
				readPersistentServicesLocked();
			}
			mServices = Maps.newHashMap();
			StringBuilder changes = new StringBuilder();
			for (ServiceInfo<V> info : serviceInfos) {
				// four cases:
				// - doesn't exist yet
				// - add, notify user that it was added
				// - exists and the UID is the same
				// - replace, don't notify user
				// - exists, the UID is different, and the new one is not a
				// system package
				// - ignore
				// - exists, the UID is different, and the new one is a system
				// package
				// - add, notify user that it was added
				Integer previousUid = mPersistentServices.get(info.type);
				if (previousUid == null) {
					changes.append("  New service added: ").append(info).append("\n");
					mServices.put(info.type, info);
					mPersistentServices.put(info.type, info.uid);
					if (!mPersistentServicesFileDidNotExist) {
						notifyListener(info.type, false /* removed */);
					}
				} else if (previousUid == info.uid) {
					if (Log.isLoggable(TAG, Log.VERBOSE)) {
						changes.append("  Existing service (nop): ").append(info).append("\n");
					}
					mServices.put(info.type, info);
				} else if (inSystemImage(info.uid) || !containsTypeAndUid(serviceInfos, info.type, previousUid)) {
					if (inSystemImage(info.uid)) {
						changes.append("  System service replacing existing: ").append(info).append("\n");
					} else {
						changes.append("  Existing service replacing a removed service: ").append(info).append("\n");
					}
					mServices.put(info.type, info);
					mPersistentServices.put(info.type, info.uid);
					notifyListener(info.type, false /* removed */);
				} else {
					// ignore
					changes.append("  Existing service with new uid ignored: ").append(info).append("\n");
				}
			}

			ArrayList<V> toBeRemoved = Lists.newArrayList();
			for (V v1 : mPersistentServices.keySet()) {
				if (!containsType(serviceInfos, v1)) {
					toBeRemoved.add(v1);
				}
			}
			for (V v1 : toBeRemoved) {
				mPersistentServices.remove(v1);
				changes.append("  Service removed: ").append(v1).append("\n");
				notifyListener(v1, true /* removed */);
			}
			if (changes.length() > 0) {
				Log.d(TAG, "generateServicesMap(" + mInterfaceName + "): " + serviceInfos.size() + " services:\n"
						+ changes);
				writePersistentServicesLocked();
			} else {
				Log.d(TAG,
						"generateServicesMap(" + mInterfaceName + "): " + serviceInfos.size() + " services unchanged");
			}
			mPersistentServicesFileDidNotExist = false;
		}
	}

	private boolean containsType(ArrayList<ServiceInfo<V>> serviceInfos, V type) {
		for (int i = 0, N = serviceInfos.size(); i < N; i++) {
			if (serviceInfos.get(i).type.equals(type)) {
				return true;
			}
		}

		return false;
	}

	private boolean containsTypeAndUid(ArrayList<ServiceInfo<V>> serviceInfos, V type, int uid) {
		for (int i = 0, N = serviceInfos.size(); i < N; i++) {
			final ServiceInfo<V> serviceInfo = serviceInfos.get(i);
			if (serviceInfo.type.equals(type) && serviceInfo.uid == uid) {
				return true;
			}
		}

		return false;
	}

	private ServiceInfo<V> parseServiceInfo(ResolveInfo service) throws XmlPullParserException, IOException {
		android.content.pm.ServiceInfo si = service.serviceInfo;
		ComponentName componentName = new ComponentName(si.packageName, si.name);

		PackageManager pm = mContext.getPackageManager();

		XmlResourceParser parser = null;
		try {
			parser = si.loadXmlMetaData(pm, mMetaDataName);
			if (parser == null) {
				throw new XmlPullParserException("No " + mMetaDataName + " meta-data");
			}

			AttributeSet attrs = Xml.asAttributeSet(parser);

			int type;
			while ((type = parser.next()) != XmlPullParser.END_DOCUMENT && type != XmlPullParser.START_TAG) {
			}

			String nodeName = parser.getName();
			if (!mAttributesName.equals(nodeName)) {
				throw new XmlPullParserException("Meta-data does not start with " + mAttributesName + " tag");
			}

			V v = parseServiceAttributes(pm.getResourcesForApplication(si.applicationInfo), si.packageName, attrs);
			if (v == null) {
				return null;
			}
			final android.content.pm.ServiceInfo serviceInfo = service.serviceInfo;
			final ApplicationInfo applicationInfo = serviceInfo.applicationInfo;
			final int uid = applicationInfo.uid;
			return new ServiceInfo<V>(v, componentName, uid);
		} catch (NameNotFoundException e) {
			throw new XmlPullParserException("Unable to load resources for pacakge " + si.packageName);
		} finally {
			if (parser != null)
				parser.close();
		}
	}

	/**
	 * Read all sync status back in to the initial engine state.
	 */
	private void readPersistentServicesLocked() {
		mPersistentServices = Maps.newHashMap();
		if (mSerializerAndParser == null) {
			return;
		}
		FileInputStream fis = null;
		try {
			mPersistentServicesFileDidNotExist = !mPersistentServicesFile.getBaseFile().exists();
			if (mPersistentServicesFileDidNotExist) {
				return;
			}
			fis = mPersistentServicesFile.openRead();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(fis, null);
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.START_TAG) {
				eventType = parser.next();
			}
			String tagName = parser.getName();
			if ("services".equals(tagName)) {
				eventType = parser.next();
				do {
					if (eventType == XmlPullParser.START_TAG && parser.getDepth() == 2) {
						tagName = parser.getName();
						if ("service".equals(tagName)) {
							V service = mSerializerAndParser.createFromXml(parser);
							if (service == null) {
								break;
							}
							String uidString = parser.getAttributeValue(null, "uid");
							int uid = Integer.parseInt(uidString);
							mPersistentServices.put(service, uid);
						}
					}
					eventType = parser.next();
				} while (eventType != XmlPullParser.END_DOCUMENT);
			}
		} catch (Exception e) {
			Log.w(TAG, "Error reading persistent services, starting from scratch", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (java.io.IOException e1) {
				}
			}
		}
	}

	/**
	 * Write all sync status to the sync status file.
	 */
	private void writePersistentServicesLocked() {
		if (mSerializerAndParser == null) {
			return;
		}
		FileOutputStream fos = null;
		try {
			fos = mPersistentServicesFile.startWrite();
			XmlSerializer out = new FastXmlSerializer();
			out.setOutput(fos, "utf-8");
			out.startDocument(null, true);
			out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
			out.startTag(null, "services");
			for (Map.Entry<V, Integer> service : mPersistentServices.entrySet()) {
				out.startTag(null, "service");
				out.attribute(null, "uid", Integer.toString(service.getValue()));
				mSerializerAndParser.writeAsXml(service.getKey(), out);
				out.endTag(null, "service");
			}
			out.endTag(null, "services");
			out.endDocument();
			mPersistentServicesFile.finishWrite(fos);
		} catch (java.io.IOException e1) {
			Log.w(TAG, "Error writing accounts", e1);
			if (fos != null) {
				mPersistentServicesFile.failWrite(fos);
			}
		}
	}

	public abstract V parseServiceAttributes(Resources res, String packageName, AttributeSet attrs);

	/**
	 * Value type that describes a Service. The information within can be used
	 * to bind to the service.
	 */
	public static class ServiceInfo<V> {
		public final V type;
		public final ComponentName componentName;
		public final int uid;

		/** @hide */
		public ServiceInfo(V type, ComponentName componentName, int uid) {
			this.type = type;
			this.componentName = componentName;
			this.uid = uid;
		}

		@Override
		public String toString() {
			return "ServiceInfo: " + type + ", " + componentName + ", uid " + uid;
		}
	}
}
