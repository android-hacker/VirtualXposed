package com.lody.virtual.service.am;

import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class StubInfo {
	public String processName;
	public List<ActivityInfo> standardActivityInfos = new ArrayList<ActivityInfo>(1);
	public List<ProviderInfo> providerInfos = new ArrayList<ProviderInfo>(1);

		public void verify() {
			if (standardActivityInfos.isEmpty()) {
				throw new IllegalStateException("Unable to find any StubActivity in " + processName);
			}
			if (providerInfos.isEmpty()) {
				throw new IllegalStateException("Unable to find any StubProvider in " + processName);
			}
		}
		public ActivityInfo fetchStubActivityInfo(ActivityInfo targetActInfo) {
			return standardActivityInfos.get(0);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof StubInfo && TextUtils.equals(((StubInfo) o).processName, processName);
		}

	}