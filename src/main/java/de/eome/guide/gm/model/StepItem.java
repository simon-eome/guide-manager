package de.eome.guide.gm.model;

import de.glassroom.gpe.Node;
import javafx.beans.property.StringProperty;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public interface NodeItem {
    public enum Type {
        STEP,
        CHAPTER
    }
    
    public Node getNode();
    public StringProperty labelProperty();
    public Type getType();
}
