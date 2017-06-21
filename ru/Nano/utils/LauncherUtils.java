package ru.Nano.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class LauncherUtils {
    public static List<String> getClasses(File file) {
        List<String> classes = new ArrayList<>();
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    String entryName = entry.getName();
                    classes.add(entryName.replace("/", ".").substring(0, entryName.lastIndexOf('.')));
                }
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    public static String getMainClass(File file) {
        try {
            JarFile jarFile = new JarFile(file);
            String mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
            jarFile.close();
            return mainClass;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void writeToZip(File archive, String file, byte[] data) {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(archive));
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(archive.getAbsolutePath() + "tmp")));

            byte[] buf = new byte[4096 * 1024];
            ZipEntry entry = zipInputStream.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();
                if (!entryName.equals(file)) {
                    zipOutputStream.putNextEntry(new ZipEntry(entryName));
                    int len;
                    while ((len = zipInputStream.read(buf)) > 0) {
                        zipOutputStream.write(buf, 0, len);
                    }
                } else if (data.length == 0) {
                    // skip (remove entry)
                }
                else {
                    zipOutputStream.putNextEntry(new ZipEntry(entryName));
                    zipOutputStream.write(data, 0, data.length);
                }
                entry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();
            zipOutputStream.closeEntry();
            zipOutputStream.close();

            Files.delete(Paths.get(archive.getAbsolutePath()));
            Files.copy(Paths.get(archive.getAbsolutePath() + "tmp"),
                    Paths.get(archive.getAbsolutePath()));
            Files.delete(Paths.get(archive.getAbsolutePath() + "tmp"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
