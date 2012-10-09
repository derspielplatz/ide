/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.ide.part;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

import org.exoplatform.ide.json.JsonArray;
import org.exoplatform.ide.json.JsonCollections;

/**
 * PartStack view class. Implements UI that manages Parts organized in a Tab-like widget.
 *
 * @author <a href="mailto:nzamosenchuk@exoplatform.com">Nikolay Zamosenchuk</a> 
 */
public class PartStackView extends Composite implements PartStackPresenter.Display
{

   /** Handles Focus Request Event. It is generated, when user clicks a stack anywhere */
   public interface FocusRequstHandler
   {
      /** PartStack is being clicked and requests Focus */
      void onRequestFocus();
   }
   
   private TabButton activeTab;

   // panels
   private final SimplePanel contentPanel;

   private boolean focused;

   // DOM Handler
   private final FocusRequstDOMHandler focusRequstHandler = new FocusRequstDOMHandler();

   private HandlerRegistration focusRequstHandlerRegistration;

   private final DockLayoutPanel parent;

   // list of tabs
   private final JsonArray<TabButton> tabs = JsonCollections.createArray();

   private final FlowPanel tabsPanel;

   private FocusRequstHandler displayHandler;

   private final PartStackResources resources;

   /**
    * Create View
    * @param partStackResources 
    */
   @Inject
   public PartStackView(PartStackResources partStackResources)
   {
      // TODO: make it elsewhere
      partStackResources.partStackCSS().ensureInjected();
      this.resources = partStackResources;
      parent = new DockLayoutPanel(Unit.PX);
      initWidget(parent);

      parent.setStyleName(resources.partStackCSS().idePartStack());
      tabsPanel = new FlowPanel();
      tabsPanel.setStyleName(resources.partStackCSS().idePartStackTabs());
      contentPanel = new SimplePanel();
      contentPanel.setStyleName(resources.partStackCSS().idePartStackContent());

      parent.addNorth(tabsPanel, 26);
      parent.add(contentPanel);

      addFocusRequestHandler();
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public TabItem addTabButton(Image icon, String title, boolean closable)
   {
      TabButton tabItem = new TabButton(title, closable);
      tabsPanel.add(tabItem);
      tabs.add(tabItem);
      return tabItem;
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public HasWidgets getContentPanel()
   {
      return contentPanel;
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void removeTabButton(int index)
   {
      if (index < tabs.size())
      {
         TabButton removed = tabs.remove(index);
         tabsPanel.remove(removed);
      }
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void setActiveTabButton(int index)
   {
      if (activeTab != null)
      {
         activeTab.removeStyleName(resources.partStackCSS().idePartStackTabSelected());
      }

      if (index >= 0 && index < tabs.size())
      {
         activeTab = tabs.get(index);
         activeTab.addStyleName(resources.partStackCSS().idePartStackTabSelected());
      }
   }

   /**
   * {@inheritDoc}
   */
   @Override
   public void setFocusRequstHandler(FocusRequstHandler handler)
   {
      this.displayHandler = handler;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setFocus(boolean focused)
   {
      if (this.focused == focused)
      {
         // already focused
         return;
      }

      this.focused = focused;

      // if focused already, then remove DOM handler
      if (focused)
      {
         parent.addStyleName(resources.partStackCSS().idePartStackFocused());
         removeFocusRequestHandler();
      }
      else
      {
         parent.removeStyleName(resources.partStackCSS().idePartStackFocused());
         addFocusRequestHandler();
      }
   }

   /**
    * Add MouseDown DOM Handler
    */
   protected void addFocusRequestHandler()
   {
      focusRequstHandlerRegistration = addDomHandler(focusRequstHandler, MouseDownEvent.getType());
   }

   /**
    * Remove MouseDown DOM Handler
    */
   protected void removeFocusRequestHandler()
   {
      if (focusRequstHandlerRegistration != null)
      {
         focusRequstHandlerRegistration.removeHandler();
         focusRequstHandlerRegistration = null;
      }
   }

   /**
    *
    */
   private class TabButton extends Composite implements PartStackPresenter.Display.TabItem
   {

      private Image image;

      private FlowPanel tabItem;

      private InlineLabel tabItemTittle;

      public TabButton(String title, boolean closable)
      {
         tabItem = new FlowPanel();
         initWidget(tabItem);
         this.setStyleName(resources.partStackCSS().idePartStackTab());
         tabItemTittle = new InlineLabel(title);
         tabItem.add(tabItemTittle);
         if (closable)
         {
            image = new Image(resources.close());
            tabItem.add(image);
            addHandlers();
         }
      }

      @Override
      public HandlerRegistration addClickHandler(ClickHandler handler)
      {
         return addDomHandler(handler, ClickEvent.getType());
      }

      @Override
      public HandlerRegistration addCloseHandler(CloseHandler<TabItem> handler)
      {
         return addHandler(handler, CloseEvent.getType());
      }

      private void addHandlers()
      {
         image.addClickHandler(new ClickHandler()
         {
            @Override
            public void onClick(ClickEvent event)
            {
               CloseEvent.fire(TabButton.this, TabButton.this);
            }
         });
      }
   }

   /**
    * Notifies delegated handler
    */
   private final class FocusRequstDOMHandler implements MouseDownHandler
   {
      @Override
      public void onMouseDown(MouseDownEvent event)
      {
         if (displayHandler != null)
         {
            displayHandler.onRequestFocus();
         }
      }
   }
}
