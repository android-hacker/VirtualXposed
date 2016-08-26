package com.lody.virtual.service.am;

import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import mirror.com.android.internal.R_styleable;

public class StubInfo {
	public String processName;
	public ProviderInfo providerInfo;
	/* package */ List<ActivityInfo> standardActivityInfos = new ArrayList<ActivityInfo>(1);
	/* package */ List<ActivityInfo> dialogActivityInfos = new ArrayList<ActivityInfo>(1);

	public void verify() {
		if (standardActivityInfos.isEmpty()) {
			throw new IllegalStateException("Unable to find any StubActivity in " + processName);
		}
		if (providerInfo == null) {
			throw new IllegalStateException("Unable to find any StubProvider in " + processName);
		}
	}

			/* package */ ActivityInfo fetchStubActivityInfo(ActivityInfo targetInfo) {

		// boolean isTranslucent = false;
		boolean isFloating = false;
		boolean isTranslucent = false;
		boolean showWallpaper = false;
		try {
			int[] R_Styleable_Window = R_styleable.Window.get();
			int R_Styleable_Window_windowIsTranslucent = R_styleable.Window_windowIsTranslucent.get();
			int R_Styleable_Window_windowIsFloating = R_styleable.Window_windowIsFloating.get();
			int R_Styleable_Window_windowShowWallpaper = R_styleable.Window_windowShowWallpaper.get();

			AttributeCache.Entry ent = AttributeCache.instance().get(targetInfo.packageName, targetInfo.theme,
					R_Styleable_Window);
			if (ent != null && ent.array != null) {
				showWallpaper = ent.array.getBoolean(R_Styleable_Window_windowShowWallpaper, false);
				isTranslucent = ent.array.getBoolean(R_Styleable_Window_windowIsTranslucent, false);
				isFloating = ent.array.getBoolean(R_Styleable_Window_windowIsFloating, false);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		boolean isDialogStyle = isFloating || isTranslucent || showWallpaper;
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