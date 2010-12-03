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
package org.exoplatform.ide.operation.restservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.exoplatform.common.http.client.ModuleException;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.ide.BaseTest;
import org.exoplatform.ide.Locators;
import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.TestConstants;
import org.exoplatform.ide.ToolbarCommands;
import org.exoplatform.ide.VirtualFileSystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
 *
 */
public class RESTServiceCreateTest extends BaseTest
{
   private static final String FOLDER_NAME=RESTServiceCreateTest.class.getSimpleName();
   
   private static final String FIRST_NAME = System.currentTimeMillis() + "test.groovy";
   
   private static final String SECOND_NAME = System.currentTimeMillis() + "новий.groovy";
   
   private final static String URL = BASE_URL +  REST_CONTEXT + "/" + WEBDAV_CONTEXT + "/" + REPO_NAME + "/" + WS_NAME + "/";
   
   @BeforeClass
   public static void setUp()
   {
      try
      {
         VirtualFileSystemUtils.mkcol(URL + FOLDER_NAME);
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
         VirtualFileSystemUtils.delete(URL + FOLDER_NAME);
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

   @Test
   public void testCreatingRESTService() throws Exception
   {
      Thread.sleep(TestConstants.SLEEP);
      IDE.toolbar().runCommand(ToolbarCommands.File.REFRESH);
      selectItemInWorkspaceTree(FOLDER_NAME);
      IDE.toolbar().runCommand(ToolbarCommands.File.REFRESH);
      
      IDE.toolbar().runCommandFromNewPopupMenu(MenuCommands.New.REST_SERVICE_FILE);
      saveAsUsingToolbarButton(FIRST_NAME);

      Thread.sleep(TestConstants.SLEEP_SHORT);
      IDE.toolbar().runCommand(ToolbarCommands.View.SHOW_PROPERTIES);

      assertTrue(selenium.isElementPresent(Locators.OperationForm.PROPERTIES_FORM_LOCATOR));

      assertEquals("false", selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextAutoload]/textbox"));
      assertEquals(TestConstants.NodeTypes.EXO_GROOVY_RESOURCE_CONTAINER,
         selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextContentNodeType]/textbox"));
      assertEquals(MimeType.GROOVY_SERVICE,
         selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextContentType]/textbox"));
      assertEquals(FIRST_NAME,
         selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextDisplayName]/textbox"));
      assertEquals(TestConstants.NodeTypes.NT_FILE,
         selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextFileNodeType]/textbox"));

      selenium.click(Locators.OperationForm.PROPERTIES_TAB_LOCATOR + Locators.CLOSE_ICON);
      
      Thread.sleep(TestConstants.SLEEP_SHORT);

      assertFalse(selenium.isElementPresent(Locators.OperationForm.PROPERTIES_FORM_LOCATOR));

      IDE.toolbar().runCommand(ToolbarCommands.View.SHOW_PROPERTIES);

      assertTrue(selenium.isElementPresent(Locators.OperationForm.PROPERTIES_FORM_LOCATOR));

      saveAsUsingToolbarButton(SECOND_NAME);
      Thread.sleep(TestConstants.SLEEP);

      assertEquals("false", selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextAutoload]/textbox"));
      assertEquals(TestConstants.NodeTypes.EXO_GROOVY_RESOURCE_CONTAINER,
         selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextContentNodeType]/textbox"));
      assertEquals(MimeType.GROOVY_SERVICE,
         selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextContentType]/textbox"));
      assertEquals(
         SECOND_NAME,
         selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextDisplayName]/textbox"));
      assertEquals(TestConstants.NodeTypes.NT_FILE,
         selenium.getText(Locators.OperationForm.PROPERTIES_FORM_LOCATOR + "/item[name=idePropertiesTextFileNodeType]/textbox"));
   }
   
}
