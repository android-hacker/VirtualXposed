package com.lody.virtual.helper.proto;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * IPC时，单独传递一个List，可能会超过IPC_MAX,从而导致IPC数据传递失败， 本类实现了List分包传输。
 */
public class VParceledListSlice implements Parcelable {

	public static final Creator CREATOR = new Creator() {
		public final VParceledListSlice createFromParcel(Parcel source) {
			return new VParceledListSlice(source);
		}

		public final VParceledListSlice[] newArray(int size) {
			return new VParceledListSlice[size];
		}
	};
	private List<Parcelable> mList;

	public VParceledListSlice(Parcel parcel) {
		readFromParcel(parcel);
	}

	public VParceledListSlice(List<Parcelable> mList) {
		this.mList = mList;
	}

	private static Parcelable newParcelable(Creator creator, Parcel parcel) {
		return (Parcelable) creator.createFromParcel(parcel);
	}

	private static Creator getCreator(Parcel parcel) {
		try {
			return (Creator) Class.forName(parcel.readString()).getField("CREATOR").get(null);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void compare(Class one, Class second) {
		if (!second.equals(one)) {
			throw new IllegalArgumentException(
					"Can't unparcel type " + second.getName() + " in list of type " + one.getName());
		}
	}

	public final List<Parcelable> getmList() {
		return this.mList;
	}

	public final void readFromParcel(Parcel parcel) {
		int i = 0;
		int length = parcel.readInt();
		this.mList = new ArrayList<Parcelable>(length);
		if (length > 0) {
			Creator b = getCreator(parcel);
			Class cls = null;
			while (i < length && parcel.readInt() != 0) {
				Parcelable parcelable = newParcelable(b, parcel);
				if (cls == null) {
					cls = parcelable.getClass();
				} else {
					compare(cls, parcelable.getClass());
				}
				this.mList.add(parcelable);
				i++;
			}
			if (i < length) {
				IBinder binder = parcel.readStrongBinder();
				while (i < length) {
					Parcel data = Parcel.obtain();
					Parcel reply = Parcel.obtain();
					data.writeInt(i);
					try {
						binder.transact(1, data, reply, 0);
						while (i < length && reply.readInt() != 0) {
							Parcelable newParcelable = newParcelable(b, reply);
							compare(cls, newParcelable.getClass());
							this.mList.add(newParcelable);
							i++;
						}
						reply.recycle();
						data.recycle();
					} catch (RemoteException e) {
						return;
					}
				}
			}
		}
	}

	public final void setParcelables(List<Parcelable> list) {
		this.mList = list;
	}

	public int describeContents() {
		int contents = 0;
		for (int i = 0; i < mList.size(); i++) {
			contents |= mList.get(i).describeContents();
		}
		return contents;
	}

	public void writeToParcel(Parcel parcel, final int flags) {
		final int N = this.mList == null ? 0 : this.mList.size();
		parcel.writeInt(N);
		if (N > 0) {
			final Class listElementClass = ((Parcelable) this.mList.get(0)).getClass();
			parcel.writeString(listElementClass.getName());
			int i = 0;
			while (i < N && parcel.dataSize() < 131072) {
				parcel.writeInt(1);
				Parcelable parcelable = this.mList.get(i);
				compare(listElementClass, parcelable.getClass());
				parcelable.writeToParcel(parcel, flags);
				i++;
			}
			if (i < N) {
				parcel.writeInt(0);
				parcel.writeStrongBinder(new Binder() {

					protected final boolean onTransact(int code, Parcel data, Parcel reply, int flags)
							throws RemoteException {
						if (code != FIRST_CALL_TRANSACTION) {
							return super.onTransact(code, data, reply, flags);
						}
						int i = data.readInt();
						while (i < N && reply.dataSize() < 262144) {
							reply.writeInt(1);
							Parcelable parcelable = mList.get(i);
							VParceledListSlice.compare(listElementClass, parcelable.getClass());
							parcelable.writeToParcel(reply, code);
							i++;
						}
						if (i < N) {
							reply.writeInt(0);
						}
						return true;
					}
				});
			}
		}
	}
}