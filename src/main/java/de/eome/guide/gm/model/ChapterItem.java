package de.eome.guide.gm.model;

import de.glassroom.gpe.Chapter;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.Node;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class ChapterItem implements NodeItem {
    private final Chapter chapter;
    private final Guide target;
    private final StringProperty labelProperty;
    
    public ChapterItem(Chapter chapter, Guide target) {
        this.chapter = chapter;
        this.target = target;
        this.labelProperty = new SimpleStringProperty(target.getMetadata().getTitle("de_DE"));
    }

    @Override
    public Node getNode() {
        return chapter;
    }

    @Override
    public StringProperty labelProperty() {
        return labelProperty;
    }

    @Override
    public Type getType() {
        return Type.CHAPTER;
    }
    
    public Guide getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.chapter);
        hash = 29 * hash + Objects.hashCode(this.target);
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
        final ChapterItem other = (ChapterItem) obj;
        if (!Objects.equals(this.chapter, other.chapter)) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        return true;
    }    
}
