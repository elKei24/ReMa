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
import javax.swing.undo.*;
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
	private static final Logger log = Logger.getLogger(CompoundUndoManager.class.getName());

	//region fields etc.
	private MyCompoundEdit compoundEdit;
	private List<UndoableEdit> ignoredEdits = new LinkedList<>();
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
	private int lastHash;
	private boolean combineEverything = false;
    private boolean ignoreChanges = false;
	private boolean forceNewCompoundEdit = false;
	//endregion

	//region Constructor
	public CompoundUndoManager(JTextComponent textComponent)
	{
		this.textComponent = textComponent;
		textComponent.getDocument().addUndoableEditListener( this );
		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();
		lastHash = textComponent.getText().hashCode();
	}
	//endregion

	//region Overwriting superclass

	@Override
	public void undo()
	{
		if (compoundEdit != null) {
			for (UndoableEdit ignoredEdit : ignoredEdits) {
				compoundEdit.addEdit(ignoredEdit);
			}
			ignoredEdits.clear();
		}

		/* Add a DocumentLister before the undo is done so we can position
	    the Caret correctly as each edit is undone */
		textComponent.getDocument().addDocumentListener(caretPositioner);
		super.undo();
		textComponent.getDocument().removeDocumentListener(caretPositioner);
	}

	@Override
	public void redo()
	{
		if (compoundEdit != null) {
			for (UndoableEdit ignoredEdit : ignoredEdits) {
				compoundEdit.addEdit(ignoredEdit);
			}
			ignoredEdits.clear();
		}

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
	private class MyCompoundEdit extends CompoundEdit {

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

		/**
		 * Returns false
		 */
		@Override
		public boolean isInProgress() {
			return false;
		}

		/**
		 * Returns a description of the undoable form of this edit.
		 *
		 * @return a description of the undoable form of this edit
		 * @see     #undo
		 * @see     CompoundEdit#getUndoPresentationName
		 * @see     UndoManager#getUndoPresentationName
		 */
		public String getUndoPresentationName() {
			UndoableEdit toBeShown = editToBeShown();
			if (toBeShown != null) {
				return toBeShown.getUndoPresentationName();
			} else {
				return UIManager.getString("AbstractUndoableEdit.undoText");
			}
		}

		/**
		 * Returns a description of the redoable form of this edit.
		 *
		 * @return a description of the redoable form of this edit
		 * @see     #redo
		 * @see     CompoundEdit#getRedoPresentationName
		 * @see     UndoManager#getRedoPresentationName
		 */
		public String getRedoPresentationName() {
			UndoableEdit toBeShown = editToBeShown();
			if (toBeShown != null) {
				return toBeShown.getRedoPresentationName();
			} else {
				return UIManager.getString("AbstractUndoableEdit.redoText");
			}
		}

		protected UndoableEdit editToBeShown() {
			for (UndoableEdit edit : edits) {
				if (edit.isSignificant()) return edit;
			}
			return null;
		}
	}

	private class IgnoredEdit implements UndoableEdit {
		UndoableEdit edit;

		public IgnoredEdit(UndoableEdit otherEdit) {
			if (otherEdit == null) throw new NullPointerException("otherEdit must not be null");
			edit = otherEdit;
		}

		@Override
		public void undo() throws CannotUndoException {//add ignored edits first if there are any
			edit.undo();
		}

		/**
		 * Returns true if this edit may be undone.
		 *
		 * @return true if this edit may be undone
		 */
		@Override
		public boolean canUndo() {
			return edit.canUndo();
		}

		/**
		 * Re-applies the edit.
		 *
		 * @throws CannotRedoException if this edit can not be redone
		 */
		@Override
		public void redo() throws CannotRedoException {
			edit.redo();
		}

		/**
		 * Returns true if this edit may be redone.
		 *
		 * @return true if this edit may be redone
		 */
		@Override
		public boolean canRedo() {
			return edit.canRedo();
		}

		/**
		 * Informs the edit that it should no longer be used. Once an
		 * <code>UndoableEdit</code> has been marked as dead it can no longer
		 * be undone or redone.
		 * <p>
		 * This is a useful hook for cleaning up state no longer
		 * needed once undoing or redoing is impossible--for example,
		 * deleting file resources used by objects that can no longer be
		 * undeleted. <code>UndoManager</code> calls this before it dequeues edits.
		 * <p>
		 * Note that this is a one-way operation. There is no "un-die"
		 * method.
		 *
		 * @see CompoundEdit#die
		 */
		@Override
		public void die() {
			edit.die();
		}

		/**
		 * Adds an <code>UndoableEdit</code> to this <code>UndoableEdit</code>.
		 * This method can be used to coalesce smaller edits into a larger
		 * compound edit.  For example, text editors typically allow
		 * undo operations to apply to words or sentences.  The text
		 * editor may choose to generate edits on each key event, but allow
		 * those edits to be coalesced into a more user-friendly unit, such as
		 * a word. In this case, the <code>UndoableEdit</code> would
		 * override <code>addEdit</code> to return true when the edits may
		 * be coalesced.
		 * <p>
		 * A return value of true indicates <code>anEdit</code> was incorporated
		 * into this edit.  A return value of false indicates <code>anEdit</code>
		 * may not be incorporated into this edit.
		 * <p>Typically the receiver is already in the queue of a
		 * <code>UndoManager</code> (or other <code>UndoableEditListener</code>),
		 * and is being given a chance to incorporate <code>anEdit</code>
		 * rather than letting it be added to the queue in turn.</p>
		 * <p>
		 * <p>If true is returned, from now on <code>anEdit</code> must return
		 * false from <code>canUndo</code> and <code>canRedo</code>,
		 * and must throw the appropriate exception on <code>undo</code> or
		 * <code>redo</code>.</p>
		 *
		 * @param anEdit the edit to be added
		 * @return true if <code>anEdit</code> may be incorporated into this
		 * edit
		 */
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			return edit.addEdit(anEdit);
		}

		/**
		 * Returns true if this <code>UndoableEdit</code> should replace
		 * <code>anEdit</code>. This method is used by <code>CompoundEdit</code>
		 * and the <code>UndoManager</code>; it is called if
		 * <code>anEdit</code> could not be added to the current edit
		 * (<code>addEdit</code> returns false).
		 * <p>
		 * This method provides a way for an edit to replace an existing edit.
		 * <p>This message is the opposite of addEdit--anEdit has typically
		 * already been queued in an <code>UndoManager</code> (or other
		 * UndoableEditListener), and the receiver is being given a chance
		 * to take its place.</p>
		 * <p>
		 * <p>If true is returned, from now on anEdit must return false from
		 * canUndo() and canRedo(), and must throw the appropriate
		 * exception on undo() or redo().</p>
		 *
		 * @param anEdit the edit that replaces the current edit
		 * @return true if this edit should replace <code>anEdit</code>
		 */
		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			return edit.replaceEdit(anEdit);
		}

		/**
		 * Returns true if this edit is considered significant.  A significant
		 * edit is typically an edit that should be presented to the user, perhaps
		 * on a menu item or tooltip.  The <code>UndoManager</code> will undo,
		 * or redo, all insignificant edits to the next significant edit.
		 *
		 * @return true if this edit is significant
		 */
		@Override
		public boolean isSignificant() {
			return false;
		}

		/**
		 * Returns a localized, human-readable description of this edit, suitable
		 * for use in a change log, for example.
		 *
		 * @return description of this edit
		 */
		@Override
		public String getPresentationName() {
			return edit.getPresentationName();
		}

		/**
		 * Returns a localized, human-readable description of the undoable form of
		 * this edit, suitable for use as an Undo menu item, for example.
		 * This is typically derived from <code>getPresentationName</code>.
		 *
		 * @return a description of the undoable form of this edit
		 */
		@Override
		public String getUndoPresentationName() {
			return edit.getUndoPresentationName();
		}

		/**
		 * Returns a localized, human-readable description of the redoable form of
		 * this edit, suitable for use as a Redo menu item, for example. This is
		 * typically derived from <code>getPresentationName</code>.
		 *
		 * @return a description of the redoable form of this edit
		 */
		@Override
		public String getRedoPresentationName() {
			return edit.getRedoPresentationName();
		}


	}
	//endregion

	//region Change-handler
	@Override
	public void undoableEditHappened(UndoableEditEvent e)
	{
		/* Whenever an UndoableEdit happens the edit will either be absorbed
		by the current compound edit or a new compound edit will be started */

		UndoableEdit edit = e.getEdit();

		//region logging stuff
		if(!(edit instanceof AbstractDocument.DefaultDocumentEvent)) {
			log.finer("Change " + edit.getPresentationName() + " is no AbstractDocument.DefaultDocumentEvent");
		} else {
			AbstractDocument.DefaultDocumentEvent event =
					(AbstractDocument.DefaultDocumentEvent) edit;

			String txt;
			try {
				txt = event.getDocument().getText(event.getOffset(), event.getLength());
			} catch (BadLocationException e1) {
				txt = "ERROR";
			}
			log.finer("Change " + (ignoreChanges ? "ignored" : "detected") + ": " + edit.getPresentationName()
					+ "\n\t Type: " + event.getType().toString()
					+ "\n\t Offset: " + event.getOffset()
					+ "\n\t Length: " + event.getLength()
					+ "\n\t Text: \"" + txt + "\""
			);
		}
		//endregion

		//region offset stuff
		int offsetChange = textComponent.getCaretPosition() - lastOffset;
		int lengthChange = textComponent.getDocument().getLength() - lastLength;
		lastOffset = textComponent.getCaretPosition();
		lastLength = textComponent.getDocument().getLength();
		//endregion

		//region Hash Change Detection
		boolean noHashChange = textComponent.getText().hashCode() == lastHash;
		lastHash = textComponent.getText().hashCode();
		//endregion

		//region handle change
		if (ignoreChanges || noHashChange) {
			edit = new IgnoredEdit(edit);
			if (compoundEdit == null) {
				ignoredEdits.add(edit);
			} else {
				compoundEdit.addEdit(edit);
			}
		} else {
			if (forceNewCompoundEdit || compoundEdit == null || (!combineEverything && (offsetChange != lengthChange || Math.abs(offsetChange) != 1))) {
				forceNewCompoundEdit = false;

				//Not incremental edit, end previous edit and start a new one
				if (compoundEdit != null) compoundEdit.end();
				compoundEdit = new MyCompoundEdit();

				//add ignored edits first if there are any
				for (UndoableEdit ignoredEdit : ignoredEdits) {
					compoundEdit.addEdit(ignoredEdit);
				}
				ignoredEdits.clear();

				compoundEdit.addEdit(edit);

				addEdit(compoundEdit);
			} else {
				//incremental edit
				compoundEdit.addEdit(edit);
			}
		}
		//endregion

		for (CompoundUndoManagerListener l : new ArrayList<>(listeners)) l.onEnabledChanged();
	}

	public void forceNewCompoundEdit() {
		forceNewCompoundEdit = true;
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
