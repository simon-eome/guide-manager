package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.eome.guide.gm.model.ContentModel;
import de.eome.guide.gm.model.GuideModel;
import de.eome.guide.gm.model.WarningModel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class AddWarningAction extends BaseAction {
    private final static Logger LOGGER = Logger.getLogger(AddWarningAction.class.getName());
    
    private final WarningModel warning;
    private final PersistenceHandler persistenceHandler;
    private final GuideModel guide;
    private final ContentModel content;
    
    public AddWarningAction(WarningModel warning, ContentModel content, GuideModel guideModel, PersistenceHandler persistenceHandler) {
        this.warning = warning;
        this.persistenceHandler = persistenceHandler;
        this.guide = guideModel;
        this.content = content;
    }

    @Override
    public void perform() {
        content.warningsProperty().add(warning);
        try {
            writeChange();
            notifyActionPerformed();
        } catch (ActionFailedException ex) {
            notifyActionFailed(ex);
        }
    }

    @Override
    public void undo() {
        content.warningsProperty().remove(warning);
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
        return ("Warnung hinzufügen");
    }
   
}
