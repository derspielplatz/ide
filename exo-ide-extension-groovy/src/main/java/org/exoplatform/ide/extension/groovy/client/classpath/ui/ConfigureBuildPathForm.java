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
package org.exoplatform.ide.extension.groovy.client.classpath.ui;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import org.exoplatform.gwtframework.ui.client.api.ListGridItem;
import org.exoplatform.gwtframework.ui.client.component.IButton;
import org.exoplatform.ide.client.framework.ui.DialogWindow;
import org.exoplatform.ide.extension.groovy.client.Images;
import org.exoplatform.ide.extension.groovy.client.classpath.GroovyClassPathEntry;

import java.util.List;

/**
 * Form for configuring classpath file.
 * 
 * @author <a href="mailto:zhulevaanna@gmail.com">Ann Zhuleva</a>
 * @version $Id: Jan 6, 2011 $
 *
 */
public class ConfigureBuildPathForm extends DialogWindow implements ConfigureBuildPathPresenter.Display
{
   public static final int WIDTH = 600;

   public static final int HEIGHT = 335;

   private final int BUTTON_WIDTH = 90;

   private final int BUTTON_HEIGHT = 22;

   public static final String ID = "ideConfigureBuildPathForm";

   //IDs for Selenium tests:

   private final String ID_CANCEL_BUTTON = "ideConfigureBuildPathFormCancelButton";

   private final String ID_SAVE_BUTTON = "ideConfigureBuildPathFormSaveButton";

   private final String ID_ADD_BUTTON = "ideConfigureBuildPathFormAddButton";

   private final String ID_REMOVE_BUTTON = "ideConfigureBuildPathFormRemoveButton";

   private final String TITLE = "Configure Classpath";

   /**
    * Cancel button.
    */
   private IButton cancelButton;

   /**
    * Save button.
    */
   private IButton saveButton;

   /**
    * Add source button.
    */
   private IButton addButton;

   /**
    * Remove source button.
    */
   private IButton removeButton;

   private ClassPathEntryListGrid classPathEntryListGrid;
   
   /**
    * @param eventBus handler manager
    */
   public ConfigureBuildPathForm(HandlerManager eventBus)
   {
      super(eventBus, WIDTH, HEIGHT, ID);
      setTitle(TITLE);

      VerticalPanel mainLayout = new VerticalPanel();
      mainLayout.setWidth("100%");
      mainLayout.setHeight("100%");
      mainLayout.setSpacing(10);
      mainLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

      mainLayout.add(createCenterLayout());
      mainLayout.add(createButtonsHorizontalLayout());

      add(mainLayout);
      show();
   }

   /**
    * Create main center layout.
    * 
    * @return {@link HorizontalPanel} center layout
    */
   private HorizontalPanel createCenterLayout()
   {
      HorizontalPanel hLayout = new HorizontalPanel();
      hLayout.setWidth("100%");
      hLayout.setSpacing(10);

      classPathEntryListGrid = new ClassPathEntryListGrid();
      classPathEntryListGrid.setWidth(420);
      classPathEntryListGrid.setHeight(210);
      
      hLayout.add(classPathEntryListGrid);
      hLayout.add(createButtonsVerticalLayout());
      return hLayout;
   }

   /**
    * Create layout with vertical buttons position.
    * 
    * @return {@link VerticalPanel} layout with buttons
    */
   private VerticalPanel createButtonsVerticalLayout()
   {
      VerticalPanel vLayout = new VerticalPanel();
      vLayout.setHeight((BUTTON_HEIGHT *2 + 10) + "px");
      vLayout.setSpacing(10);

      addButton = createButton(ID_ADD_BUTTON, "Add...", Images.Buttons.ADD);
      removeButton = createButton(ID_REMOVE_BUTTON, "Remove", Images.Buttons.REMOVE);

      vLayout.add(addButton);
      vLayout.add(removeButton);

      return vLayout;
   }

   /**
    * Create layout with horizontal buttons position.
    * 
    * @return {@link HorizontalPanel} layout with buttons
    */
   private HorizontalPanel createButtonsHorizontalLayout()
   {
      HorizontalPanel hLayout = new HorizontalPanel();
      hLayout.setHeight(BUTTON_HEIGHT+"px");
      hLayout.setSpacing(5);

      saveButton = createButton(ID_SAVE_BUTTON, "Save", Images.Buttons.OK);
      cancelButton = createButton(ID_CANCEL_BUTTON, "Cancel", Images.Buttons.NO);

      hLayout.add(saveButton);
      hLayout.add(cancelButton);

      return hLayout;
   }

   /**
    * Create button.
    * 
    * @param id button's id
    * @param title button's title
    * @param icon button's icon
    * @return {@link IButton} created button
    */
   private IButton createButton(String id, String title, String icon)
   {
      IButton button = new IButton(title);
      button.setID(id);
      button.setIcon(icon);
      button.setWidth(BUTTON_WIDTH);
      button.setHeight(BUTTON_HEIGHT);
      return button;
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#getAddButton()
    */
   public HasClickHandlers getAddButton()
   {
      return addButton;
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#getRemoveButton()
    */
   public HasClickHandlers getRemoveButton()
   {
      return removeButton;
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#getSaveButton()
    */
   public HasClickHandlers getSaveButton()
   {
      return saveButton;
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#getCancelButton()
    */
   public HasClickHandlers getCancelButton()
   {
      return cancelButton;
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#closeView()
    */
   public void closeView()
   {
      destroy();
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#getClassPathEntryListGrid()
    */
   public ListGridItem<GroovyClassPathEntry> getClassPathEntryListGrid()
   {
      return classPathEntryListGrid;
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#enableRemoveButton(boolean)
    */
   public void enableRemoveButton(boolean isEnabled)
   {
      if (isEnabled)
      {
         removeButton.enable();
      }
      else
      {
         removeButton.disable();
      }
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#getSelectedItems()
    */
   public List<GroovyClassPathEntry> getSelectedItems()
   {
      return classPathEntryListGrid.getSelectedItems();
   }

   /**
    * @see org.exoplatform.ide.client.module.groovy.classpath.ui.ConfigureBuildPathPresenter.Display#setCurrentRepository(java.lang.String)
    */
   @Override
   public void setCurrentRepository(String repository)
   {
      classPathEntryListGrid.setCurrentRepository(repository);
   }
}
