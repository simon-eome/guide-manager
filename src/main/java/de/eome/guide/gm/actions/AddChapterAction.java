package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.glassroom.gpe.Chapter;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.Node;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class AddChapterAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(AddChapterAction.class.getName());
    
    private final PersistenceHandler persistenceHandler;
    private final Guide guide;
    private final Node predecessor;
    private final String target;
    private Chapter chapter;
    
    public AddChapterAction(String target, Node predecessor, Guide guide, PersistenceHandler persistenceHandler) {
        this.target = target;
        this.persistenceHandler = persistenceHandler;
        this.predecessor = predecessor;
        this.guide = guide;
    }
    
    @Override
    public void perform() {
        try {
            chapter = new Chapter(target);
            guide.addNode(chapter, predecessor);
                        
            // Persist changes of guide.
            persistenceHandler.writeGuide(guide);
            notifyActionPerformed();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to persist content or guide.", e);
            notifyActionFailed(new ActionFailedException("Failed to persist content or guide.", e));
        }
    }

    @Override
    public void undo() {
        try {
            guide.removeNode(chapter);
            persistenceHandler.writeGuide(guide);
            notifyActionUndone();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to persist guide.", e);
            notifyUndoFailed(new ActionFailedException("Failed to write guide.", e));
        }
    }

    @Override
    public String getDescription() {
        return "Kapitel erstellen";
    }
    
}
