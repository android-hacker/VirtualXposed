package com.lody.virtual.helper.loaders;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.lody.virtual.helper.utils.FileIO;

/**
 * @author Lody
 *
 */
public class AssetFileUnpacker {

	private static final String ASSET_DIR_NAME = "assets/";

	public static void unpack(File apkFile, File targetDir) throws IOException {
		ZipFile zipFile = new ZipFile(apkFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory() && entry.getName().startsWith(ASSET_DIR_NAME)) {
				String fileName = entry.getName().substring(ASSET_DIR_NAME.length());
				File targetFile = new File(targetDir, fileName);
				File targetParentDir = targetFile.getParentFile();
				if (!targetParentDir.exists()) {
					targetParentDir.mkdirs();
				}
				FileIO.writeToFile(zipFile.getInputStream(entry), targetFile);
			}
		}
		zipFile.close();
	}
}
