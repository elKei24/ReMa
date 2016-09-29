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
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
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
	implements UndoableEditListener
{
	//FIXME: der scheiß gad ums verrecken ned, des kann doch ned so schwer sein einfach die CHANGE events zu ignorieren :(
	private static final Logger log = Logger.getLogger(CompoundUndoManager.class.getName());

	//region fields etc.
	private CompoundEdit compoundEdit;
	private JTextComponent textComponent;
	private List<CompoundUndoManagerListener> listeners = new LinkedList<>();
	private final DocumentListener caretPositioner = new DocumentListener() {
		@Override
		public void removeUpdate(DocumentEvent e)
		{
			textComponent.setCaretPosition(e.getOffset());
		}

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
					int caret = Math.min(e.getOffset() + e.getLength(), textComponent.getDocument().getLength());
					textComponent.setCaretPosition(caret);
				}
			});
		}

		@Override
		public void changedUpdate(DocumentEvent e) {}
	};

	private int lastOffset;
	private int lastLength;
	private boolean combineEverything = false;
    private boolean ignoreChanges = false;
	//endregion

	//region Constructor
	public CompoundUndoManager(JTextComponent textComponent)
	{
		this.textComponent = textComponent;
		textComponent.getDocument().addUndoableEditListener( this );
		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();
	}
	//endregion

	//region Overwriting superclass

	@Override
	public void undo()
	{
		/* Add a DocumentLister before the undo is done so we can position
	    the Caret correctly as each edit is undone */
		textComponent.getDocument().addDocumentListener(caretPositioner);
		super.undo();
		textComponent.getDocument().removeDocumentListener(caretPositioner);
	}

	@Override
	public void redo()
	{
		/* Add a DocumentLister before the redo is done so we can position
	    the Caret correctly as each edit is redone. */
		textComponent.getDocument().addDocumentListener(caretPositioner);
		super.redo();
		textComponent.getDocument().removeDocumentListener(caretPositioner);
	}

	@Override
	public synchronized void discardAllEdits() {
		CompoundUndoManager.super.discardAllEdits();
		compoundEdit = null;
	}
	//endregion

	//region Own compound edit classes

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

	private class IgnoredCompoundEdit extends CompoundEdit {
		/**
		 * Returns false.
		 */
		@Override
		public boolean isSignificant() {
			return false;
		}

		@Override
		/**
		 * Returns false.
		 */
		public boolean isInProgress()
		{

			return false;
		}
	}
	//endregion

	//region Change-handler
	@Override
	public void undoableEditHappened(UndoableEditEvent e)
	{
		/* Whenever an UndoableEdit happens the edit will either be absorbed
		by the current compound edit or a new compound edit will be started */

		//region logging stuff
		assert e.getEdit() instanceof AbstractDocument.DefaultDocumentEvent;
		AbstractDocument.DefaultDocumentEvent event =
				(AbstractDocument.DefaultDocumentEvent)e.getEdit();

		String txt;
		try {
			txt = event.getDocument().getText(event.getOffset(), event.getLength());
		} catch (BadLocationException e1) {
			txt = "ERROR";
		}
		log.finer("Change " + (ignoreChanges ? "ignored" : "detected") + ": " + e.getEdit().getPresentationName()
				+ "\n\t Type: " + event.getType().toString()
				+ "\n\t Offset: " + event.getOffset()
				+ "\n\t Length: " + event.getLength()
				+ "\n\t Text: \"" + txt + "\""
		);
		//endregion

		//region offset stuff
		int offsetChange = textComponent.getCaretPosition() - lastOffset;
		int lengthChange = textComponent.getDocument().getLength() - lastLength;
		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();
		//endregion

		//region handle change
		if (ignoreChanges) {
			CompoundEdit ignoredEdit = new IgnoredCompoundEdit();
			ignoredEdit.addEdit(e.getEdit());

			addEdit(ignoredEdit);
			/* int index = edits.indexOf(ignoredEdit);
			if (index >= 0) trimEdits(index, index); */
		} else if (compoundEdit == null || (!combineEverything && (offsetChange != lengthChange || Math.abs(offsetChange) != 1))) {
			//Not incremental edit, end previous edit and start a new one
			if (compoundEdit != null) compoundEdit.end();

			compoundEdit = new MyCompoundEdit();
			compoundEdit.addEdit(e.getEdit());

			addEdit( compoundEdit );
		} else {
			//incremental edit
			compoundEdit.addEdit(e.getEdit());
		}
		//endregion

		for (CompoundUndoManagerListener l : new ArrayList<>(listeners)) l.onEnabledChanged();
	}
	//endregion

	//region Get/Set/Listener

	public boolean isCombineEverything() {
		return combineEverything;
	}

	public void setCombineEverything(boolean combineEverything) {
		this.combineEverything = combineEverything;
	}

    public boolean isIgnoreChanges() {
        return ignoreChanges;
    }

    public void setIgnoreChanges(boolean ignoreChanges) {
        this.ignoreChanges = ignoreChanges;
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
	//endregion


}
