/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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
package com.codenvy.runner.docker.dockerfile;

import com.codenvy.api.core.util.Pair;
import com.codenvy.runner.docker.DockerImage;
import com.codenvy.runner.docker.DockerfileParser;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andrew00x
 */
public class DockerfileParserTest {
    @Test
    public void testParse() throws Exception {
        String dockerfileContent = "FROM base_image\n" +
                                   "# Comment 1\n" +
                                   "MAINTAINER Codenvy Corp\n" +
                                   "# Comment 2\n" +
                                   "RUN echo 1 > /dev/null\n" +
                                   "RUN echo 2 > /dev/null\n" +
                                   "RUN echo 3 > /dev/null\n" +
                                   "ADD file1 /tmp/file1\n" +
                                   "ADD http://example.com/folder/some_file.txt /tmp/file.txt  \n" +
                                   "EXPOSE 6000 7000\n" +
                                   "EXPOSE 8000   9000\n" +
                                   "# Comment 3\n" +
                                   "ENV ENV_VAR1 hello world\n" +
                                   "ENV ENV_VAR2\t to be or not to be\n" +
                                   "VOLUME [\"/data1\", \t\"/data2\"]\n" +
                                   "USER andrew\n" +
                                   "WORKDIR /tmp\n" +
                                   "ENTRYPOINT echo hello > /dev/null\n" +
                                   "CMD echo hello > /tmp/test";
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").toURI()).getParentFile();
        File file = new File(targetDir, "testParse");
        FileWriter w = new FileWriter(file);
        w.write(dockerfileContent);
        w.flush();
        w.close();
        List<DockerImage> dockerImages = DockerfileParser.parse(file).getImages();
        Assert.assertEquals(1, dockerImages.size());
        DockerImage dockerImage = dockerImages.get(0);
        Assert.assertEquals("base_image", dockerImage.getFrom());
        Assert.assertEquals(Arrays.asList("Codenvy Corp"), dockerImage.getMaintainer());
        Assert.assertEquals(Arrays.asList("echo 1 > /dev/null", "echo 2 > /dev/null", "echo 3 > /dev/null"), dockerImage.getRun());
        Assert.assertEquals("echo hello > /tmp/test", dockerImage.getCmd());
        Assert.assertEquals(Arrays.asList("6000", "7000", "8000", "9000"), dockerImage.getExpose());
        Map<String, String> env = new LinkedHashMap<>();
        env.put("ENV_VAR1", "hello world");
        env.put("ENV_VAR2", "to be or not to be");
        Assert.assertEquals(env, dockerImage.getEnv());
        Assert.assertEquals(
                Arrays.asList(Pair.of("file1", "/tmp/file1"), Pair.of("http://example.com/folder/some_file.txt", "/tmp/file.txt")),
                dockerImage.getAdd());
        Assert.assertEquals("echo hello > /dev/null", dockerImage.getEntrypoint());
        Assert.assertEquals(Arrays.asList("/data1", "/data2"), dockerImage.getVolume());
        Assert.assertEquals("andrew", dockerImage.getUser());
        Assert.assertEquals("/tmp", dockerImage.getWorkdir());
        Assert.assertEquals(Arrays.asList("Comment 1", "Comment 2", "Comment 3"), dockerImage.getComments());
    }

    @Test
    public void testParseMultipleImages() throws Exception {
        String dockerfileContent = "FROM base_image_1\n" +
                                   "# Image 1\n" +
                                   "MAINTAINER Codenvy Corp\n" +
                                   "RUN echo 1 > /dev/null\n" +
                                   "ADD http://example.com/folder/some_file.txt /tmp/file.txt  \n" +
                                   "EXPOSE 6000 7000\n" +
                                   "ENV ENV_VAR\t to be or not to be\n" +
                                   "VOLUME [\"/data1\"]\n" +
                                   "USER andrew\n" +
                                   "WORKDIR /tmp\n" +
                                   "ENTRYPOINT echo hello > /dev/null\n" +
                                   "CMD echo hello > /tmp/test1" +
                                   "\n" +
                                   "\n" +
                                   "FROM base_image_2\n" +
                                   "# Image 2\n" +
                                   "MAINTAINER Codenvy Corp\n" +
                                   "RUN echo 2 > /dev/null\n" +
                                   "ADD file1 /tmp/file1\n" +
                                   "EXPOSE 8000 9000\n" +
                                   "ENV ENV_VAR\t to be or not to be\n" +
                                   "VOLUME [\"/data2\"]\n" +
                                   "USER andrew\n" +
                                   "WORKDIR /home/andrew\n" +
                                   "ENTRYPOINT echo test > /dev/null\n" +
                                   "CMD echo hello > /tmp/test2";
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").toURI()).getParentFile();
        File file = new File(targetDir, "testParse");
        FileWriter w = new FileWriter(file);
        w.write(dockerfileContent);
        w.flush();
        w.close();
        List<DockerImage> dockerImages = DockerfileParser.parse(file).getImages();
        Assert.assertEquals(2, dockerImages.size());
        DockerImage dockerImage1 = dockerImages.get(0);
        Assert.assertEquals("base_image_1", dockerImage1.getFrom());
        Assert.assertEquals(Arrays.asList("Codenvy Corp"), dockerImage1.getMaintainer());
        Assert.assertEquals(Arrays.asList("echo 1 > /dev/null"), dockerImage1.getRun());
        Assert.assertEquals("echo hello > /tmp/test1", dockerImage1.getCmd());
        Assert.assertEquals(Arrays.asList("6000", "7000"), dockerImage1.getExpose());
        Map<String, String> env1 = new LinkedHashMap<>();
        env1.put("ENV_VAR", "to be or not to be");
        Assert.assertEquals(env1, dockerImage1.getEnv());
        Assert.assertEquals(Arrays.asList(Pair.of("http://example.com/folder/some_file.txt", "/tmp/file.txt")),
                            dockerImage1.getAdd());
        Assert.assertEquals("echo hello > /dev/null", dockerImage1.getEntrypoint());
        Assert.assertEquals(Arrays.asList("/data1"), dockerImage1.getVolume());
        Assert.assertEquals("andrew", dockerImage1.getUser());
        Assert.assertEquals("/tmp", dockerImage1.getWorkdir());
        Assert.assertEquals(Arrays.asList("Image 1"), dockerImage1.getComments());

        DockerImage dockerImage2 = dockerImages.get(1);
        Assert.assertEquals("base_image_2", dockerImage2.getFrom());
        Assert.assertEquals(Arrays.asList("Codenvy Corp"), dockerImage2.getMaintainer());
        Assert.assertEquals(Arrays.asList("echo 2 > /dev/null"), dockerImage2.getRun());
        Assert.assertEquals("echo hello > /tmp/test2", dockerImage2.getCmd());
        Assert.assertEquals(Arrays.asList("8000", "9000"), dockerImage2.getExpose());
        Map<String, String> env2 = new LinkedHashMap<>();
        env2.put("ENV_VAR", "to be or not to be");
        Assert.assertEquals(env2, dockerImage2.getEnv());
        Assert.assertEquals(Arrays.asList(Pair.of("file1", "/tmp/file1")), dockerImage2.getAdd());
        Assert.assertEquals("echo test > /dev/null", dockerImage2.getEntrypoint());
        Assert.assertEquals(Arrays.asList("/data2"), dockerImage2.getVolume());
        Assert.assertEquals("andrew", dockerImage2.getUser());
        Assert.assertEquals("/home/andrew", dockerImage2.getWorkdir());
        Assert.assertEquals(Arrays.asList("Image 2"), dockerImage2.getComments());
    }
}
