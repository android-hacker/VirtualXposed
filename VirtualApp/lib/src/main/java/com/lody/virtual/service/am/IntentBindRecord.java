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

package com.lody.virtual.service.am;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Intent;
import android.os.IBinder;

/**
 * A particular Intent that has been bound to a Service.
 */
class IntentBindRecord {
	/** The running service. */
	final ServiceRecord service;
	/** The intent that is bound. */
	final Intent.FilterComparison intent; //
	/** All apps that have bound to this Intent. */
	final HashMap<ProcessRecord, AppBindRecord> apps = new HashMap<ProcessRecord, AppBindRecord>();
	/** Binder published from service. */
	IBinder binder;
	/** Set when we have initiated a request for this binder. */
	boolean requested;
	/** Set when we have received the requested binder. */
	boolean received;
	/** Set when we still need to tell the service all clients are unbound. */
	boolean hasBound;
	/**
	 * Set when the service's onUnbind() has asked to be told about new clients.
	 */
	boolean doRebind;

	String stringName; // caching of toString

	IntentBindRecord(ServiceRecord _service, Intent.FilterComparison _intent) {
		service = _service;
		intent = _intent;
	}

	void dump(PrintWriter pw, String prefix) {
		pw.print(prefix);
		pw.print("service=");
		pw.println(service);
		dumpInService(pw, prefix);
	}

	void dumpInService(PrintWriter pw, String prefix) {
		pw.print(prefix);
		pw.print("intent={");
		pw.print(intent.getIntent().toString());
		pw.println('}');
		pw.print(prefix);
		pw.print("binder=");
		pw.println(binder);
		pw.print(prefix);
		pw.print("requested=");
		pw.print(requested);
		pw.print(" received=");
		pw.print(received);
		pw.print(" hasBound=");
		pw.print(hasBound);
		pw.print(" doRebind=");
		pw.println(doRebind);
		if (apps.size() > 0) {
			Iterator<AppBindRecord> it = apps.values().iterator();
			while (it.hasNext()) {
				AppBindRecord a = it.next();
				pw.print(prefix);
				pw.print("* Client AppBindRecord{");
				pw.print(Integer.toHexString(System.identityHashCode(a)));
				pw.print(' ');
				pw.print(a.client);
				pw.println('}');
				a.dumpInIntentBind(pw, prefix + "  ");
			}
		}
	}

	public String toString() {
		if (stringName != null) {
			return stringName;
		}
		StringBuilder sb = new StringBuilder(128);
		sb.append("IntentBindRecord{");
		sb.append(Integer.toHexString(System.identityHashCode(this)));
		sb.append(' ');
		sb.append(service.shortName);
		sb.append(':');
		sb.append('}');
		return stringName = sb.toString();
	}
}
