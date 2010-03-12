/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ideall.client.component;

import java.util.List;

import org.exoplatform.gwtframework.ui.client.smartgwt.component.ListGrid;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:tnemov@gmail.com">Evgen Vidolob</a>
 * @version $Id: $
*/
public class WadlParameterEntryListGrid extends ListGrid<WadlParameterEntry> implements EditCompleteHandler
{

   HandlerRegistration editCompleteHandler;

   public WadlParameterEntryListGrid()
   {
      setHeaderHeight(22);

      ListGridField fieldName = new ListGridField("name", "Name");
      fieldName.setAlign(Alignment.LEFT);
      ListGridField fieldType = new ListGridField("type", "Type");
      fieldName.setAlign(Alignment.LEFT);
      ListGridField fieldValue = new ListGridField("value", "Value");
      fieldValue.setAlign(Alignment.LEFT);
      setData(new ListGridRecord[0]);
      setFields(fieldName, fieldType, fieldValue);

      setShowHeader(true);

      editCompleteHandler = addEditCompleteHandler(this);
   }

   protected void fireOnValueChangeEvent()
   {
      for (ValueChangeHandler<List<WadlParameterEntry>> handler : valueChangeHandlers)
      {
         //handler.onValueChange(new ValueChangeEvent(<List<WadlParameterEntry>>(items));
      }
   }

   @Override
   protected void setRecordFields(ListGridRecord record, WadlParameterEntry item)
   {
      record.setAttribute("name", item.getName());
      record.setAttribute("type", item.getType());
      record.setAttribute("value", item.getValue());
   }

   public void onEditComplete(EditCompleteEvent event)
   {
      editCompleteHandler.removeHandler();

      WadlParameterEntry listItem =
         (WadlParameterEntry)event.getOldRecord().getAttributeAsObject(getValuePropertyName());
      if (event.getNewValues().get("name") != null)
      {
         listItem.setName("" + event.getNewValues().get("name"));
      }
      if (event.getNewValues().get("value") != null)
      {
         listItem.setValue("" + event.getNewValues().get("value"));
      }
      if (event.getNewValues().get("type") != null)
      {
         listItem.setType("" + event.getNewValues().get("type"));
      }
      fireOnValueChangeEvent();
      editCompleteHandler = addEditCompleteHandler(this);
   }

}
