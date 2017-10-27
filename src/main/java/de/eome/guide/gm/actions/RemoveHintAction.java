package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.model.ContentModel;
import de.eome.guide.gm.model.GuideModel;
import de.eome.guide.gm.model.HintModel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class RemoveHintAction extends BaseAction {
    private final static Logger LOGGER = Logger.getLogger(RemoveHintAction.class.getName());
    
    private final HintModel hint;
    private final PersistenceHandler persistenceHandler;
    private final GuideModel guide;
    private final ContentModel content;
    
    public RemoveHintAction(HintModel hint, ContentModel content, GuideModel guideModel, PersistenceHandler persistenceHandler) {
        this.hint = hint;
        this.persistenceHandler = persistenceHandler;
        this.guide = guideModel;
        this.content = content;
    }

    @Override
    public void perform() {
        content.hintsProperty().remove(hint);
        try {
            writeChange();
            notifyActionPerformed();
        } catch (ActionFailedException ex) {
            notifyActionFailed(ex);
        }
    }

    @Override
    public void undo() {
        content.hintsProperty().add(hint);
        try {
            writeChange();
            notifyActionUndone();
        } catch (ActionFailedException ex) {
            notifyUndoFailed(ex);
        }
    }
    
    private void writeChange() throws ActionFailedException {
        try {
            persistenceHandler.writeContentDescriptor(guide.getBean(), content.getBean());
        } catch (IOException ex) {
           LOGGER.log(Level.SEVERE, "Failed to update content descriptor.", ex);
           throw new ActionFailedException("Failed to update content descriptor.", ex);
        }
    }

    @Override
    public String getDescription() {
        return ("Hinweis löschen");
    }
   
}
