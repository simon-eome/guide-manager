package de.eome.guide.gm.model;

import de.eome.guide.api.Step;
import javafx.beans.property.StringProperty;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public interface StepItem {
    public enum Type {
        STEP,
        CHAPTER
    }
    
    public Step getNode();
    public StringProperty labelProperty();
    public Type getType();
}
