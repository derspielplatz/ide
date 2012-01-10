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
package org.exoplatform.ide.operation.autocompletion.groovy;

import static org.junit.Assert.assertTrue;

import org.exoplatform.ide.MenuCommands;
import org.exoplatform.ide.operation.autocompletion.CodeAssistantBaseTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Keys;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: Dec 10, 2010 2:34:17 PM evgen $
 *
 */
public class GroovyClassNameCompletionTest extends CodeAssistantBaseTest
{
   
   @BeforeClass
   public static void createProject()
   {
      createProject(GroovyClassNameCompletionTest.class.getSimpleName());
   }

   @Test
   public void testGroovyClassNameCompletion() throws Exception
   {
      IDE.TOOLBAR.runCommandFromNewPopupMenu(MenuCommands.New.REST_SERVICE_FILE);
      IDE.EDITOR.waitActiveFile(projectName + "/" + "Untitled file.grs");

      IDE.EDITOR.moveCursorDown(0, 9);
      IDE.EDITOR.typeTextIntoEditor(0, Keys.END.toString() + "\nCollection");

      //open autocomplete form
      IDE.CODEASSISTANT.openForm();

      assertTrue(IDE.CODEASSISTANT.isElementPresent("Collections"));
      assertTrue(IDE.CODEASSISTANT.isElementPresent("Collection"));

      IDE.CODEASSISTANT.moveCursorDown(1);

      IDE.CODEASSISTANT.insertSelectedItem();

      assertTrue(IDE.EDITOR.getTextFromCodeEditor(0).contains("Collections"));
   }

}
