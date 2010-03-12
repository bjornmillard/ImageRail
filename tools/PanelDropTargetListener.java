package tools;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import main.MainGUI;
import main.Plate;

public class PanelDropTargetListener implements DropTargetListener {
	public Plate ThePlate;

	private static final String URI_LIST_MIME_TYPE = "text/uri-list;class=java.lang.String";

	public void dragEnter(DropTargetDragEvent event) {
		if (!isDragAcceptable(event)) {
			event.rejectDrag();
			return;
		}
	}

	public void dragExit(DropTargetEvent event) {
	}

	public void dragOver(DropTargetDragEvent event) {
	}

	public void dropActionChanged(DropTargetDragEvent event) {
		if (!isDragAcceptable(event)) {
			event.rejectDrag();
			return;
		}
	}

	public void drop(DropTargetDropEvent event)
	{
		if (!isDropAcceptable(event))
		{
			event.rejectDrop();
			return;
		}
		
		event.acceptDrop(DnDConstants.ACTION_COPY);

	    DataFlavor uriListFlavor = null;
	    try
	    {
	      uriListFlavor = new DataFlavor(URI_LIST_MIME_TYPE);
	    }
	    catch (ClassNotFoundException e)
	    {
	      e.printStackTrace();
	    }

		Transferable transferable = event.getTransferable();

		try
		{
			List<File> fileList = null;
			
			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				fileList = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
			}
			else if (transferable.isDataFlavorSupported(uriListFlavor))
			{
				String data = (String) transferable.getTransferData(uriListFlavor);
				fileList = textURIListToFileList(data);
			}

			if (fileList != null)
			{
				Iterator<File> iterator = fileList.iterator();
				while (iterator.hasNext())
				{
					File f = iterator.next();
					MainGUI.getGUI().load(f, ThePlate);
				}
			}
			else
			{
				System.out.println("Drag and drop error: Could not find any flavor I understand\n");
			}
		}
		catch (Exception e)
		{
			System.out.println("Drag and drop error: " + e + "\n");
		}
		
		event.dropComplete(true);
	}

	public boolean isDragAcceptable(DropTargetDragEvent event) { // usually, you
		// check the
		// available
		// data flavors
		// here
		// in this program, we accept all flavors
		return (event.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
	}

	public boolean isDropAcceptable(DropTargetDropEvent event) { // usually, you
		// check the
		// available
		// data flavors
		// here
		// in this program, we accept all flavors
		return (event.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0;
	}

	private static List<File> textURIListToFileList(String data) {
		List<File> list = new ArrayList<File>(1);
		for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) {
			String s = st.nextToken();
			if (s.startsWith("#")) {
				// the line is a comment (as per the RFC 2483)
				continue;
			}
			try {
				URI uri = new URI(s);
				File file = new File(uri);
				list.add(file);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

}
