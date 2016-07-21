package com.lody.virtual.helper.bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

public class IntentResolver {

	private static Comparator<ResolveInfo> sResolvePrioritySorter = new Comparator<ResolveInfo>() {
		public int compare(ResolveInfo r1, ResolveInfo r2) {
			int v1 = r1.priority;
			int v2 = r2.priority;
			if (v1 != v2) {
				return (v1 > v2) ? -1 : 1;
			}
			v1 = r1.preferredOrder;
			v2 = r2.preferredOrder;
			if (v1 != v2) {
				return (v1 > v2) ? -1 : 1;
			}
			if (r1.isDefault != r2.isDefault) {
				return r1.isDefault ? -1 : 1;
			}
			v1 = r1.match;
			v2 = r2.match;
			if (v1 != v2) {
				return (v1 > v2) ? -1 : 1;
			}
			return 0;
		}
	};

	public static List<ResolveInfo> resolveReceiverIntent(Context context, Map<String, APKBundle> pluginPackages,
			Intent intent, String resolvedType, int flags) throws Exception {
		if (intent == null || context == null) {
			return null;
		}
		List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();

				}
			}
		}

		if (comp != null && comp.getPackageName() != null) {
			APKBundle parser = pluginPackages.get(comp.getPackageName());
			if (parser != null) {
				queryIntentReceiverForPackage(context, parser, intent, flags, list);
				if (list.size() > 0) {
					Collections.sort(list, sResolvePrioritySorter);
					return list;
				}
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
			Collections.sort(list, sResolvePrioritySorter);
			return list;
		}

		final String pkgName = intent.getPackage();
		if (pkgName != null) {
			APKBundle parser = pluginPackages.get(pkgName);
			if (parser != null) {
				queryIntentReceiverForPackage(context, parser, intent, flags, list);
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
		} else {
			for (APKBundle parser : pluginPackages.values()) {
				queryIntentReceiverForPackage(context, parser, intent, flags, list);
			}

		}
		Collections.sort(list, sResolvePrioritySorter);
		return list;
	}

	public static List<ResolveInfo> resolveServiceIntent(Context context, Map<String, APKBundle> pluginPackages,
			Intent intent, String resolvedType, int flags) throws Exception {
		if (intent == null || context == null) {
			return null;
		}
		List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();

				}
			}
		}

		if (comp != null && comp.getPackageName() != null) {
			APKBundle parser = pluginPackages.get(comp.getPackageName());
			if (parser != null) {

				queryIntentServiceForPackage(context, parser, intent, flags, list);
				if (list.size() > 0) {
					Collections.sort(list, sResolvePrioritySorter);
					return list;
				}

			} else {
				// intent指定的包名不在我们的插件列表中。
			}
			Collections.sort(list, sResolvePrioritySorter);
			return list;
		}

		final String pkgName = intent.getPackage();
		if (pkgName != null) {
			APKBundle parser = pluginPackages.get(pkgName);
			if (parser != null) {
				queryIntentServiceForPackage(context, parser, intent, flags, list);
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
		} else {
			for (APKBundle parser : pluginPackages.values()) {
				queryIntentServiceForPackage(context, parser, intent, flags, list);
			}
		}
		Collections.sort(list, sResolvePrioritySorter);
		return list;
	}

	public static List<ResolveInfo> resolveProviderIntent(Context context, Map<String, APKBundle> pluginPackages,
			Intent intent, String resolvedType, int flags) throws Exception {
		if (intent == null || context == null) {
			return null;
		}
		List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();

				}
			}
		}

		if (comp != null && comp.getPackageName() != null) {
			APKBundle parser = pluginPackages.get(comp.getPackageName());
			if (parser != null) {
				queryIntentProviderForPackage(context, parser, intent, flags, list);
				if (list.size() > 0) {
					Collections.sort(list, sResolvePrioritySorter);
					return list;
				}
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
			Collections.sort(list, sResolvePrioritySorter);
			return list;
		}

		final String pkgName = intent.getPackage();
		if (pkgName != null) {
			APKBundle parser = pluginPackages.get(pkgName);
			if (parser != null) {
				queryIntentProviderForPackage(context, parser, intent, flags, list);
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
		} else {
			for (APKBundle parser : pluginPackages.values()) {
				queryIntentProviderForPackage(context, parser, intent, flags, list);
			}

		}
		Collections.sort(list, sResolvePrioritySorter);
		return list;
	}

	public static List<ResolveInfo> resolveActivityIntent(Context context, Map<String, APKBundle> pluginPackages,
			Intent intent, String resolvedType, int flags) throws Exception {
		if (intent == null || context == null) {
			return null;
		}
		List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();

				}
			}
		}

		if (comp != null && comp.getPackageName() != null) {
			APKBundle parser = pluginPackages.get(comp.getPackageName());
			if (parser != null) {
				queryIntentActivityForPackage(context, parser, intent, flags, list);
				if (list.size() > 0) {
					Collections.sort(list, sResolvePrioritySorter);
					return list;
				}
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
			Collections.sort(list, sResolvePrioritySorter);
			return list;
		}

		final String pkgName = intent.getPackage();
		if (pkgName != null) {
			APKBundle parser = pluginPackages.get(pkgName);
			if (parser != null) {
				queryIntentActivityForPackage(context, parser, intent, flags, list);
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
		} else {
			for (APKBundle parser : pluginPackages.values()) {
				queryIntentActivityForPackage(context, parser, intent, flags, list);
			}

		}
		Collections.sort(list, sResolvePrioritySorter);
		return list;
	}

	public static List<ResolveInfo> resolveIntent(Context context, Map<String, APKBundle> pluginPackages, Intent intent,
			String resolvedType, int flags) throws Exception {
		if (intent == null || context == null) {
			return null;
		}
		List<ResolveInfo> list = new ArrayList<ResolveInfo>(1);
		ComponentName comp = intent.getComponent();
		if (comp == null) {
			if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				if (intent.getSelector() != null) {
					intent = intent.getSelector();
					comp = intent.getComponent();

				}
			}
		}

		if (comp != null && comp.getPackageName() != null) {
			APKBundle parser = pluginPackages.get(comp.getPackageName());
			if (parser != null) {
				queryIntentActivityForPackage(context, parser, intent, flags, list);
				if (list.size() > 0) {
					Collections.sort(list, sResolvePrioritySorter);
					return list;
				}
				queryIntentServiceForPackage(context, parser, intent, flags, list);
				if (list.size() > 0) {
					Collections.sort(list, sResolvePrioritySorter);
					return list;
				}
				queryIntentProviderForPackage(context, parser, intent, flags, list);
				if (list.size() > 0) {
					Collections.sort(list, sResolvePrioritySorter);
					return list;
				}
				queryIntentReceiverForPackage(context, parser, intent, flags, list);
				if (list.size() > 0) {
					Collections.sort(list, sResolvePrioritySorter);
					return list;
				}
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
			Collections.sort(list, sResolvePrioritySorter);
			return list;
		}

		final String pkgName = intent.getPackage();
		if (pkgName != null) {
			APKBundle parser = pluginPackages.get(pkgName);
			if (parser != null) {
				queryIntentActivityForPackage(context, parser, intent, flags, list);
				queryIntentServiceForPackage(context, parser, intent, flags, list);
				queryIntentProviderForPackage(context, parser, intent, flags, list);
				queryIntentReceiverForPackage(context, parser, intent, flags, list);
			} else {
				// intent指定的包名不在我们的插件列表中。
			}
		} else {
			for (APKBundle parser : pluginPackages.values()) {
				queryIntentActivityForPackage(context, parser, intent, flags, list);
				queryIntentServiceForPackage(context, parser, intent, flags, list);
				queryIntentProviderForPackage(context, parser, intent, flags, list);
				queryIntentReceiverForPackage(context, parser, intent, flags, list);
			}

		}
		Collections.sort(list, sResolvePrioritySorter);
		return list;
	}

	private static void queryIntentReceiverForPackage(Context context, APKBundle packageParser, Intent intent,
			int flags, List<ResolveInfo> outList) throws Exception {
		List<ActivityInfo> receivers = packageParser.getReceivers();
		if (receivers != null && receivers.size() >= 0) {
			for (ActivityInfo receiver : receivers) {

				ComponentName className = new ComponentName(receiver.packageName, receiver.name);
				if (intent != null && intent.getComponent() != null) {
					ComponentName cn = intent.getComponent();
					if (className.equals(cn)) {
						ActivityInfo flagProviderInfo = packageParser
								.getReceiverInfo(new ComponentName(receiver.packageName, receiver.name), flags);
						ResolveInfo resolveInfo = newResolveInfo(flagProviderInfo, new IntentFilter());
						if (VERSION.SDK_INT >= VERSION_CODES.M) {
							resolveInfo.match = PackageManager.MATCH_ALL;
						}
						resolveInfo.isDefault = true;
						outList.add(resolveInfo);
					}
				}

				List<IntentFilter> intentFilters = packageParser.getReceiverIntentFilter(receiver);
				if (intentFilters != null && intentFilters.size() > 0) {
					for (IntentFilter intentFilter : intentFilters) {
						int match = intentFilter.match(context.getContentResolver(), intent, true, "");
						if (match >= 0) {
							ActivityInfo flagInfo = packageParser
									.getReceiverInfo(new ComponentName(receiver.packageName, receiver.name), flags);
							if ((flags & PackageManager.MATCH_DEFAULT_ONLY) != 0) {
								if (intentFilter.hasCategory(Intent.CATEGORY_DEFAULT)) {
									ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
									resolveInfo.match = match;
									resolveInfo.isDefault = true;
									outList.add(resolveInfo);
								} else {
									// 只是匹配默认。这里也算匹配不上。
								}
							} else {
								ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
								resolveInfo.match = match;
								resolveInfo.isDefault = false;
								outList.add(resolveInfo);
							}
						}
					}
					if (outList.size() <= 0) {
						// 没有在插件包中找到IntentFilter匹配的ACTIVITY
					}
				} else {
					// 该插件包中没有具有IntentFilter的ACTIVITY
				}
			}
		} else {
			// 该插件apk包中没有ACTIVITY
		}
	}

	private static void queryIntentProviderForPackage(Context context, APKBundle packageParser, Intent intent,
			int flags, List<ResolveInfo> outList) throws Exception {
		List<ProviderInfo> providerInfos = packageParser.getProviders();
		if (providerInfos != null && providerInfos.size() >= 0) {
			for (ProviderInfo providerInfo : providerInfos) {
				ComponentName className = new ComponentName(providerInfo.packageName, providerInfo.name);

				if (intent != null && intent.getComponent() != null) {
					ComponentName cn = intent.getComponent();
					if (className.equals(cn)) {
						ProviderInfo flagProviderInfo = packageParser
								.getProviderInfo(new ComponentName(providerInfo.packageName, providerInfo.name), flags);
						ResolveInfo resolveInfo = newResolveInfo(flagProviderInfo, new IntentFilter());
						if (VERSION.SDK_INT >= VERSION_CODES.M) {
							resolveInfo.match = PackageManager.MATCH_ALL;
						}
						resolveInfo.isDefault = true;
						outList.add(resolveInfo);
					}
				}

				List<IntentFilter> intentFilters = packageParser.getProviderIntentFilter(className);
				if (intentFilters != null && intentFilters.size() > 0) {
					for (IntentFilter intentFilter : intentFilters) {
						int match = intentFilter.match(context.getContentResolver(), intent, true, "");
						if (match >= 0) {
							ProviderInfo flagInfo = packageParser.getProviderInfo(
									new ComponentName(providerInfo.packageName, providerInfo.name), flags);
							if ((flags & PackageManager.MATCH_DEFAULT_ONLY) != 0) {
								if (intentFilter.hasCategory(Intent.CATEGORY_DEFAULT)) {
									ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
									resolveInfo.match = match;
									resolveInfo.isDefault = true;
									outList.add(resolveInfo);
								} else {
									// 只是匹配默认。这里也算匹配不上。
								}
							} else {
								ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
								resolveInfo.match = match;
								resolveInfo.isDefault = false;
								outList.add(resolveInfo);
							}
						}
					}
					if (outList.size() <= 0) {
						// 没有在插件包中找到IntentFilter匹配的Service
					}
				} else {
					// 该插件包中没有具有IntentFilter的Service
				}
			}
		} else {
			// 该插件apk包中没有Service
		}
	}

	private static void queryIntentServiceForPackage(Context context, APKBundle packageParser, Intent intent, int flags,
			List<ResolveInfo> outList) throws Exception {
		List<ServiceInfo> serviceInfos = packageParser.getServices();
		if (serviceInfos != null && serviceInfos.size() >= 0) {
			for (ServiceInfo serviceInfo : serviceInfos) {
				ComponentName className = new ComponentName(serviceInfo.packageName, serviceInfo.name);

				if (intent != null && intent.getComponent() != null) {
					ComponentName cn = intent.getComponent();
					if (className.equals(cn)) {
						ServiceInfo flagServiceInfo = packageParser
								.getServiceInfo(new ComponentName(serviceInfo.packageName, serviceInfo.name), flags);
						ResolveInfo resolveInfo = newResolveInfo(flagServiceInfo, new IntentFilter());
						if (VERSION.SDK_INT >= VERSION_CODES.M) {
							resolveInfo.match = PackageManager.MATCH_ALL;
						}
						resolveInfo.isDefault = true;
						outList.add(resolveInfo);
					}
				}

				List<IntentFilter> intentFilters = packageParser.getServiceIntentFilter(className);
				if (intentFilters != null && intentFilters.size() > 0) {
					for (IntentFilter intentFilter : intentFilters) {
						int match = intentFilter.match(context.getContentResolver(), intent, true, "");
						if (match >= 0) {
							ServiceInfo flagServiceInfo = packageParser.getServiceInfo(
									new ComponentName(serviceInfo.packageName, serviceInfo.name), flags);
							if ((flags & PackageManager.MATCH_DEFAULT_ONLY) != 0) {
								if (intentFilter.hasCategory(Intent.CATEGORY_DEFAULT)) {
									ResolveInfo resolveInfo = newResolveInfo(flagServiceInfo, intentFilter);
									resolveInfo.match = match;
									resolveInfo.isDefault = true;
									outList.add(resolveInfo);
								} else {
									// 只是匹配默认。这里也算匹配不上。
								}
							} else {
								ResolveInfo resolveInfo = newResolveInfo(flagServiceInfo, intentFilter);
								resolveInfo.match = match;
								resolveInfo.isDefault = false;
								outList.add(resolveInfo);
							}
						}
					}
					if (outList.size() <= 0) {
						// 没有在插件包中找到IntentFilter匹配的Service
					}
				} else {
					// 该插件包中没有具有IntentFilter的Service
				}
			}
		} else {
			// 该插件apk包中没有Service
		}
	}

	private static void queryIntentActivityForPackage(Context context, APKBundle packageParser, Intent intent,
			int flags, List<ResolveInfo> outList) throws Exception {
		List<ActivityInfo> activityInfos = packageParser.getActivities();
		if (activityInfos != null && activityInfos.size() >= 0) {
			for (ActivityInfo activityInfo : activityInfos) {
				ComponentName className = new ComponentName(activityInfo.packageName, activityInfo.name);

				if (intent != null && intent.getComponent() != null) {
					ComponentName cn = intent.getComponent();
					if (className.equals(cn)) {
						ActivityInfo flagActivityInfo = packageParser
								.getActivityInfo(new ComponentName(activityInfo.packageName, activityInfo.name), flags);
						ResolveInfo resolveInfo = newResolveInfo(flagActivityInfo, new IntentFilter());
						if (VERSION.SDK_INT >= VERSION_CODES.M) {
							resolveInfo.match = PackageManager.MATCH_ALL;
						}
						resolveInfo.isDefault = true;
						outList.add(resolveInfo);
					}
				}

				List<IntentFilter> intentFilters = packageParser.getActivityIntentFilter(className);
				if (intentFilters != null && intentFilters.size() > 0) {
					for (IntentFilter intentFilter : intentFilters) {
						int match = intentFilter.match(context.getContentResolver(), intent, true, "");
						if (match >= 0) {
							ActivityInfo flagInfo = packageParser.getActivityInfo(
									new ComponentName(activityInfo.packageName, activityInfo.name), flags);
							if ((flags & PackageManager.MATCH_DEFAULT_ONLY) != 0) {
								if (intentFilter.hasCategory(Intent.CATEGORY_DEFAULT)) {
									ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
									resolveInfo.match = match;
									resolveInfo.isDefault = true;
									outList.add(resolveInfo);
								} else {
									// 只是匹配默认。这里也算匹配不上。
								}
							} else {
								ResolveInfo resolveInfo = newResolveInfo(flagInfo, intentFilter);
								resolveInfo.match = match;
								resolveInfo.isDefault = false;
								outList.add(resolveInfo);
							}
						}
					}
					if (outList.size() <= 0) {
						// 没有在插件包中找到IntentFilter匹配的ACTIVITY
					}
				} else {
					// 该插件包中没有具有IntentFilter的ACTIVITY
				}
			}
		} else {
			// 该插件apk包中没有ACTIVITY
		}
	}

	@TargetApi(VERSION_CODES.KITKAT)
	private static ResolveInfo newResolveInfo(ProviderInfo providerInfo, IntentFilter intentFilter) {
		ResolveInfo resolveInfo = new ResolveInfo();
		resolveInfo.providerInfo = providerInfo;
		resolveInfo.filter = intentFilter;
		resolveInfo.resolvePackageName = providerInfo.packageName;
		resolveInfo.labelRes = providerInfo.labelRes;
		resolveInfo.icon = providerInfo.icon;
		resolveInfo.specificIndex = 1;
		resolveInfo.priority = intentFilter.getPriority();
		resolveInfo.preferredOrder = 0;
		return resolveInfo;
	}

	private static ResolveInfo newResolveInfo(ServiceInfo serviceInfo, IntentFilter intentFilter) {
		ResolveInfo resolveInfo = new ResolveInfo();
		resolveInfo.serviceInfo = serviceInfo;
		resolveInfo.filter = intentFilter;
		resolveInfo.resolvePackageName = serviceInfo.packageName;
		resolveInfo.labelRes = serviceInfo.labelRes;
		resolveInfo.icon = serviceInfo.icon;
		resolveInfo.specificIndex = 1;
		resolveInfo.priority = intentFilter.getPriority();
		resolveInfo.preferredOrder = 0;
		return resolveInfo;
	}

	private static ResolveInfo newResolveInfo(ActivityInfo activityInfo, IntentFilter intentFilter) {
		ResolveInfo resolveInfo = new ResolveInfo();
		resolveInfo.activityInfo = activityInfo;
		resolveInfo.filter = intentFilter;
		resolveInfo.resolvePackageName = activityInfo.packageName;
		resolveInfo.labelRes = activityInfo.labelRes;
		resolveInfo.icon = activityInfo.icon;
		resolveInfo.specificIndex = 1;
		resolveInfo.priority = intentFilter.getPriority();
		resolveInfo.preferredOrder = 0;
		return resolveInfo;
	}

	public static ResolveInfo findBest(List<ResolveInfo> infos) {
		return infos.get(0);
	}
}