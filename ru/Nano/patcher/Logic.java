package ru.Nano.patcher;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import ru.Nano.utils.LauncherUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logic {
    private final File launcher;
    private final ClassPool pool = new ClassPool();
    private final List<String> classList = new ArrayList<>();
    private final HashMap<String, CtClass> deobfuscationMap = new HashMap<>();
    private final HashMap<CtClass, byte[]> patchedClasses = new HashMap<>();

    private File tempDir;

    public Logic(File launcher) {
        this.launcher = launcher;
        try {
            pool.appendSystemPath();
            pool.insertClassPath(launcher.getAbsolutePath());
            classList.addAll(LauncherUtils.getClasses(launcher));

            tempDir = File.createTempFile("patcher_", Long.toString(System.nanoTime()));
            if (!tempDir.delete())
                System.out.println("Can't delete temp file!");
            if (!tempDir.mkdir())
                System.out.println("Can't create temp dir!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void deobfuscate() {
        deobfuscationMap.put("launcher.helper.SecurityHelper", getClassByMethod("toPublicRSAKey"));
        deobfuscationMap.put("launcher.hasher.FileNameMatcher", getClassByMethod("shouldUpdate"));
        deobfuscationMap.put("launcher.helper.VerifyHelper", getClassByMethod("isValidIDName"));
    }

    public void patch() {
        try {
            // Disable certificate checks
            CtClass SecurityHelper = deobfuscationMap.get("launcher.helper.SecurityHelper");
            CtMethod isValidCertificate = SecurityHelper.getDeclaredMethod("isValidCertificate");
            CtMethod isValidCertificates = SecurityHelper.getDeclaredMethod("isValidCertificates");
            isValidCertificate.setBody("{ return true; }");
            isValidCertificates.setBody("{ return true; }");
            for (CtMethod m : SecurityHelper.getDeclaredMethods()) {
                if (m.getName().equals("isValidSign"))
                    m.setBody("{return true;}");

                if (m.getName().equals("isValidCertificates"))
                    m.setBody("{return true;}");
            }
            patchedClasses.put(SecurityHelper, SecurityHelper.toBytecode());

            // Disable file update
            CtClass FileNameMatcher = deobfuscationMap.get("launcher.hasher.FileNameMatcher");
            for (CtMethod m : FileNameMatcher.getDeclaredMethods()) {
                if (m.getName().equals("shouldUpdate") || m.getName().equals("shouldVerify") || m.getName().equals("anyMatch")) {
                    m.setBody("{ return false; }");
                }
            }
            patchedClasses.put(FileNameMatcher, FileNameMatcher.toBytecode());

            // IDK
            CtClass VerifyHelper = deobfuscationMap.get("launcher.helper.VerifyHelper");
            VerifyHelper.getDeclaredMethod("getMapValue").setBody("{return $1.get($2);}");
            patchedClasses.put(VerifyHelper, VerifyHelper.toBytecode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rebuild() {
        String tempLauncher = tempDir.getAbsolutePath() + "\\original.jar";
        try {
            Files.copy(Paths.get(launcher.getAbsolutePath()),
                    Paths.get(tempLauncher));
            String mainClass = LauncherUtils.getMainClass(new File(tempLauncher));

            String manifest = "Manifest-Version: 1.0\n" +
                    "Main-Class: " + mainClass + "\n";

            File archive = new File(tempLauncher);
            LauncherUtils.writeToZip(archive, "META-INF/LAUNCHER.SF", new byte[0]); // remove
            LauncherUtils.writeToZip(archive, "META-INF/LAUNCHER.RSA", new byte[0]); // remove
            LauncherUtils.writeToZip(archive, "META-INF/MANIFEST.MF", manifest.getBytes());

            for (Map.Entry<CtClass, byte[]> entry : patchedClasses.entrySet()) {
                LauncherUtils.writeToZip(archive,
                        entry.getKey().getName().replace(".", "/") + ".class",
                        entry.getValue());
            }

            Files.copy(archive.toPath(),
                    Paths.get(launcher.getAbsolutePath().replace(launcher.getName(),
                            launcher.getName().replace(".jar", "_Patch.jar"))));
            Files.walk(tempDir.toPath()).forEach(file -> file.toFile().delete());
            Files.delete(tempDir.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CtClass getClassByMethod(String name) {
        try {
            for (String className : classList) {
                CtClass klass = pool.getCtClass(className);
                for (CtMethod ctMethod : klass.getDeclaredMethods()) {
                    if (ctMethod.getName().equals(name))
                        return klass;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
