/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.runner.sdk;

import com.codenvy.api.core.util.LineConsumer;
import com.codenvy.api.core.util.ProcessUtil;
import com.codenvy.api.runner.RunnerException;
import com.codenvy.commons.lang.IoUtil;
import com.codenvy.ide.commons.GwtXmlUtils;
import com.codenvy.ide.maven.tools.MavenUtils;

import org.apache.maven.model.Model;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A smattering of useful methods.
 *
 * @author Artem Zatsarynnyy
 */
class Utils {
    /** Not instantiable. */
    private Utils() {
    }

    /** Returns URL to get Tomcat binary distribution. */
    static URL getTomcatBinaryDistribution() throws IOException {
        URL tomcatDistributionUrl = Thread.currentThread().getContextClassLoader().getResource("tomcat.zip");
        if (tomcatDistributionUrl == null) {
            throw new IOException("Unable to get Tomcat binary distribution.");
        }
        return tomcatDistributionUrl;
    }

    /** Returns URL to get Codenvy Platform binary distribution. */
    static URL getCodenvyPlatformBinaryDistribution() throws IOException {
        URL codenvyPlatformDistributionUrl = Thread.currentThread().getContextClassLoader().getResource("CodenvyPlatform.zip");
        if (codenvyPlatformDistributionUrl == null) {
            throw new IOException("Unable to get Codenvy Platform binary distribution.");
        }
        return codenvyPlatformDistributionUrl;
    }

    /**
     * Builds project with Maven from the specified sources.
     *
     * @param sourcesPath
     *         path to the folder that contains project sources to build
     * @param artifactNamePattern
     *         name pattern of the artifact to return
     * @return {@link java.util.zip.ZipFile} that represents a built artifact
     * @throws RunnerException
     */
    static ZipFile buildProjectFromSources(Path sourcesPath, String artifactNamePattern) throws Exception {
        final String[] command = new String[]{MavenUtils.getMavenExecCommand(), "clean", "package"};
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(sourcesPath.toFile());
        Process process = processBuilder.start();
        ProcessLineConsumer consumer = new ProcessLineConsumer();
        ProcessUtil.process(process, consumer, consumer);
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new Exception(consumer.getOutput().toString());
        }
        return new ZipFile(IoUtil.findFile(artifactNamePattern, sourcesPath.resolve("target").toFile()));
    }

    static ExtensionDescriptor getExtensionFromJarFile(ZipFile zipFile) throws IOException {
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry gwtXmlEntry = null;
            ZipEntry pomEntry = null;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    if (entry.getName().endsWith(GwtXmlUtils.GWT_MODULE_XML_SUFFIX)) {
                        gwtXmlEntry = entry;
                    } else if (entry.getName().endsWith("pom.xml")) {
                        pomEntry = entry;
                    }
                }
                // have both entries
                if (pomEntry != null && gwtXmlEntry != null) {
                    break;
                }
            }

            // TODO: consider Codenvy extensions validator
            if (gwtXmlEntry == null || pomEntry == null) {
                throw new IllegalArgumentException(String.format("%s is not a valid Codenvy extension", zipFile.getName()));
            }

            String gwtModuleName = gwtXmlEntry.getName().replace(java.io.File.separatorChar, '.');
            gwtModuleName = gwtModuleName.substring(0, gwtModuleName.length() - GwtXmlUtils.GWT_MODULE_XML_SUFFIX.length());
            Model pom = MavenUtils.readModel(zipFile.getInputStream(pomEntry));
            return new ExtensionDescriptor(gwtModuleName, MavenUtils.getGroupId(pom), pom.getArtifactId(), MavenUtils.getVersion(pom));
        } finally {
            zipFile.close();
        }
    }

    private static class ProcessLineConsumer implements LineConsumer {
        final StringBuilder output = new StringBuilder();

        @Override
        public void writeLine(String line) throws IOException {
            output.append('\n').append(line);
        }

        @Override
        public void close() throws IOException {
            //nothing to close
        }

        StringBuilder getOutput() {
            return output;
        }
    }

    static class ExtensionDescriptor {
        String gwtModuleName;
        String groupId;
        String artifactId;
        String version;

        ExtensionDescriptor(String gwtModuleName, String groupId, String artifactId, String version) {
            this.gwtModuleName = gwtModuleName;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }
}
