/*
 * jEdit - Programmer's Text Editor
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright © 2011-2012 Matthieu Casanova
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.kpouer.jedit.smartopen;

//{{{ 
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import common.gui.itemfinder.AbstractItemFinder;
import common.gui.itemfinder.PathCellRenderer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.textarea.JEditTextArea;
//}}}

/**
 * @author Matthieu Casanova
 */
public class FileItemFinder extends AbstractItemFinder<String>
{
	private final MyListModel model;
	private final ListCellRenderer listCellRenderer;
	private final FileIndex itemFinder;

	private String position;

	//{{{ FileItemFinder constructor
	public FileItemFinder(FileIndex itemFinder)
	{
		model = new MyListModel();
		listCellRenderer = new PathCellRenderer();
		this.itemFinder = itemFinder;
	} //}}}

	//{{{ getLabel() method
	@Override
	public String getLabel()
	{
		return jEdit.getProperty("search-file.label");
	} //}}}

	//{{{ getModel() method
	@Override
	public ListModel getModel()
	{
		return model;
	} //}}}

	//{{{ updateList() method
	@Override
	public void updateList(String search)
	{
		int index = search.indexOf(':');
		position = null;
		if (index != -1)
		{
			if (index < search.length())
			{
				position = search.substring(index + 1);
			}
			search = search.substring(0, index);
		}
		List<String> files = itemFinder.getFiles(search);
		model.setData(files);
	} //}}}

	//{{{ selectionMade() method
	@Override
	public void selectionMade(final String path)
	{
		if (position != null)
		{
			final String[] split = position.split(",");
			try
			{
				final int _seletedLine = Integer.parseInt(split[0]) - 1;
				EditBus.addToBus(new EBComponent()
				{
					@Override
					public void handleMessage(EBMessage message)
					{
						if (message instanceof BufferUpdate)
						{
							BufferUpdate bufferUpdate = (BufferUpdate) message;
							if (bufferUpdate.getWhat() == BufferUpdate.LOADED &&
								bufferUpdate.getBuffer().getPath().equals(path))
							{
								EditBus.removeFromBus(this);
								JEditTextArea textArea = jEdit.getActiveView().getEditPane().getTextArea();
								int caret = textArea.getLineStartOffset(_seletedLine);
								if (caret == -1)
									return;

								if (split.length > 0)
								{
									int offset = Integer.parseInt(split[1]);
									if (offset < textArea.getLineLength(_seletedLine))
									{
										caret += offset;
									}
								}
								textArea.setCaretPosition(caret);
							}
						}
					}
				});
			}
			catch (NumberFormatException e)
			{
			}
		}
		jEdit.openFile(jEdit.getActiveView().getEditPane(), path);
	} //}}}

	//{{{ getListCellRenderer() method
	@Override
	public ListCellRenderer getListCellRenderer()
	{
		return listCellRenderer;
	} //}}}

	//{{{ MyListModel class
	private static class MyListModel extends AbstractListModel
	{
		private List<String> data;

		//{{{ MyListModel constructor
		private MyListModel()
		{
			data = new ArrayList<String>();
		} //}}}

		//{{{ setData() method
		public void setData(List<String> data)
		{
			this.data = data;
			fireContentsChanged(this, 0, data.size());
		} //}}}

		//{{{ getSize() method
		@Override
		public int getSize()
		{
			return data.size();
		} //}}}

		//{{{ getElementAt() method
		@Override
		public Object getElementAt(int index)
		{
			return data.get(index);
		} //}}}
	} //}}}
}
