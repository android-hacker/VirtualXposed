package com.lody.virtual.service.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import java.util.Arrays;
import java.util.jar.Attributes;

/**
 * Represents the manifest digest for a package. This is suitable for comparison
 * of two packages to know whether the manifests are identical.
 *
 * @hide
 */
public class ManifestDigest implements Parcelable {
	public static final Creator<ManifestDigest> CREATOR = new Creator<ManifestDigest>() {
		public ManifestDigest createFromParcel(Parcel source) {
			return new ManifestDigest(source);
		}

		public ManifestDigest[] newArray(int size) {
			return new ManifestDigest[size];
		}
	};
	/** Digest field names to look for in preferred order. */
	private static final String[] DIGEST_TYPES = {"SHA1-Digest", "SHA-Digest", "MD5-Digest",};

	/** What we print out first when toString() is called. */
	private static final String TO_STRING_PREFIX = "ManifestDigest {mDigest=";
	/**
	 * The digits for every supported radix.
	 */
	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	private static final char[] UPPER_CASE_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
			'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
			'Y', 'Z'};
	/** The digest of the manifest in our preferred order. */
	private final byte[] mDigest;

	ManifestDigest(byte[] digest) {
		mDigest = digest;
	}

	private ManifestDigest(Parcel source) {
		mDigest = source.createByteArray();
	}

	public static ManifestDigest fromAttributes(Attributes attributes) {
		if (attributes == null) {
			return null;
		}

		String encodedDigest = null;

		for (String t : DIGEST_TYPES) {
			final String value = attributes.getValue(t);
			if (value != null) {
				encodedDigest = value;
				break;
			}
		}

		if (encodedDigest == null) {
			return null;
		}

		final byte[] digest = Base64.decode(encodedDigest, Base64.DEFAULT);
		return new ManifestDigest(digest);
	}

	public static StringBuilder appendByteAsHex(StringBuilder sb, byte b, boolean upperCase) {
		char[] digits = upperCase ? UPPER_CASE_DIGITS : DIGITS;
		sb.append(digits[(b >> 4) & 0xf]);
		sb.append(digits[b & 0xf]);
		return sb;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ManifestDigest)) {
			return false;
		}

		final ManifestDigest other = (ManifestDigest) o;

		return this == other || Arrays.equals(mDigest, other.mDigest);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(mDigest);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(TO_STRING_PREFIX.length() + (mDigest.length * 3) + 1);

		sb.append(TO_STRING_PREFIX);

		final int N = mDigest.length;
		for (int i = 0; i < N; i++) {
			final byte b = mDigest[i];
			appendByteAsHex(sb, b, false);
			sb.append(',');
		}
		sb.append('}');

		return sb.toString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByteArray(mDigest);
	}

}