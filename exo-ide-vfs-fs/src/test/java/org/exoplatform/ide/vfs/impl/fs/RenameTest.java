/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.ide.vfs.impl.fs;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.ide.vfs.shared.ExitCodes;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.exoplatform.ide.vfs.shared.VirtualFileSystemInfo.BasicPermissions;

public class RenameTest extends LocalFileSystemTest
{
   private final String lockToken = "01234567890abcdef";

   private String fileId;
   private String filePath;

   private String lockedFileId;
   private String lockedFilePath;

   private String protectedFileId;
   private String protectedFilePath;

   private String folderId;
   private String folderPath;

   private String protectedFolderId;
   private String protectedFolderPath;

   private Map<String, String[]> properties;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      properties = new HashMap<String, String[]>(2);
      properties.put("MyProperty01", new String[]{"foo"});
      properties.put("MyProperty02", new String[]{"bar"});

      filePath = createFile(testRootPath, "RenameFileTest_File", DEFAULT_CONTENT_BYTES);
      lockedFilePath = createFile(testRootPath, "RenameFileTest_LockedFile", DEFAULT_CONTENT_BYTES);
      protectedFilePath = createFile(testRootPath, "RenameFileTest_ProtectedFile", DEFAULT_CONTENT_BYTES);
      folderPath = createDirectory(testRootPath, "RenameFileTest_Folder");
      writeProperties(folderPath, properties);
      // Add custom properties for each item in tree. Will check after rename to be sure all original properties are saved.
      createTree(folderPath, 6, 4, properties);
      protectedFolderPath = createDirectory(testRootPath, "RenameFileTest_ProtectedFolder");
      createTree(protectedFolderPath, 6, 4, properties);
      writeProperties(protectedFolderPath, properties);

      // Add custom properties for file. Will check after rename to be sure all original properties are saved.
      writeProperties(filePath, properties);

      createLock(lockedFilePath, lockToken);

      HashMap<String, Set<BasicPermissions>> accessList = new HashMap<String, Set<BasicPermissions>>(2);
      accessList.put("andrew", EnumSet.of(BasicPermissions.ALL));
      accessList.put("admin", EnumSet.of(BasicPermissions.READ));
      writeACL(protectedFilePath, accessList);
      writeACL(protectedFolderPath, accessList);

      fileId = pathToId(filePath);
      lockedFileId = pathToId(lockedFilePath);
      protectedFileId = pathToId(protectedFilePath);
      folderId = pathToId(folderPath);
      protectedFolderId = pathToId(protectedFolderPath);
   }

   public void testRenameFile() throws Exception
   {
      final String newName = "_FILE_NEW_NAME_";
      final String newMediaType = "text/*;charset=ISO-8859-1";
      String requestPath = SERVICE_URI + "rename/" + fileId + '?' + "newname=" + newName + '&' +
         "mediaType=" + newMediaType;
      ContainerResponse response = launcher.service("POST", requestPath, BASE_URI, null, null, null);
      assertEquals(200, response.getStatus());
      assertFalse("File must be removed. ", exists(filePath));
      String expectedPath = testRootPath + '/' + newName;
      assertTrue("Not found new file in expected location. ", exists(expectedPath));
      assertTrue(Arrays.equals(DEFAULT_CONTENT_BYTES, readFile(expectedPath)));
      Map<String,String[]> expectedProperties = new HashMap<String, String[]>(properties);
      expectedProperties.put("vfs:mimeType", new String[]{newMediaType});
      validateProperties(expectedPath, expectedProperties);
   }

   public void testRenameFileAlreadyExists() throws Exception
   {
      final String newName = "_FILE_NEW_NAME_";
      final byte[] existedFileContent = "existed file".getBytes();
      final String existedFile = createFile(testRootPath, newName, existedFileContent);
      String requestPath = SERVICE_URI + "rename/" + fileId + '?' + "newname=" + newName + '&' + "mediaType=" +
         "text/*;charset=ISO-8859-1";
      ContainerResponse response = launcher.service("POST", requestPath, BASE_URI, null, null, null);
      assertEquals(400, response.getStatus());
      assertEquals(ExitCodes.ITEM_EXISTS, Integer.parseInt((String)response.getHttpHeaders().getFirst("X-Exit-Code")));
      // Be sure file exists.
      assertTrue(exists(existedFile));
      // Check content.
      assertTrue(Arrays.equals(existedFileContent, readFile(existedFile)));
   }

   public void testRenameFileLocked() throws Exception
   {
      final String newName = "_FILE_NEW_NAME_";
      final String newMediaType = "text/*;charset=ISO-8859-1";
      String requestPath = SERVICE_URI + "rename/" + lockedFileId +
         '?' + "newname=" + newName + '&' + "mediaType=" + newMediaType + '&' + "lockToken=" + lockToken;
      ContainerResponse response = launcher.service("POST", requestPath, BASE_URI, null, null, null);
      assertEquals(200, response.getStatus());
      String expectedPath = testRootPath + '/' + newName;
      assertFalse("File must be removed. ", exists(lockedFilePath));
      assertTrue("Not found new file in expected location. ", exists(expectedPath));
      assertTrue(Arrays.equals(DEFAULT_CONTENT_BYTES, readFile(expectedPath)));

      Map<String,String[]> expectedProperties = new HashMap<String, String[]>(1);
      expectedProperties.put("vfs:mimeType", new String[]{newMediaType});
      validateProperties(expectedPath, expectedProperties);
   }

   public void testRenameFileLocked_NoLockToken() throws Exception
   {
      final String newName = "_FILE_NEW_NAME_";
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "rename/" + lockedFileId +
         '?' + "newname=" + newName + '&' + "mediaType=" + "text/*;charset=ISO-8859-1";
      ContainerResponse response = launcher.service("POST", requestPath, BASE_URI, null, null, writer, null);
      assertEquals(423, response.getStatus());
      log.info(new String(writer.getBody()));
      assertTrue("File must not be removed. ", exists(lockedFilePath));
      String expectedPath = testRootPath + '/' + newName;
      assertFalse("File must not be created. ", exists(expectedPath));
      assertTrue(Arrays.equals(DEFAULT_CONTENT_BYTES, readFile(lockedFilePath)));
   }

   public void testRenameFileNoPermissions() throws Exception
   {
      final String newName = "_FILE_NEW_NAME_";
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "rename/" + protectedFileId + '?' + "newname=" + newName;
      ContainerResponse response = launcher.service("POST", requestPath, BASE_URI, null, null, writer, null);
      assertEquals(403, response.getStatus());
      log.info(new String(writer.getBody()));
      assertTrue("File must not be removed. ", exists(protectedFilePath));
      String expectedPath = testRootPath + '/' + newName;
      assertFalse("File must not be created. ", exists(expectedPath));
      assertTrue(Arrays.equals(DEFAULT_CONTENT_BYTES, readFile(protectedFilePath)));
   }

   public void testRenameFolder() throws Exception
   {
      List<String> before = flattenDirectory(folderPath);

      final String newName = "_FOLDER_NEW_NAME_";
      String path = SERVICE_URI + "rename/" + folderId + '?' + "newname=" + newName;
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, null, null);
      assertEquals(200, response.getStatus());

      assertFalse("Folder must be removed. ", exists(folderPath));
      String expectedPath = testRootPath + '/' + newName;
      assertTrue("Not found new folder in expected location. ", exists(expectedPath));

      List<String> after = flattenDirectory(expectedPath);
      // Be sure there are no missed files.
      before.removeAll(after);
      assertTrue(String.format("Missed items: %s", before), before.isEmpty());

      validateProperties(expectedPath, properties, true);
   }

   public void testRenameFolderNoPermissions() throws Exception
   {
      final String newName = "_FOLDER_NEW_NAME_";
      List<String> before = flattenDirectory(protectedFolderPath);
      ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
      String requestPath = SERVICE_URI + "rename/" + protectedFolderId + '?' + "newname=" + newName;
      ContainerResponse response = launcher.service("POST", requestPath, BASE_URI, null, null, writer, null);
      assertEquals(403, response.getStatus());
      log.info(new String(writer.getBody()));
      assertTrue("Folder must not be removed. ", exists(protectedFolderPath));
      String expectedPath = testRootPath + '/' + newName;
      assertFalse("Folder must not be created. ", exists(expectedPath));
      List<String> after = flattenDirectory(protectedFolderPath);
      // Be sure there are no missed files.
      before.removeAll(after);
      assertTrue(String.format("Missed items: %s", before), before.isEmpty());
   }

   public void testRenameFolderUpdateMimeType() throws Exception
   {
      final String newName = "_FOLDER_NEW_NAME_";
      final String newMediaType = "text/directory%2BFOO"; // text/directory+FOO
      String path = SERVICE_URI + "rename/" + folderId + '?' + "newname=" + newName + '&' +
         "mediaType=" + newMediaType;
      ContainerResponse response = launcher.service("POST", path, BASE_URI, null, null, null);
      assertEquals(200, response.getStatus());
      String expectedPath = testRootPath + '/' + newName;
      assertTrue(exists(expectedPath));
      Map<String,String[]> expectedProperties = new HashMap<String, String[]>(1);
      expectedProperties.put("vfs:mimeType", new String[]{"text/directory+FOO"});
      validateProperties(expectedPath, expectedProperties, false); // media type updated only for current folder
      validateProperties(expectedPath, properties, true);
   }
}