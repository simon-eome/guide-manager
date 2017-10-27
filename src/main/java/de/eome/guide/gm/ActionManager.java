package de.eome.guide.gm;

import java.util.Stack;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Manager for model changes.
 * @author Schwantzer
 */
public class ActionManager {
    private static final int MAX_CHANGES = 20;
    
    private final Stack<Action> changes;
    private final BooleanProperty hasChangesProperty;
    private final StringProperty descriptionProperty;
    
    public ActionManager() {
        changes = new Stack<>();
        hasChangesProperty = new SimpleBooleanProperty();
        descriptionProperty = new SimpleStringProperty();
    }
    
    /**
     * Checks if any changes can be undone.
     * @return <code>true</code> if undoable changes are available, otherwise <code>false</code>.
     */
    public boolean hasChanges() {
        return hasChangesProperty.get();
    }
    
    /**
     * Return a property indicating the availability of an undo operation.
     * @return Property which is <code>true</code> if an undo operation is available, otherwise <code>false</code>.
     */
    public BooleanProperty hasChangesProperty() {
        return hasChangesProperty;
    }
    
    /**
     * Performs a change and adds it to the stack of undoable changes.
     * @param change Change to perform.
     */
    public void performChange(Action change) {
        change.perform();
        changes.push(change);
        hasChangesProperty.set(true);
        if (changes.size() > MAX_CHANGES) {
            changes.remove(0);
        }
        descriptionProperty.set(change.getDescription());
    }
    
    /**
     * Undo the latest change.
     * @throws IllegalStateException No undoable changes available.
     */
    public void undoLatestChange() throws IllegalStateException {
        checkUndoAvailable();
        Action change = changes.pop();
        change.undo();
        if (!changes.isEmpty()) {
            descriptionProperty.set(changes.peek().getDescription());
            hasChangesProperty.set(true);
        } else {
            descriptionProperty.set(null);
            hasChangesProperty.set(false);
        }
    }
    
    /**
     * Returns the latest change.
     * @throws IllegalStateException No undoable changes available.
     * @return Latest change applied.
     */
    public Action getLastestChange() throws IllegalStateException {
        checkUndoAvailable();
        return changes.peek();
    }
    
    private void checkUndoAvailable() throws IllegalStateException {
        if (changes.isEmpty()) {
            throw new IllegalStateException("No undoable changes available.");
        }
    }
    
    public StringProperty descriptionProperty() {
        return descriptionProperty;
    }
}
