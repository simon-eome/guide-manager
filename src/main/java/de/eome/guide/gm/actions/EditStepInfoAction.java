package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.model.ContentModel;
import de.glassroom.gpe.Guide;
import java.io.IOException;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class EditStepInfoAction extends BaseAction {
    private final PersistenceHandler persistenceHandler;
    private final Guide guide;
    private final ContentModel content;
    private final String newText;
    private final String oldText;
    
    public EditStepInfoAction(String newText, ContentModel content, Guide guide, PersistenceHandler persistenceHandler) {
        this.persistenceHandler = persistenceHandler;
        this.guide = guide;
        this.content = content;
        this.newText = newText;
        this.oldText = content.infoProperty().get();
    }

    @Override
    public void perform() {
        content.infoProperty().set(newText);
        
        try {
            persistenceHandler.writeContentDescriptor(guide, content.getBean());
            notifyActionPerformed();
        } catch (IOException e) {
            notifyActionFailed(new ActionFailedException("Failed to update content descriptor.", e));
        }
    }

    @Override
    public void undo() {
        content.infoProperty().set(oldText);
        
        try {
            persistenceHandler.writeContentDescriptor(guide, content.getBean());
            notifyActionUndone();
        } catch (IOException e) {
            notifyUndoFailed(new ActionFailedException("Failed to update content descriptor.", e));
        }
    }

    @Override
    public String getDescription() {
        return "Beschreibung ändern";
    }
    
}
