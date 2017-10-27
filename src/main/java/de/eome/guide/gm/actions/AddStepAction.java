package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.MainApp;
import de.eome.guide.gm.PersistenceHandler;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.Node;
import de.glassroom.gpe.Step;
import de.glassroom.gpe.annotations.ContentAnnotation;
import de.glassroom.gpe.content.ContentDescriptor;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class AddStepAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(AddStepAction.class.getName());
    
    private final PersistenceHandler persistenceHandler;
    private final Guide guide;
    private final Node predecessor;
    private Step newStep;
    private ContentDescriptor content;
    
    public AddStepAction(Node predecessor, Guide guide, PersistenceHandler persistenceHandler) {
        this.persistenceHandler = persistenceHandler;
        this.predecessor = predecessor;
        this.guide = guide;
    }
    
    @Override
    public void perform() {
        try {
            newStep = new Step();
            
            // Create new content package.
            content = new ContentDescriptor(UUID.randomUUID().toString(), MainApp.LANG);
            persistenceHandler.writeContentDescriptor(guide, content);
            
            // Create new step.
            ContentAnnotation contentAnnotation = new ContentAnnotation();
            contentAnnotation.setContentPackage(MainApp.LANG, content.getId());
            newStep.setContent(contentAnnotation);
            
            // Add step to guide.
            guide.addNode(newStep, predecessor);
            
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
            persistenceHandler.deleteContentPackage(guide.getId(), content.getId());
            guide.removeNode(newStep);
            persistenceHandler.writeGuide(guide);
            notifyActionUndone();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to persist guide.", e);
            notifyUndoFailed(new ActionFailedException("Failed to write guide.", e));
        }
    }

    @Override
    public String getDescription() {
        return "Schritt erstellen";
    }
    
}
