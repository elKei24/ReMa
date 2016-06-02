/*
 * Copyright (c) 2016 by Elias Keis. All rights reserved.
 */

package com.ekeis.rema.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
**  <p>This class will merge individual edits into a single larger edit.
**  That is, characters entered sequentially will be grouped together and
**  undone as a group. Any attribute changes will be considered as part
**  of the group and will therefore be undone when the group is undone.</p>
 *
 * <p>Source: <a href=https://tips4java.wordpress.com/2008/10/27/compound-undo-manager/>tips4java.wordpress.com/</a></p>
**/
public class CompoundUndoManager extends UndoManager
	implements UndoableEditListener, DocumentListener
{
	private static final Logger log = Logger.getLogger(CompoundUndoManager.class.getName());

	private CompoundEdit compoundEdit;
	private JTextComponent textComponent;
	private List<CompoundUndoManagerListener> listeners = new LinkedList<>();

	//  These fields are used to help determine whether the edit is an
	//  incremental edit. The offset and length should increase by 1 for
	//  each character added or decrease by 1 for each character removed.

	private int lastOffset;
	private int lastLength;
	private boolean combineEverything = false;

	public CompoundUndoManager(JTextComponent textComponent)
	{
		this.textComponent = textComponent;
		textComponent.getDocument().addUndoableEditListener( this );
		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();
	}

	/*
	**  Add a DocumentLister before the undo is done so we can position
	**  the Caret correctly as each edit is undone.
	*/
	@Override
	public void undo()
	{
		textComponent.getDocument().addDocumentListener( this );
		super.undo();
		textComponent.getDocument().removeDocumentListener( this );
	}

	/*
	**  Add a DocumentLister before the redo is done so we can position
	**  the Caret correctly as each edit is redone.
	*/
	@Override
	public void redo()
	{
		textComponent.getDocument().addDocumentListener( this );
		super.redo();
		textComponent.getDocument().removeDocumentListener( this );
	}

	/*
	**  Whenever an UndoableEdit happens the edit will either be absorbed
	**  by the current compound edit or a new compound edit will be started
	*/
	@Override
	public void undoableEditHappened(UndoableEditEvent e)
	{

		int offsetChange = textComponent.getCaretPosition() - lastOffset;
		int lengthChange = textComponent.getDocument().getLength() - lastLength;

		AbstractDocument.DefaultDocumentEvent event =
				(AbstractDocument.DefaultDocumentEvent)e.getEdit();

		//discard changes without real adding or removing of text
		if  (event.getType().equals(DocumentEvent.EventType.CHANGE))
		{
			return;
		}

		//  Start a new compound edit
		if (compoundEdit == null)
		{
			compoundEdit = startCompoundEdit( e.getEdit() );
			return;
		}

		//  Check for an incremental edit or backspace.
		//  The Change in Caret position and Document length should both be
		//  either 1 or -1.

//		int offsetChange = textComponent.getCaretPosition() - lastOffset;
//		int lengthChange = textComponent.getDocument().getLength() - lastLength;

		if (combineEverything || (offsetChange == lengthChange
		&&  Math.abs(offsetChange) == 1))
		{
			compoundEdit.addEdit( e.getEdit() );
			lastOffset = textComponent.getCaretPosition();
			lastLength = textComponent.getDocument().getLength();
			return;
		}

		//  Not incremental edit, end previous edit and start a new one

		compoundEdit.end();
		compoundEdit = startCompoundEdit( e.getEdit() );
	}

	/*
	**  Each CompoundEdit will store a group of related incremental edits
	**  (ie. each character typed or backspaced is an incremental edit)
	*/
	private CompoundEdit startCompoundEdit(UndoableEdit anEdit)
	{
		//  Track Caret and Document information of this compound edit

		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();

		//  The compound edit is used to store incremental edits

		compoundEdit = new MyCompoundEdit();
		compoundEdit.addEdit( anEdit );

		//  The compound edit is added to the UndoManager. All incremental
		//  edits stored in the compound edit will be undone/redone at once

		addEdit( compoundEdit );

		for (CompoundUndoManagerListener l : new ArrayList<>(listeners)) l.onEnabledChanged();

		return compoundEdit;
	}
//
//  Implement DocumentListener
//
	/*
	 *  Updates to the Document as a result of Undo/Redo will cause the
	 *  Caret to be repositioned
	 */
	@Override
	public void insertUpdate(final DocumentEvent e)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int offset = e.getOffset() + e.getLength();
				offset = Math.min(offset, textComponent.getDocument().getLength());
				textComponent.setCaretPosition( offset );
			}
		});
	}

	@Override
	public void removeUpdate(DocumentEvent e)
	{
		textComponent.setCaretPosition(e.getOffset());
	}

	@Override
	public void changedUpdate(DocumentEvent e) {}

	@Override
	public synchronized void discardAllEdits() {
		super.discardAllEdits();
		compoundEdit = null;
	}

	class MyCompoundEdit extends CompoundEdit
	{
		@Override
		public boolean isInProgress()
		{
			//  in order for the canUndo() and canRedo() methods to work
			//  assume that the compound edit is never in progress

			return false;
		}

		@Override
		public void undo() throws CannotUndoException
		{
			//  End the edit so future edits don't get absorbed by this edit

			if (compoundEdit != null)
				compoundEdit.end();

			super.undo();

			//Always start a new compound edit after an undo

			compoundEdit = null;
		}

	}

	public boolean isCombineEverything() {
		return combineEverything;
	}

	public void setCombineEverything(boolean combineEverything) {
		this.combineEverything = combineEverything;
	}

	public interface CompoundUndoManagerListener {
		void onEnabledChanged();
	}
	public void addListener(CompoundUndoManagerListener l) {
		listeners.add(l);
	}

	public void removeListener(CompoundUndoManagerListener listener) {
		listeners.remove(listener);
	}


}
