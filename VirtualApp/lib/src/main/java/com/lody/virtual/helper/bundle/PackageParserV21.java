package com.lody.virtual.helper.bundle;

import android.content.pm.PackageParser;

import java.io.File;

/**
 * @author Lody
 *
 */
/* package */ class PackageParserV21 extends PackageParserV17 {

	@Override
	public void parsePackage(File file, int flags) throws Exception {
		mParser = new PackageParser();
		mPackage = mParser.parsePackage(file, flags);
	}
}
