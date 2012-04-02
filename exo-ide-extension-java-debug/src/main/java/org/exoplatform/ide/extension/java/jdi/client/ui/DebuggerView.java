/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.ide.extension.java.jdi.client.ui;

import org.exoplatform.gwtframework.ui.client.CellTreeResource;
import org.exoplatform.gwtframework.ui.client.component.IconButton;
import org.exoplatform.gwtframework.ui.client.component.ImageButton;
import org.exoplatform.gwtframework.ui.client.component.Label;
import org.exoplatform.gwtframework.ui.client.tablayout.TabPanel;
import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.ui.impl.ViewType;
import org.exoplatform.ide.extension.java.jdi.client.DebuggerClientBundle;
import org.exoplatform.ide.extension.java.jdi.client.DebuggerExtension;
import org.exoplatform.ide.extension.java.jdi.client.DebuggerPresenter;
import org.exoplatform.ide.extension.java.jdi.shared.BreakPoint;
import org.exoplatform.ide.extension.java.jdi.shared.BreakPointList;
import org.exoplatform.ide.extension.java.jdi.shared.DebuggerInfo;
import org.exoplatform.ide.extension.java.jdi.shared.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.i18n.shared.DirectionEstimator;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vparfonov@exoplatform.com">Vitaly Parfonov</a>
 * @version $Id: $
*/
public class DebuggerView extends ViewImpl implements DebuggerPresenter.Display
{

   private static final String ID = "ideDebuggerView";

   private static DebugWindowUiBinder uiBinder = GWT.create(DebugWindowUiBinder.class);

   interface DebugWindowUiBinder extends UiBinder<Widget, DebuggerView>
   {
   }

   @UiField
   TabPanel variabelsPanel;

   @UiField
   TabPanel breakPointsPanel;

   @UiField
   ImageButton resumeButton;

   @UiField
   ImageButton disconnectButton;

   @UiField
   ImageButton removeAllBreakpointsButton;
   
   @UiField
   Label vmName;

   CellList<BreakPoint> breakpointsContainer;

   CellTree frameTree;

   private SingleSelectionModel<Variable> selectionModel;

   private FrameTreeViewModel frameTreeViewModel;

   private CellTree.Resources res = GWT.create(CellTreeResource.class);
   
   private DebuggerInfo debuggerInfo;

   public DebuggerView(DebuggerInfo debuggerInfo)
   {

      super(ID, ViewType.OPERATION, DebuggerExtension.LOCALIZATION_CONSTANT.debug());
      add(uiBinder.createAndBindUi(this));

      this.debuggerInfo = debuggerInfo;
      
      frameTreeViewModel = new FrameTreeViewModel(selectionModel, debuggerInfo);
      frameTree = new CellTree(frameTreeViewModel, null, res);

      BreakPointCell breakpointCell = new BreakPointCell();
      breakpointsContainer = new CellList<BreakPoint>(breakpointCell);
      breakpointsContainer.setHeight("100%");
      breakpointsContainer.setWidth("100%");

      breakPointsPanel.addTab("breakpointstabid", new Image(DebuggerClientBundle.INSTANCE.breakPointsIcon()),
         DebuggerExtension.LOCALIZATION_CONSTANT.breakPoints(), breakpointsContainer, false);
      breakPointsPanel.setWidth("100%");
      breakPointsPanel.setHeight("100%");
      ScrollPanel scrollPanel = new ScrollPanel(frameTree);
      variabelsPanel.addTab("variabletabid", new Image(DebuggerClientBundle.INSTANCE.variable()),
         DebuggerExtension.LOCALIZATION_CONSTANT.variabels(), scrollPanel, false);
      
      vmName.setText(debuggerInfo.getVmName() + " " + debuggerInfo.getVmVersion(), Direction.RTL);
      vmName.setDirectionEstimator(true);
   }

  
   @Override
   public HasClickHandlers getResumeButton()
   {
      return resumeButton;
   }

   @Override
   public HasClickHandlers getRemoveAllBreakpointsButton()
   {
      return removeAllBreakpointsButton;
   }

   @Override
   public HasClickHandlers getDisconnectButton()
   {
      return disconnectButton;
   }

   @Override
   public void setBreakPoints(List<BreakPoint> breakPoints)
   {
      breakpointsContainer.setRowData(breakPoints);
   }

   @Override
   public void cleareVariabels()
   {
      frameTreeViewModel = new FrameTreeViewModel(selectionModel, debuggerInfo); 
      frameTree = new CellTree(frameTreeViewModel, null, res);
   }

   @Override
   public ListDataProvider<Variable> getDataProvider()
   {
      return frameTreeViewModel.getDataProvider();
   }

}