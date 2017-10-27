package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.Node;
import de.glassroom.gpe.annotations.ContentAnnotation;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class DeleteNodeAction extends BaseAction {
    private final static Logger LOGGER = Logger.getLogger(DeleteNodeAction.class.getName());
    
    private final Node node;
    private final Guide guide;    
    private final PersistenceHandler persistenceHandler;
    private Set<Node> previousNodes, nextNodes;
    private final Map<String, File> backupPackages;
    
    public DeleteNodeAction(Node nodeToDelete, Guide guide, PersistenceHandler persistenceHandler) {
        this.guide = guide;
        this.persistenceHandler = persistenceHandler;
        this.node = nodeToDelete;
        this.backupPackages = new HashMap<>();
    }

    @Override
    public void perform() {
        previousNodes = new HashSet<>(node.getPreviousNodes());
        nextNodes = new HashSet<>(node.getNextNodes());
        
        try {
            // Remove step from guide.
            guide.removeNode(node);
            
            // Delete related content packages.
            ContentAnnotation content = node.getContent();
            if (content != null) {
                for (String contentId : content.getContentPackages().values()) {
                    File backup = persistenceHandler.backupContentPackage(guide.getId(), contentId);
                    backupPackages.put(contentId, backup);
                    persistenceHandler.deleteContentPackage(guide.getId(), contentId);
                }
            }
            
            // Persist changes.
            persistenceHandler.writeGuide(guide);
            notifyActionPerformed();
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Failed to remove node: {0}", e.getMessage());
            notifyActionFailed(new ActionFailedException("Failed to remove node.", e));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write guide.", e);
            notifyActionFailed(new ActionFailedException("Failed to write guide.", e));
        }
    }

    @Override
    public void undo() {
        try {
            // Add step to guide.
            Optional<Node> anyPredecessor = previousNodes.stream().findAny();
            if (anyPredecessor.isPresent()) {
                guide.addNode(node, anyPredecessor.get());
            } else {
                guide.addNode(node);
            }
            previousNodes.forEach(predecessor -> node.addPrevious(predecessor));
            nextNodes.forEach(successor -> node.addNext(successor));
            
            // Restore related content packages.
            backupPackages.forEach((contentId, backupDir) -> persistenceHandler.restoreContentPackage(guide.getId(), contentId, backupDir));
            
            // Persist changes.
            persistenceHandler.writeGuide(guide);
            notifyActionUndone();
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Failed to restore node: {0}", e.getMessage());
            notifyUndoFailed(new ActionFailedException("Failed to restore node.", e));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write guide.", e);
            notifyUndoFailed(new ActionFailedException("Failed to write guide.", e));
        }
    }

    @Override
    public String getDescription() {
        return "Schritt/Kapitel löschen";
    }
    
}
