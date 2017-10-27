package de.eome.guide.gm.actions;

import de.eome.guide.gm.Action;
import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.ActionListener;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation to handle change listeners.
 * @author simon.schwantzer(at)im-c.de
 */
public abstract class BaseAction implements Action {
    private static final Logger LOGGER = Logger.getLogger(BaseAction.class.getName());
    
    private Set<ActionListener> listeners;
    
    public BaseAction() {
        this.listeners = new LinkedHashSet<>();
    }
    
    /**
     * Adds an action listener.
     * @param listener Listener to add.
     */
    public void addActionListener(ActionListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes an action listener.
     * @param listener Listener to remove.
     */
    public void removeActionListener(ActionListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Returns all action listeners.
     * @return Insertion ordered set of registered listeners. May be <code>null</code>.
     */
    public Set<ActionListener> getActionListeners() {
        return listeners;
    }
    
    public void notifyActionPerformed() {
        listeners.forEach((listener) -> {
            try {
                listener.changePerformed(this);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to notifiy action listener." , e);
            }
        });
    }
    
    public void notifyActionUndone() {
        listeners.forEach((listener) -> {
            try {
                listener.changeUndone(this);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to notifiy action listener." , e);
            }
        });
    }
    
    public void notifyActionFailed(ActionFailedException cause) {
        listeners.forEach((listener) -> {
            try {
                listener.changeFailed(this, cause);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to notifiy action listener." , e);
            }
        });
    }
    
    public void notifyUndoFailed(ActionFailedException cause) {
        listeners.forEach((listener) -> {
            try {
                listener.undoneFailed(this, cause);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to notifiy action listener." , e);
            }
        });
    }
}
