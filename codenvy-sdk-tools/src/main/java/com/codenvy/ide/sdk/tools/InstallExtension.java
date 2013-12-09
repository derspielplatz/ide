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
package com.codenvy.ide.sdk.tools;

import com.codenvy.ide.commons.FileUtils;

import org.apache.maven.model.Model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Tool to integrate 3rd-party extensions to existing Codenvy IDE.
 * <p/>
 * This tool looks up all 3rd-party extensions in a special folder.
 * For every found extension:
 * <p/>
 * - get GWT-module name from gwt.xml descriptor;
 * <p/>
 * - get Maven artifact coordinates;
 * <p/>
 * - add collected info in IDE.gwt.xml and pom.xml.
 *
 * @author <a href="mailto:azatsarynnyy@codenvy.com">Artem Zatsarynnyy</a>
 */
public class InstallExtension {
    /** Filename suffix used for GWT module XML files. */
    public static final String GWT_MODULE_XML_SUFFIX       = ".gwt.xml";
    public static final String IDE_GWT_XML_FILE_NAME       = "IDE.gwt.xml";
    /** CLI argument specifies the location of the directory that contains extensions to add. */
    public static final String EXT_DIR_PARAMETER           = "--extDir=";
    /** CLI argument specifies the location of the directory that contains resource files to re-build Codenvy IDE. */
    public static final String EXT_RESOURCES_DIR_PARAMETER = "--extResourcesDir=";
    /** Location of the directory that contains 3rd-party extensions. */
    public static       Path   extDirPath                  = null;
    /** Location of the directory that contains resource files to re-build Codenvy IDE. */
    public static       Path   extResourcesWorkDirPath     = null;

    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            if (arg.startsWith(EXT_DIR_PARAMETER)) {
                extDirPath = Paths.get(arg.substring(EXT_DIR_PARAMETER.length()));
            } else if (arg.startsWith(EXT_RESOURCES_DIR_PARAMETER)) {
                final Path extResourcesDirPath = Paths.get(arg.substring(EXT_RESOURCES_DIR_PARAMETER.length()));
                final String tempDirName = "temp";
                extResourcesWorkDirPath = extResourcesDirPath.resolve(tempDirName);
                // delete working directory from previous build if it exist
                FileUtils.deleteRecursive(extResourcesWorkDirPath.toFile());
                Files.createDirectory(extResourcesWorkDirPath);
                FileUtils.copy(extResourcesDirPath.toFile(), extResourcesWorkDirPath.toFile(), new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return !(tempDirName.equals(name));
                    }
                });
            } else {
                System.err.println("Unknown flag: " + arg);
                System.exit(1);
            }
        }

        List<Extension> extensions = findExtensionsByPath(extDirPath);
        for (Extension extension : extensions) {
            Utils.addDependencyToPom(extResourcesWorkDirPath.resolve("pom.xml"),
                                     extension.groupId, extension.artifactId, extension.artifactVersion);
            final Path ideGwtXmlPath = Utils.findFile(IDE_GWT_XML_FILE_NAME, extResourcesWorkDirPath);
            Utils.inheritGwtModule(ideGwtXmlPath, extension.gwtModuleName);
        }
    }

    private static List<Extension> findExtensionsByPath(Path extDirPath) throws IOException {
        File[] files = extDirPath.toFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        List<Extension> extensions = new ArrayList<>();
        for (File file : files) {
            Extension extension = getExtensionFromFile(file.toPath());
            extensions.add(extension);
            System.out.println(String.format("Extension %s found", extension.gwtModuleName));
        }
        System.out.println(String.format("Found: %d extension(s)", extensions.size()));
        return extensions;
    }

    private static Extension getExtensionFromFile(Path zipPath) throws IOException {
        ZipFile zipFile = new ZipFile(zipPath.toString());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry gwtXmlEntry = null;
        ZipEntry pomEntry = null;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().endsWith(GWT_MODULE_XML_SUFFIX)) {
                    gwtXmlEntry = entry;
                } else if (entry.getName().endsWith("pom.xml")) {
                    pomEntry = entry;
                }
            }
        }

        // TODO consider Codenvy extension validator
        if (gwtXmlEntry == null || pomEntry == null) {
            throw new IllegalArgumentException(zipPath.toString() + " is not a valid Codenvy extension");
        }

        String gwtModuleName = gwtXmlEntry.getName().replace(File.separatorChar, '.');
        gwtModuleName = gwtModuleName.substring(0, gwtModuleName.length() - GWT_MODULE_XML_SUFFIX.length());
        // TODO use utils from codenvy-builder-tools
        final Model pom = Utils.readPom(zipFile.getInputStream(pomEntry));
        zipFile.close();
        return new Extension(gwtModuleName, pom.getGroupId(), pom.getArtifactId(), pom.getVersion());
    }

    private static class Extension {
        String gwtModuleName;
        String groupId;
        String artifactId;
        String artifactVersion;

        Extension(String gwtModuleName, String groupId, String artifactId, String artifactVersion) {
            this.gwtModuleName = gwtModuleName;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.artifactVersion = artifactVersion;
        }

        @Override
        public String toString() {
            return "Extension{" +
                   "gwtModuleName='" + gwtModuleName + '\'' +
                   ", groupId='" + groupId + '\'' +
                   ", artifactId='" + artifactId + '\'' +
                   ", artifactVersion='" + artifactVersion + '\'' +
                   '}';
        }

    }
}
