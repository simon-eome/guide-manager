package de.eome.guide.gm;

/**
 * Interface for a model change.
 * @author simon.schwantzer(at)im-c.de
 */
public interface Action {
    public void perform();
    public void undo();
    public String getDescription();
}
