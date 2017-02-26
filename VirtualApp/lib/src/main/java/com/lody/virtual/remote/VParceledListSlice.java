package com.lody.virtual.remote;

import java.util.ArrayList;
import java.util.List;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

/**
 * Transfer a large list of Parcelable objects across an IPC. Splits into
 * multiple transactions if needed.
 *
 * Caveat: for efficiency and security, all elements must be the same concrete
 * type. In order to avoid writing the class name of each object, we must ensure
 * that each object is the same type, or else unparceling then reparceling the
 * data may yield a different result if the class name encoded in the Parcelable
 * is a Base type. See b/17671747.
 *
 * @hide
 *
 * 		OpenSilk: Modified to remove the creator optimization which uses hidden
 *       apis. this means we write an extra string for every list item. Class
 *       verification left in place since the Base type issue still applies
 */
public class VParceledListSlice<T extends Parcelable> implements Parcelable {
	@SuppressWarnings("unchecked")
	public static final Parcelable.ClassLoaderCreator<VParceledListSlice> CREATOR = new Parcelable.ClassLoaderCreator<VParceledListSlice>() {
		public VParceledListSlice createFromParcel(Parcel in) {
			return new VParceledListSlice(in, null);
		}

		@Override
		public VParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
			return new VParceledListSlice(in, loader);
		}

		public VParceledListSlice[] newArray(int size) {
			return new VParceledListSlice[size];
		}
	};
	/*
	 * TODO get this number from somewhere else. For now set it to a quarter of
	 * the 1MB limit.
	 */
	private static final int MAX_IPC_SIZE = 256 * 1024;
	private static final int MAX_FIRST_IPC_SIZE = MAX_IPC_SIZE / 2;
	private static String TAG = "ParceledListSlice";
	private static boolean DEBUG = false;
	private final List<T> mList;

	public VParceledListSlice(List<T> list) {
		mList = list;
	}

	private VParceledListSlice(Parcel p, ClassLoader loader) {
		final int N = p.readInt();
		mList = new ArrayList<T>(N);
		if (DEBUG)
			Log.d(TAG, "Retrieving " + N + " items");
		if (N <= 0) {
			return;
		}

		// Parcelable.Creator<T> creator = p.readParcelableCreator(loader);
		Class<?> listElementClass = null;

		int i = 0;
		while (i < N) {
			if (p.readInt() == 0) {
				break;
			}

			// final T parcelable = p.readCreator(creator, loader);
			final T parcelable = p.readParcelable(loader);
			if (listElementClass == null) {
				listElementClass = parcelable.getClass();
			} else {
				verifySameType(listElementClass, parcelable.getClass());
			}

			mList.add(parcelable);

			if (DEBUG)
				Log.d(TAG, "Read inline #" + i + ": " + mList.get(mList.size() - 1));
			i++;
		}
		if (i >= N) {
			return;
		}
		final IBinder retriever = p.readStrongBinder();
		while (i < N) {
			if (DEBUG)
				Log.d(TAG, "Reading more @" + i + " of " + N + ": retriever=" + retriever);
			Parcel data = Parcel.obtain();
			Parcel reply = Parcel.obtain();
			data.writeInt(i);
			try {
				retriever.transact(IBinder.FIRST_CALL_TRANSACTION, data, reply, 0);
			} catch (RemoteException e) {
				Log.w(TAG, "Failure retrieving array; only received " + i + " of " + N, e);
				return;
			}
			while (i < N && reply.readInt() != 0) {
				// final T parcelable = reply.readCreator(creator, loader);
				final T parcelable = reply.readParcelable(loader);
				verifySameType(listElementClass, parcelable.getClass());

				mList.add(parcelable);

				if (DEBUG)
					Log.d(TAG, "Read extra #" + i + ": " + mList.get(mList.size() - 1));
				i++;
			}
			reply.recycle();
			data.recycle();
		}
	}

	private static void verifySameType(final Class<?> expected, final Class<?> actual) {
		if (!actual.equals(expected)) {
			throw new IllegalArgumentException(
					"Can't unparcel type " + actual.getName() + " in list of type " + expected.getName());
		}
	}

	public List<T> getList() {
		return mList;
	}

	@Override
	public int describeContents() {
		int contents = 0;
		for (int i = 0; i < mList.size(); i++) {
			contents |= mList.get(i).describeContents();
		}
		return contents;
	}

	/**
	 * Write this to another Parcel. Note that this discards the internal Parcel
	 * and should not be used anymore. This is so we can pass this to a Binder
	 * where we won't have a chance to call recycle on this.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		final int N = mList.size();
		final int callFlags = flags;
		dest.writeInt(N);
		if (DEBUG)
			Log.d(TAG, "Writing " + N + " items");
		if (N > 0) {
			final Class<?> listElementClass = mList.get(0).getClass();
			// dest.writeParcelableCreator(mList.get(0));
			int i = 0;
			while (i < N && dest.dataSize() < MAX_FIRST_IPC_SIZE) {
				dest.writeInt(1);

				final T parcelable = mList.get(i);
				verifySameType(listElementClass, parcelable.getClass());
				// parcelable.writeToParcel(dest, callFlags);
				dest.writeParcelable(parcelable, callFlags);

				if (DEBUG)
					Log.d(TAG, "Wrote inline #" + i + ": " + mList.get(i));
				i++;
			}
			if (i < N) {
				dest.writeInt(0);
				Binder retriever = new Binder() {
					@Override
					protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
							throws RemoteException {
						if (code != FIRST_CALL_TRANSACTION) {
							return super.onTransact(code, data, reply, flags);
						}
						int i = data.readInt();
						if (DEBUG)
							Log.d(TAG, "Writing more @" + i + " of " + N);
						while (i < N && reply.dataSize() < MAX_IPC_SIZE) {
							reply.writeInt(1);

							final T parcelable = mList.get(i);
							verifySameType(listElementClass, parcelable.getClass());
							// parcelable.writeToParcel(reply, callFlags);
							reply.writeParcelable(parcelable, callFlags);

							if (DEBUG)
								Log.d(TAG, "Wrote extra #" + i + ": " + mList.get(i));
							i++;
						}
						if (i < N) {
							if (DEBUG)
								Log.d(TAG, "Breaking @" + i + " of " + N);
							reply.writeInt(0);
						}
						return true;
					}
				};
				if (DEBUG)
					Log.d(TAG, "Breaking @" + i + " of " + N + ": retriever=" + retriever);
				dest.writeStrongBinder(retriever);
			}
		}
	}
}