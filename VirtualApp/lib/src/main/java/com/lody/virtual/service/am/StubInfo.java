package com.lody.virtual.service.am;

import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.text.TextUtils;

import com.lody.virtual.helper.utils.Reflect;

import java.util.ArrayList;
import java.util.List;

public class StubInfo {
	public String processName;
	/*package*/ List<ActivityInfo> standardActivityInfos = new ArrayList<ActivityInfo>(1);
	/*package*/ List<ActivityInfo> dialogActivityInfos = new ArrayList<ActivityInfo>(1);
	public ProviderInfo providerInfo;

		public void verify() {
			if (standardActivityInfos.isEmpty()) {
				throw new IllegalStateException("Unable to find any StubActivity in " + processName);
			}
			if (providerInfo == null) {
				throw new IllegalStateException("Unable to find any StubProvider in " + processName);
			}
		}
	/*package*/ ActivityInfo fetchStubActivityInfo(ActivityInfo targetInfo) {

			boolean isTranslucent = false;
			boolean isFloating = false;
			try {
				Reflect style = Reflect.on(com.android.internal.R.styleable.class);
				int[] R_Styleable_Window = style.get("Window");
				int R_Styleable_Window_windowIsTranslucent = style.get("Window_windowIsTranslucent");
				int R_Styleable_Window_windowIsFloating = style.get("Window_windowIsFloating");

				AttributeCache.Entry ent = AttributeCache.instance().get(targetInfo.packageName, targetInfo.theme,
						R_Styleable_Window);
				if (ent != null && ent.array != null) {
					isTranslucent = ent.array.getBoolean(R_Styleable_Window_windowIsTranslucent, false);
					isFloating = ent.array.getBoolean(R_Styleable_Window_windowIsFloating, false);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}

			boolean isDialogStyle = isTranslucent || isFloating;
			if (isDialogStyle) {
				return dialogActivityInfos.get(0);
			} else {
				return standardActivityInfos.get(0);
			}
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof StubInfo && TextUtils.equals(((StubInfo) o).processName, processName);
		}

	}