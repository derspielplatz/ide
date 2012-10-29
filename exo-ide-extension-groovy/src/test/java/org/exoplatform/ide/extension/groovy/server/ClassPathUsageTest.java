/*
 * Copyright (C) 2011 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
///*
// * Copyright (C) 2010 eXo Platform SAS.
// *
// * This is free software; you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as
// * published by the Free Software Foundation; either version 2.1 of
// * the License, or (at your option) any later version.
// *
// * This software is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this software; if not, write to the Free
// * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
// * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
// */
//package org.exoplatform.ide.extension.groovy.server;
//
//import org.everrest.core.impl.provider.json.JsonException;
//import org.everrest.groovy.SourceFile;
//import org.everrest.groovy.SourceFolder;
//import org.exoplatform.ide.codeassistant.framework.server.utils.GroovyClassPath;
//import org.exoplatform.ide.codeassistant.framework.server.utils.GroovyClassPathEntry;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.io.ByteArrayInputStream;
//
///**
// * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
// * @version $Id: Dec 28, 2010 $
// */
//public class ClassPathUsageTest
//{
//   private final String correctClassPathFile =
//      "{\"entries\": [{\"kind\": \"file\",\"path\": \"dev-monit#/Test.groovy\"},"
//         + "{\"kind\": \"dir\",\"path\": \"/dev-monit#/test/\"}]}";
//
//   private final String emptyEntriesClassPathFile = "{\"entries\": []}";
//
//   private final String wrongTypeClassPathFile =
//      "{\"entries\": [{\"kind\": \"file\",\"path\": \"dev-monit#/Test.groovy\"},"
//         + "{\"kind\": \"\",\"path\": \"dev-monit#/test1/\"},"
//         + "{\"kind\": \"filee\",\"path\": \"dev-monit#/test2/\"},"
//         + "{\"kind\": \"dir\",\"path\": \"dev-monit#/test3/\"}" + "]}";
//
//   @Test
//   public void classPathFileCorrect() throws JsonException
//   {
//      GroovyClassPath classPath =
//         GroovyScriptServiceUtil.json2ClassPath(new ByteArrayInputStream(correctClassPathFile.getBytes()));
//      Assert.assertEquals(classPath.getEntries().length, 2);
//      GroovyClassPathEntry entry = classPath.getEntries()[0];
//      Assert.assertEquals(entry.getKind(), "file");
//      Assert.assertEquals(entry.getPath(), "dev-monit#/Test.groovy");
//      entry = classPath.getEntries()[1];
//      Assert.assertEquals(entry.getKind(), "dir");
//      Assert.assertEquals(entry.getPath(), "/dev-monit#/test/");
//
//      Assert.assertEquals(GroovyClassPathHelper.getSourceFiles(classPath).length, 1);
//      Assert.assertEquals(GroovyClassPathHelper.getSourceFolders(classPath).length, 1);
//   }
//
//   @Test
//   public void classPathFileWrongType() throws JsonException
//   {
//      GroovyClassPath classPath =
//         GroovyScriptServiceUtil.json2ClassPath(new ByteArrayInputStream(wrongTypeClassPathFile.getBytes()));
//      Assert.assertEquals(classPath.getEntries().length, 4);
//
//      SourceFile[] files = GroovyClassPathHelper.getSourceFiles(classPath);
//      SourceFolder[] src = GroovyClassPathHelper.getSourceFolders(classPath);
//      Assert.assertEquals(files.length, 1);
//      Assert.assertEquals(files[0].getPath().toString(), "ide+vfs:/dev-monit#/Test.groovy");
//
//      Assert.assertEquals(src.length, 1);
//      Assert.assertEquals(src[0].getPath().toString(), "ide+vfs:/dev-monit#/test3/");
//   }
//
//   @Test
//   public void classPathFileEmptyEntries() throws JsonException
//   {
//      GroovyClassPath classPath =
//         GroovyScriptServiceUtil.json2ClassPath(new ByteArrayInputStream(emptyEntriesClassPathFile.getBytes()));
//      Assert.assertEquals(classPath.getEntries().length, 0);
//
//      Assert.assertEquals(GroovyClassPathHelper.getSourceFiles(classPath).length, 0);
//      Assert.assertEquals(GroovyClassPathHelper.getSourceFolders(classPath).length, 0);
//   }
//
//   @Test
//   public void emptyClassPathFile() throws JsonException
//   {
//      GroovyClassPath classPath = GroovyScriptServiceUtil.json2ClassPath(new ByteArrayInputStream("".getBytes()));
//      Assert.assertNull(classPath);
//   }
//}