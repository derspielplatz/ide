/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ide.client.template;

import com.google.gwt.event.shared.HandlerManager;

import org.exoplatform.gwtframework.commons.dialogs.Dialogs;
import org.exoplatform.gwtframework.commons.dialogs.callback.BooleanValueReceivedCallback;
import org.exoplatform.ide.client.framework.event.OpenFileEvent;
import org.exoplatform.ide.client.model.template.FileTemplate;
import org.exoplatform.ide.client.model.template.ProjectTemplate;
import org.exoplatform.ide.client.model.template.Template;
import org.exoplatform.ide.client.model.template.TemplateService;
import org.exoplatform.ide.client.model.util.IDEMimeTypes;
import org.exoplatform.ide.client.model.util.ImageUtil;
import org.exoplatform.ide.client.framework.module.vfs.api.File;
import org.exoplatform.ide.client.framework.module.vfs.api.Item;
import org.exoplatform.ide.client.framework.module.vfs.api.NodeTypeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version @version $Id: $
 */

public class CreateFileFromTemplatePresenter extends AbstractCreateFromTemplatePresenter<FileTemplate>
{

   private String previousExtension;
   
   private String baseHref;
   
   private List<ProjectTemplate> projectTemplateList;
   
   private List<ProjectTemplate> usedProjectTemplates;
   
   public CreateFileFromTemplatePresenter(HandlerManager eventBus, List<Item> selectedItems, List<Template> templateList)
   {
      super(eventBus, selectedItems);
      
      this.templateList = new ArrayList<FileTemplate>();
      
      projectTemplateList = new ArrayList<ProjectTemplate>();
      
      if (selectedItems != null && selectedItems.size() != 0)
      {
         Item item = selectedItems.get(0);

         baseHref = item.getHref();
         if (item instanceof File)
         {
            baseHref = baseHref.substring(0, baseHref.lastIndexOf("/") + 1);
         }
      }
      
      updateTemplateList(templateList);
   }
   
   /**
    * @see org.exoplatform.ide.client.template.AbstractCreateFromTemplatePresenter#bindDisplay(org.exoplatform.ide.client.template.CreateFromTemplateDisplay)
    */
   @Override
   public void bindDisplay(CreateFromTemplateDisplay<FileTemplate>d)
   {
      super.bindDisplay(d);
      display.getNameField().setValue("Untitled file");
   }

   /**
    * @see org.exoplatform.ide.client.template.AbstractCreateFromTemplatePresenter#updateTemplateList(java.util.List)
    */
   @Override
   void updateTemplateList(List<Template> templates)
   {
      templateList.clear();
      projectTemplateList.clear();
      
      for (Template template : templates)
      {
         if (template instanceof FileTemplate)
         {
            templateList.add((FileTemplate)template);
         }
         else if (template instanceof ProjectTemplate)
         {
            projectTemplateList.add((ProjectTemplate)template);
         }
      }
   }

   /**
    * @see org.exoplatform.ide.client.template.AbstractCreateFromTemplatePresenter#setNewInstanceName()
    */
   @Override
   void setNewInstanceName()
   {
      String extension = IDEMimeTypes.getExtensionsMap().get(selectedTemplate.getMimeType());
      if (previousExtension != null)
      {
         String fName = display.getNameField().getValue();
         if (fName.endsWith("." + previousExtension))
         {
            fName = fName.substring(0, fName.length() - previousExtension.length() - 1);
         }
         fName += "." + extension;
         display.getNameField().setValue(fName);
      }
      else
      {
         display.getNameField().setValue(display.getNameField().getValue() + "." + extension);
      }
      previousExtension = extension;
   }

   /**
    * @see org.exoplatform.ide.client.template.AbstractCreateFromTemplatePresenter#submitTemplate()
    */
   @Override
   void submitTemplate()
   {
      String fileName = display.getNameField().getValue();

      if ("".equals(fileName.trim()))
      {
         Dialogs.getInstance().showError("You must enter file name the first!");
         return;
      }
      
      if (baseHref == null)
      {
         baseHref = "";
      }

      String contentType = selectedTemplate.getMimeType();

      File newFile = new File(baseHref + fileName);
      newFile.setContentType(contentType);
      newFile.setJcrContentNodeType(NodeTypeUtil.getContentNodeType(contentType));
      newFile.setIcon(ImageUtil.getIcon(contentType));
      newFile.setNewFile(true);
      newFile.setContent(selectedTemplate.getContent());
      newFile.setContentChanged(true);

      eventBus.fireEvent(new OpenFileEvent(newFile));

      display.closeForm();
   }
   
   @Override
   protected void deleteTemplate()
   {
      String message = "Do you want to delete template <b>" + selectedTemplate.getName() + "</b>?";
      Dialogs.getInstance().ask("eXo IDE", message, new BooleanValueReceivedCallback()
      {
         public void execute(Boolean value)
         {
            if (value == null)
            {
               return;
            }
            if (value)
            {
               checkTemplateUsedInProjectTemplate();
            }
         }
      });
   }
   
   private void checkTemplateUsedInProjectTemplate()
   {
      usedProjectTemplates = new ArrayList<ProjectTemplate>();
      
      for (ProjectTemplate projectTemplate : projectTemplateList)
      {
         if (isPresentInProjectTemplate(projectTemplate))
         {
            usedProjectTemplates.add(projectTemplate);
         }
      }
      
      if (usedProjectTemplates.size() == 0)
      {
         TemplateService.getInstance().deleteTemplate(selectedTemplate);
         return;
      }
      
      String msg = "File template <b>" + selectedTemplate.getName() + "</b> is used in <b>";
      
      for (ProjectTemplate template : usedProjectTemplates)
      {
         msg += template.getName() + ", ";
      }
      
      msg = msg.substring(0, msg.length() - 2);
      msg += "</b> project template(s). Are your sure you want to delete this template?";
      
      Dialogs.getInstance().ask("IDE", msg, new BooleanValueReceivedCallback()
      {
         public void execute(Boolean value)
         {
            if (value == null)
            {
               return;
            }
            if (value)
            {
               TemplateService.getInstance().deleteTemplate(selectedTemplate);
            }
         }
      });
      
   }
   
   private boolean isPresentInProjectTemplate(ProjectTemplate projectTemplate)
   {
      if (projectTemplate.getChildren() == null)
      {
         return false;
      }
      for (Template template : projectTemplate.getChildren())
      {
         if (template instanceof FileTemplate && template.getName().equals(selectedTemplate.getName()))
         {
            return true;
         }
         else if (template instanceof ProjectTemplate)
         {
            return isPresentInProjectTemplate((ProjectTemplate)template);
         }
      }
      return false;
   }

}
