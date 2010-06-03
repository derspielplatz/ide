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

package org.exoplatform.ideall.client.browser;

import java.util.List;

import org.exoplatform.gwtframework.ui.client.api.TreeGridItem;
import org.exoplatform.ideall.client.model.ApplicationContext;
import org.exoplatform.ideall.client.model.vfs.api.Item;
import org.exoplatform.ideall.client.panel.SimpleTabPanel;

import com.google.gwt.event.shared.HandlerManager;
import com.smartgwt.client.widgets.events.MouseDownEvent;
import com.smartgwt.client.widgets.events.MouseDownHandler;

public class BrowserFormNew extends SimpleTabPanel implements BrowserPanel, BrowserPresenter.Display
{

   public static final String TITLE = "Workspace";

   private HandlerManager eventBus;

   private GWTItemTreeGrid treeGrid;

   private ApplicationContext context;

   private BrowserPresenter presenter;

   public BrowserFormNew(HandlerManager eventBus, ApplicationContext context)
   {
      super(ID);
      this.eventBus = eventBus;

      this.context = context;

      treeGrid = new GWTItemTreeGrid();
      treeGrid.setEmptyMessage("Root folder not found!");
      addMember(treeGrid);

      presenter = new BrowserPresenter(eventBus, context);
      presenter.bindDisplay(this);

      addMouseDownHandler(new MouseDownHandler()
      {
         public void onMouseDown(MouseDownEvent event)
         {
            event.cancel();
         }
      });
   }

   @Override
   public void destroy()
   {
      super.destroy();
      presenter.destroy();
   }

   public TreeGridItem<Item> getBrowserTree()
   {
      return treeGrid;
   }

   public void selectItem(String path)
   {
      //treeGrid.selectItem(path);
   }

   public List<Item> getSelectedItems()
   {
      return treeGrid.getSelectedItems();
   }

}
