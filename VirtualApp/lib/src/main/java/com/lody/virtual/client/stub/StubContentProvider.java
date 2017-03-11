package com.lody.virtual.client.stub;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Process;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.compat.BundleCompat;

/**
 * @author Lody
 *
 */
public class StubContentProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		if ("_VA_|_init_process_".equals(method)) {
			return initProcess(extras);
		}
		return null;
	}

	private Bundle initProcess(Bundle extras) {
		ConditionVariable lock = VirtualCore.get().getInitLock();
		if (lock != null) {
			lock.block();
		}
		IBinder token = BundleCompat.getBinder(extras,"_VA_|_binder_");
		int vuid = extras.getInt("_VA_|_vuid_");
		VClientImpl client = VClientImpl.get();
		client.initProcess(token, vuid);
		Bundle res = new Bundle();
		BundleCompat.putBinder(res, "_VA_|_client_", client.asBinder());
		res.putInt("_VA_|_pid_", Process.myPid());
		return res;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}


	public static class C0 extends StubContentProvider {
	}

	public static class C1 extends StubContentProvider {
	}

	public static class C2 extends StubContentProvider {
	}

	public static class C3 extends StubContentProvider {
	}

	public static class C4 extends StubContentProvider {
	}

	public static class C5 extends StubContentProvider {
	}

	public static class C6 extends StubContentProvider {
	}

	public static class C7 extends StubContentProvider {
	}

	public static class C8 extends StubContentProvider {
	}

	public static class C9 extends StubContentProvider {
	}

	public static class C10 extends StubContentProvider {
	}

	public static class C11 extends StubContentProvider {
	}

	public static class C12 extends StubContentProvider {
	}

	public static class C13 extends StubContentProvider {
	}

	public static class C14 extends StubContentProvider {
	}

	public static class C15 extends StubContentProvider {
	}

	public static class C16 extends StubContentProvider {
	}

	public static class C17 extends StubContentProvider {
	}

	public static class C18 extends StubContentProvider {
	}

	public static class C19 extends StubContentProvider {
	}

	public static class C20 extends StubContentProvider {
	}

	public static class C21 extends StubContentProvider {
	}

	public static class C22 extends StubContentProvider {
	}

	public static class C23 extends StubContentProvider {
	}

	public static class C24 extends StubContentProvider {
	}

	public static class C25 extends StubContentProvider {
	}

	public static class C26 extends StubContentProvider {
	}

	public static class C27 extends StubContentProvider {
	}

	public static class C28 extends StubContentProvider {
	}

	public static class C29 extends StubContentProvider {
	}

	public static class C30 extends StubContentProvider {
	}

	public static class C31 extends StubContentProvider {
	}

	public static class C32 extends StubContentProvider {
	}

	public static class C33 extends StubContentProvider {
	}

	public static class C34 extends StubContentProvider {
	}

	public static class C35 extends StubContentProvider {
	}

	public static class C36 extends StubContentProvider {
	}

	public static class C37 extends StubContentProvider {
	}

	public static class C38 extends StubContentProvider {
	}

	public static class C39 extends StubContentProvider {
	}

	public static class C40 extends StubContentProvider {
	}

	public static class C41 extends StubContentProvider {
	}

	public static class C42 extends StubContentProvider {
	}

	public static class C43 extends StubContentProvider {
	}

	public static class C44 extends StubContentProvider {
	}

	public static class C45 extends StubContentProvider {
	}

	public static class C46 extends StubContentProvider {
	}

	public static class C47 extends StubContentProvider {
	}

	public static class C48 extends StubContentProvider {
	}

	public static class C49 extends StubContentProvider {
	}

	public static class C50 extends StubContentProvider {
	}

	public static class C51 extends StubContentProvider {
	}

	public static class C52 extends StubContentProvider {
	}

	public static class C53 extends StubContentProvider {
	}

	public static class C54 extends StubContentProvider {
	}

	public static class C55 extends StubContentProvider {
	}

	public static class C56 extends StubContentProvider {
	}

	public static class C57 extends StubContentProvider {
	}

	public static class C58 extends StubContentProvider {
	}

	public static class C59 extends StubContentProvider {
	}

	public static class C60 extends StubContentProvider {
	}

	public static class C61 extends StubContentProvider {
	}

	public static class C62 extends StubContentProvider {
	}

	public static class C63 extends StubContentProvider {
	}

	public static class C64 extends StubContentProvider {
	}

	public static class C65 extends StubContentProvider {
	}

	public static class C66 extends StubContentProvider {
	}

	public static class C67 extends StubContentProvider {
	}

	public static class C68 extends StubContentProvider {
	}

	public static class C69 extends StubContentProvider {
	}

	public static class C70 extends StubContentProvider {
	}

	public static class C71 extends StubContentProvider {
	}

	public static class C72 extends StubContentProvider {
	}

	public static class C73 extends StubContentProvider {
	}

	public static class C74 extends StubContentProvider {
	}

	public static class C75 extends StubContentProvider {
	}

	public static class C76 extends StubContentProvider {
	}

	public static class C77 extends StubContentProvider {
	}

	public static class C78 extends StubContentProvider {
	}

	public static class C79 extends StubContentProvider {
	}

	public static class C80 extends StubContentProvider {
	}

	public static class C81 extends StubContentProvider {
	}

	public static class C82 extends StubContentProvider {
	}

	public static class C83 extends StubContentProvider {
	}

	public static class C84 extends StubContentProvider {
	}

	public static class C85 extends StubContentProvider {
	}

	public static class C86 extends StubContentProvider {
	}

	public static class C87 extends StubContentProvider {
	}

	public static class C88 extends StubContentProvider {
	}

	public static class C89 extends StubContentProvider {
	}

	public static class C90 extends StubContentProvider {
	}

	public static class C91 extends StubContentProvider {
	}

	public static class C92 extends StubContentProvider {
	}

	public static class C93 extends StubContentProvider {
	}

	public static class C94 extends StubContentProvider {
	}

	public static class C95 extends StubContentProvider {
	}

	public static class C96 extends StubContentProvider {
	}

	public static class C97 extends StubContentProvider {
	}

	public static class C98 extends StubContentProvider {
	}

	public static class C99 extends StubContentProvider {
	}


}
