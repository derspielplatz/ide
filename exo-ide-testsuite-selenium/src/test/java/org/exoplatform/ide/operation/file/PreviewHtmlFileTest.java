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
package org.exoplatform.ide.operation.file;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.TestConstants;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id:
 *
 */
public class PreviewHtmlFileTest extends BaseTest
{
   private final static String FILE_NAME = "PreviewHtmlFile.html";
   
   private final static String TEST_FOLDER = PreviewHtmlFileTest.class.getSimpleName();

   private final static String URL = BASE_URL + REST_CONTEXT + "/" + WEBDAV_CONTEXT + "/" + REPO_NAME + "/" + WS_NAME + "/";
   
   @BeforeClass
   public static void setUp()
   {
      
      String filePath ="src/test/resources/org/exoplatform/ide/operation/file/PreviewHtmlFile.html";
      try
      {
         VirtualFileSystemUtils.mkcol(URL + TEST_FOLDER);
         VirtualFileSystemUtils.put(filePath, MimeType.TEXT_HTML, URL+TEST_FOLDER+"/"+FILE_NAME);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (ModuleException e)
      {
         e.printStackTrace();
      }
   }
   
   @AfterClass
   public static void tearDown()
   {
      try
      {
         VirtualFileSystemUtils.delete(URL+TEST_FOLDER);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (ModuleException e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * IDE-65: Preview HTML File 
    * @throws Exception
    */
   @Test
   public void previewHtmlFile() throws Exception
   {
      //---- 1 -----------------
      //open html file
      Thread.sleep(TestConstants.SLEEP);
      
      IDE.toolbar().runCommandFromNewPopupMenu(MenuCommands.New.HTML_FILE);
      Thread.sleep(TestConstants.SLEEP);
      
      //---- 2 -----------------
      //check is Show Preview button disabled
      IDE.toolbar().checkButtonExistAtRight(ToolbarCommands.Run.SHOW_PREVIEW, true);
      IDE.toolbar().assertButtonEnabled(ToolbarCommands.Run.SHOW_PREVIEW, false);
      
      IDE.menu().checkCommandEnabled(MenuCommands.Run.RUN, MenuCommands.Run.SHOW_PREVIEW, false);
      
      //---- 3 -----------------
      //open file with text
//      Thread.sleep(TestConstants.SLEEP);
//      openOrCloseFolder(WS_NAME);
//      Thread.sleep(TestConstants.SLEEP);
      IDE.navigator().selectItem(WS_URL);
      IDE.menu().runCommand(MenuCommands.File.FILE, MenuCommands.File.REFRESH);
//      Thread.sleep(TestConstants.SLEEP);
      
      IDE.navigator().clickOpenIconOfFolder(WS_URL + TEST_FOLDER + "/");
      IDE.navigator().openFileFromNavigationTreeWithCodeEditor(FILE_NAME, false);
      
      Thread.sleep(TestConstants.SLEEP);
      
      //check is Show Preview menu enabled
      IDE.menu().checkCommandEnabled(MenuCommands.Run.RUN, MenuCommands.Run.SHOW_PREVIEW, true);
      
      //check is Show Preview button enabled
      IDE.toolbar().assertButtonEnabled(ToolbarCommands.Run.SHOW_PREVIEW, true);
      
      //---- 4 -----------------
      //click on button Show Preview on Toolbar
      IDE.toolbar().runCommand(ToolbarCommands.Run.SHOW_PREVIEW);
      Thread.sleep(TestConstants.SLEEP);
      
      //"Preview" tab should be displayed and we will see formatted text "Changed Content." 
      //and Google Logo (the same as http://www.google.com.ua/intl/en_com/images/logo_plain.png)
      checkPreviewTab();
      
      Thread.sleep(TestConstants.SLEEP);
      
      //---- 5 -----------------
      //close preview tab
      selenium.click("scLocator=//TabSet[ID=\"ideOperationPanel\"]/tab[ID=Preview]/icon");
      Thread.sleep(TestConstants.SLEEP_SHORT);
      
      //---- 6 -----------------
      //close tab with saved html file
      IDE.editor().closeTab(1);
      Thread.sleep(TestConstants.SLEEP);
      
      //new html file is opened.
      //Show Preview button is disabled
      IDE.toolbar().checkButtonExistAtRight(ToolbarCommands.Run.SHOW_PREVIEW, true);
      IDE.toolbar().assertButtonEnabled(ToolbarCommands.Run.SHOW_PREVIEW, false);
      IDE.menu().checkCommandEnabled(MenuCommands.Run.RUN, MenuCommands.Run.SHOW_PREVIEW, false);
      
      //---- 7 -----------------
      //Reopen HTML-file and click on "Run->Show Preview" top menu command.
      IDE.navigator().openFileFromNavigationTreeWithCodeEditor(FILE_NAME, false);
      IDE.menu().runCommand(MenuCommands.Run.RUN, MenuCommands.Run.SHOW_PREVIEW);
//      Thread.sleep(TestConstants.SLEEP);
      
      //"Preview" tab should be opened again and we will the same content as at the step 4.
      checkPreviewTab();
      
   }
   
   private void checkPreviewTab() throws Exception
   {
    //is Preview Tab present
      assertTrue(selenium.isElementPresent("scLocator=//TabSet[ID=\"ideOperationPanel\"]/tab[ID=Preview]/"));
      
      selenium.selectFrame("//iframe[@src='" + URL+TEST_FOLDER+"/"+FILE_NAME + "']");
      
      assertTrue(selenium.isElementPresent("//p/b/i[text()='Changed Content.']"));
      
      assertTrue(selenium.isElementPresent("//img[@src='http://www.google.com.ua/intl/en_com/images/logo_plain.png']"));
      
      IDE.editor().selectMainFrame();
   }
   
}
