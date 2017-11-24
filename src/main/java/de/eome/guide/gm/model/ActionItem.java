package de.eome.guide.gm.model;

import de.eome.guide.api.Action;
import de.eome.guide.api.Content;
import de.eome.guide.api.Step;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class ActionItem implements StepItem {
    private final Action step;
    private final Content content;
    private final StringProperty labelProperty;
    
    public ActionItem(Action step, Content content) {
        this.step = step;
        this.content = content;
        String info = content.getDescription();
        this.labelProperty = new SimpleStringProperty(info != null ? info : "[n/a]");
    }
    
    @Override
    public Type getType() {
        return Type.STEP;
    }
    
    @Override
    public Step getNode() {
        return step;
    }
    
    public Step getStep() {
        return step;
    }
    
    public Content getContent() {
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
        final ActionItem other = (ActionItem) obj;
        if (!Objects.equals(this.step, other.step)) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        return true;
    }
}
