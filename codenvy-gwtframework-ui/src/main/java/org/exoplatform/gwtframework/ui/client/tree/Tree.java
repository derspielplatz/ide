/**
 * Copyright (C) 2009 eXo Platform SAS.
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
 *
 */
package org.exoplatform.gwtframework.ui.client.tree;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class Tree extends Composite
{

   /**
    * Tree styles
    */
   public interface Style
   {

      static final String PANEL = "exo-tree-panel";

   }

   /**
    * Component panel
    */
   private FlowPanel treePanel = new FlowPanel();

   //   private class TreePanel extends FlowPanel
   //   {
   //      
   //      public TreePanel()
   //      {
   //         sinkEvents(Event.)
   //      }
   //      
   //   }

   /**
    * Panel for calculating width of tree content
    */
   private FlowPanel calcPanel = new FlowPanel();

   /**
    * Root record
    */
   private TreeRecord root;

   /**
    * Width of table
    */
   private int tableVisibleWidth = -1;

   /**
    * Allow multiselection
    */
   private boolean allowMultiSelect = false;

   /**
    * Selected records
    */
   private List<TreeRecord> selectedRecords = new ArrayList<TreeRecord>();

   /**
    * Record which last clicked
    */
   private TreeRecord lastSelectedRecord;

   /**
    * Currently overed record
    */
   private TreeRecord overedRecord;

   /**
    * Boolean value for selecting range of elements ( with shift key )
    */
   private boolean selectionStarted = false;

   /**
    * Empty tree prompt
    */
   private String emptyMessage = "Empty";

   /**
    * Constructor
    */
   public Tree()
   {
      DOM.setStyleAttribute(treePanel.getElement(), "background", "#FFFFFF");
      DOM.setStyleAttribute(treePanel.getElement(), "border", "#AAAAAA 1px solid");
      DOM.setStyleAttribute(treePanel.getElement(), "overflow", "auto");

      initWidget(treePanel);

      DOM.setStyleAttribute(calcPanel.getElement(), "width", "100%");
      DOM.setStyleAttribute(calcPanel.getElement(), "height", "100%");
      treePanel.add(calcPanel);
   }

   /**
    * Allow multiselection
    * 
    * @return
    */
   public boolean isAllowMultiSelect()
   {
      return allowMultiSelect;
   }

   /**
    * Set allow multiselection
    * 
    * @param allowMultiSelect
    */
   public void setAllowMultiSelect(boolean allowMultiSelect)
   {
      this.allowMultiSelect = allowMultiSelect;
   }

   public String getEmptyMessage()
   {
      return emptyMessage;
   }

   public void setEmptyMessage(String emptyMessage)
   {
      this.emptyMessage = emptyMessage;

      if (root == null)
      {
         showEmptyMessage();
      }
   }

   public List<TreeRecord> getSelectedRecords()
   {
      return selectedRecords;
   }

   public TreeRecord getSelectedRecord()
   {
      if (selectedRecords.size() == 0)
      {
         return null;
      }

      return selectedRecords.get(0);
   }

   /**
    * Set RootNode
    * 
    * @param treeNode
    */
   public void setRoot(TreeNode treeNode)
   {
      if (treeNode == null)
      {
         calcPanel.clear();
         root = null;
         showEmptyMessage();
         return;
      }

      root = new TreeRecord(this, treeNode, 0);
      calcPanel.clear();
      calcPanel.add(root);
      updateWidthTimer.schedule(10);
   }

   public TreeRecord getRootRecord()
   {
      return root;
   }

   /**
    * Return Root Node
    * 
    * @return
    */
   public TreeNode getRoot()
   {
      if (root == null)
      {
         return null;
      }

      return root.getNode();
   }

   private Timer updateWidthTimer = new Timer()
   {
      @Override
      public void run()
      {
         tableVisibleWidth = calcPanel.getOffsetWidth();
         updateRowsWidth();
      }
   };

   private void showEmptyMessage()
   {
      if (emptyMessage == null)
      {
         calcPanel.clear();
         return;
      }

      Grid emptyGrid = new Grid(1, 1);
      emptyGrid.setHTML(0, 0, emptyMessage);

      emptyGrid.setWidth("100%");
      emptyGrid.setHeight("20px");

      emptyGrid.getRowFormatter().setStyleName(0, TreeRecord.Style.TITLE_TABLE_TR);
      emptyGrid.getCellFormatter().setStyleName(0, 0, TreeRecord.Style.TITLE_TABLE_TD);

      DOM.setStyleAttribute(emptyGrid.getCellFormatter().getElement(0, 0), "textAlign", "center");

      calcPanel.add(emptyGrid);
   }

   public void updateRowsWidth(int tableVisibleWidth)
   {
      this.tableVisibleWidth = tableVisibleWidth;
      updateRowsWidth();
   }

   /**
    * Refresh width of visible rows
    * 
    */
   public void updateRowsWidth()
   {
      if (root == null)
      {
         return;
      }

      int maxWidth = getMaxWidth(root, tableVisibleWidth);
      updateWidth(root, maxWidth);
   }

   /**
    * Return max width of visible rows
    * 
    * @param record
    * @param width
    * @return
    */
   private int getMaxWidth(TreeRecord record, int width)
   {
      int maxWidth = record.getTitlePanelWidth();

      if (width > maxWidth)
      {
         maxWidth = width;
      }

      if (record.isSubtreePanelVisible())
      {
         for (TreeRecord r : record.getChildren())
         {
            maxWidth = getMaxWidth(r, maxWidth);
         }
      }

      return maxWidth;
   }

   /**
    * Update width of rows
    * 
    * @param record
    * @param width
    */
   private void updateWidth(TreeRecord record, int width)
   {
      record.refreshWidth(width);
      for (TreeRecord r : record.getChildren())
      {
         updateWidth(r, width);
      }
   }

   /**
    * On Mouse Over handler.
    * Must be called by TreeRecord.
    */
   public void recordMouseOver(TreeRecord treeRecord)
   {
      doOutTimer.cancel();

      if (overedRecord == null)
      {
         overedRecord = treeRecord;
         treeRecord.setStyleOver();
      }
      else
      {
         if (overedRecord == treeRecord)
         {
            return;
         }

         overedRecord.setStyleNormal();
         overedRecord = treeRecord;
         treeRecord.setStyleOver();
      }
   }

   /**
    * On Mouse Out handler.
    * Must be called by TreeRecord.
    */
   public void recordMouseOut(TreeRecord treeRecord)
   {
      doOutTimer.cancel();
      doOutTimer.schedule(10);
   }

   /**
    *  
    */
   private Timer doOutTimer = new Timer()
   {
      @Override
      public void run()
      {
         if (overedRecord != null)
         {
            overedRecord.setStyleNormal();
            overedRecord = null;
         }
      }
   };

   /**
    * Select one record
    * 
    * @param treeRecord
    */
   private void selectOne(TreeRecord treeRecord)
   {
      for (TreeRecord r : selectedRecords)
      {
         r.setSelected(false);
         r.setStyleNormal();
      }
      selectedRecords.clear();

      selectedRecords.add(treeRecord);

      treeRecord.setSelected(true);
      treeRecord.setStyleOver();

      lastSelectedRecord = treeRecord;
   }

   /**
    * Append record to selection 
    * @param treeRecord
    */
   private void appendSelection(TreeRecord treeRecord)
   {
      if (selectedRecords.contains(treeRecord))
      {
         selectedRecords.remove(treeRecord);
         treeRecord.setSelected(false);
      }
      else
      {
         selectedRecords.add(treeRecord);
         treeRecord.setSelected(true);
      }

      treeRecord.setStyleOver();

      lastSelectedRecord = treeRecord;
   }

   public void recordMouseDown(TreeRecord treeRecord, boolean ctrlKey, boolean shiftKey)
   {
      if (allowMultiSelect)
      {
         if (ctrlKey)
         {
            appendSelection(treeRecord);
         }
         else if (shiftKey)
         {
            doShiftSelection(treeRecord);
         }
         else
         {
            selectOne(treeRecord);
         }
      }
      else
      {
         selectOne(treeRecord);
      }

   }

   private void doShiftSelection(TreeRecord treeRecord)
   {
      if (treeRecord == lastSelectedRecord)
      {
         return;
      }

      traverseTree(root, treeRecord);
   }

   /**
    * Select one record
    * 
    * @param record
    * @param isOver
    */
   private void select(TreeRecord record, boolean isOver)
   {
      if (!selectedRecords.contains(record))
      {
         selectedRecords.add(record);
         record.setSelected(true);
         if (isOver)
         {
            record.setStyleOver();
         }
         else
         {
            record.setStyleNormal();
         }
      }
   }

   /**
    * Traverse tree and select specific range
    * 
    * @param parentRecord
    * @param recordToSelect
    */
   private void traverseTree(TreeRecord parentRecord, TreeRecord recordToSelect)
   {
      if (parentRecord == recordToSelect || parentRecord == lastSelectedRecord)
      {
         if (selectionStarted)
         {
            selectionStarted = false;
            select(parentRecord, true);
            return;
         }
         else
         {
            selectionStarted = true;
            select(parentRecord, true);
         }
      }

      if (selectionStarted)
      {
         select(parentRecord, false);
      }

      if (parentRecord.isSubtreePanelVisible())
      {
         for (TreeRecord record : parentRecord.getChildren())
         {
            traverseTree(record, recordToSelect);
         }
      }

   }

   /**
    * Mouse double click handler
    */
   public void onDoubleClick(TreeRecord treeRecord)
   {
      selectOne(treeRecord);

      if (treeRecord.isExpanded())
      {
         treeRecord.collapse();
      }
      else
      {
         treeRecord.expand();
      }
   }

   public void recordMouseUp(TreeRecord treeRecord)
   {
   }

   public void onClick(TreeRecord treerecord)
   {
   }

   /**
    * Override this method to complete open node handling
    * 
    */
   public void onExpand(TreeRecord treeRecord)
   {
   }

   public void onCollapse(TreeRecord treeRecord)
   {
   }

}
