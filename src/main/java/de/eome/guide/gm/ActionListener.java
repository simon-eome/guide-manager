package de.eome.guide.gm;

/**
 * Interface for model change listeners.
 * @author simon.schwantzer(at)im-c.de
 */
public interface ActionListener {
    /**
     * Method called when a change has been performed.
     * @param change Change performed.
     */
    public void changePerformed(Action change);
    
    /**
     * Method called when a change has been reverted.
     * @param change Change undone.
     */
    public void changeUndone(Action change);
    
    /**
     * Methos called if the change failed.
     * @param change Failed change.
     * @param e Cause of failure.
     */
    public void changeFailed(Action change, ActionFailedException e);
    
    /**
     * Method called if the undoing of a change failed.
     * @param change Change to be undone.
     * @param e Cause of failure.
     */
    public void undoneFailed(Action change, ActionFailedException e);
}
