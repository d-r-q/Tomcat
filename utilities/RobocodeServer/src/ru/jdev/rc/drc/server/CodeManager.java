/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class CodeManager {

    private final Map<Competitor, CompetitorCode> codeBase = new HashMap<Competitor, CompetitorCode>();

    private final File robotsDirectory = new File("." + File.separator + "rc" + File.separator + "dev_path");

    public void updateCompetitorCode(Competitor competitor, CompetitorCode competitorCode) throws IOException {
        final CompetitorCode localCode = codeBase.get(competitor);
        if (localCode == null || !localCode.equals(competitorCode)) {
            storeCompetitor(competitorCode);
        }
    }

    public void storeCompetitor(CompetitorCode competitorCode) throws IOException {
        long startTime = System.currentTimeMillis();
        // final JarInputStream jis = new JarInputStream(new ByteArrayInputStream(competitorCode.code));
        JarInputStream jis = new JarInputStream(new FileInputStream("D:\\projects\\private\\jdevs-robocode\\trunk\\tomcat\\builds\\lxx.Tomcat_3.7.95.jar"));
        JarEntry e;
        while ((e = jis.getNextJarEntry()) != null) {
            String eName = e.getName();
            int idx = eName.lastIndexOf("/");

            final File dir = new File(robotsDirectory.getAbsolutePath() + File.separator + eName.substring(0, idx));
            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (idx != eName.length() - 1) {
                FileOutputStream fos = new FileOutputStream(new File(dir.getAbsolutePath() + File.separator + eName.substring(idx + 1)));
                byte[] buff = new byte[1024];
                int len;
                while ((len = jis.read(buff)) != -1) {
                    fos.write(buff, 0, len);
                }
                fos.flush();
                fos.close();
            }
        }
        jis.close();

        System.out.println("Store time: " + (System.currentTimeMillis() - startTime));
    }

    public void cleanup() {
        for (File file : robotsDirectory.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
    }

    private static void deleteDirectory(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
        directory.delete();
    }

}
