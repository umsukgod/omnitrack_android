package kr.ac.snu.hcil.android.common.file;

/*
  Copyright (c) 2014 CoderKiss
  <p>
  CoderKiss[AT]gmail.com
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static final String TAG = "ZipUtil";

    private final static int BUFFER_SIZE = 8192;

    public static boolean zip(String filePath, OutputStream zipStream) {
        try {
            File file = new File(filePath);
            BufferedInputStream bis = null;
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
                    zipStream));
            if (file.isDirectory()) {
                int baseLength = file.getParent().length() + 1;
                zipFolder(zos, file, baseLength);
            } else {
                byte data[] = new byte[BUFFER_SIZE];
                FileInputStream fis = new FileInputStream(filePath);
                bis = new BufferedInputStream(fis, BUFFER_SIZE);
                String entryName = file.getName();
                Log.i(TAG, "zip entry " + entryName);
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                int count;
                while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
                    zos.write(data, 0, count);
                }
            }
            zos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void zipFolder(ZipOutputStream zos, File folder,
                                  int baseLength) throws IOException {
        if (zos == null || folder == null) {
            return;
        }
        File[] fileList = folder.listFiles();

        if (fileList == null || fileList.length == 0) {
            return;
        }

        for (File file : fileList) {
            if (file.isDirectory()) {
                zipFolder(zos, file, baseLength);
            } else {
                byte data[] = new byte[BUFFER_SIZE];
                String unmodifiedFilePath = file.getPath();
                String realPath = unmodifiedFilePath.substring(baseLength);
                Log.i(TAG, "zip entry " + realPath);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                BufferedInputStream bis = new BufferedInputStream(fi,
                        BUFFER_SIZE);
                ZipEntry entry = new ZipEntry(realPath);
                zos.putNextEntry(entry);
                int count;
                while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
                    zos.write(data, 0, count);
                }
                bis.close();
            }
        }
    }
/*
    public static boolean unzip(String zipPath, String unzipFolder) {
        if (!new File(zipPath).exists()) {
            LogUtil.e(TAG, "zip path not exists!");
            return false;
        }

        if (!FileUtil.mkdirs(unzipFolder, true)) {
            LogUtil.e(TAG, "failed to create unzip folder.");
            return false;
        }

        try {
            FileInputStream fin = new FileInputStream(zipPath);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                String entryName = ze.getName();
                LogUtil.d(TAG, "unzip entry " + entryName);

                String entryPath = unzipFolder + "/" + entryName;
                if (ze.isDirectory()) {
                    FileUtil.mkdirs(entryPath);
                } else {
                    if (!FileUtil.create(entryPath, true)) {
                        continue;
                    }
                    FileOutputStream fout = new FileOutputStream(entryPath);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;
                    while ((len = zin.read(buffer)) != -1) {
                        fout.write(buffer, 0, len);
                    }
                    fout.close();
                    zin.closeEntry();
                }
            }
            zin.close();
        } catch (Exception e) {
            LogUtil.e("unzip exception", e);
            return false;
        }
        return true;
    }*/
}