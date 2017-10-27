package de.eome.guide.gm.model;

import de.glassroom.gpe.Node;
import de.glassroom.gpe.Step;
import de.glassroom.gpe.content.ContentDescriptor;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class StepItem implements NodeItem {
    private final Step step;
    private final ContentDescriptor content;
    private final StringProperty labelProperty;
    
    public StepItem(Step step, ContentDescriptor content) {
        this.step = step;
        this.content = content;
        String info = content.getInfo();
        this.labelProperty = new SimpleStringProperty(info != null ? info : "[n/a]");
    }
    
    @Override
    public Type getType() {
        return Type.STEP;
    }
    
    @Override
    public Node getNode() {
        return step;
    }
    
    public Step getStep() {
        return step;
    }
    
    public ContentDescriptor getContent() {
        return content;
    }
    
    @Override
    public StringProperty labelProperty() {
        return labelProperty;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.step);
        hash = 29 * hash + Objects.hashCode(this.content);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StepItem other = (StepItem) obj;
        if (!Objects.equals(this.step, other.step)) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        return true;
    }
}
