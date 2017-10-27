package de.eome.guide.gm.actions;

import de.eome.guide.gm.ActionFailedException;
import de.eome.guide.gm.PersistenceHandler;
import de.glassroom.gpe.Guide;
import de.glassroom.gpe.Node;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class MoveNodeAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(MoveNodeAction.class.getName());
    
    private final PersistenceHandler persistenceHandler;
    private final Guide guide;
    private final Node nodeToMove;
    private final Node newPredecessor;
    private final Set<Node> previousNodes;
    
    public MoveNodeAction(Node nodeToMove, Node newPredecessor, Guide guide, PersistenceHandler persistenceHandler) {
        this.persistenceHandler = persistenceHandler;
        this.guide = guide;
        this.newPredecessor = newPredecessor;
        this.nodeToMove = nodeToMove;
        this.previousNodes = new HashSet<>(nodeToMove.getPreviousNodes());
    }

    @Override
    public void perform() {
        try {
            guide.moveNode(nodeToMove, newPredecessor);
            persistenceHandler.writeGuide(guide);
            notifyActionPerformed();
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Failed to move node: {0}", e.getMessage());
            notifyActionFailed(new ActionFailedException("Failed to move node.", e));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write guide.", e);
            notifyActionFailed(new ActionFailedException("Failed to write guide.", e));
        }
    }

    @Override
    public void undo() {
        try {
            Optional<Node> anyPredecessor = previousNodes.stream().findAny();
            if (anyPredecessor.isPresent()) {
                guide.moveNode(nodeToMove, anyPredecessor.get());
                persistenceHandler.writeGuide(guide);
            }
            notifyActionUndone();
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Failed to move node: {0}", e.getMessage());
            notifyUndoFailed(new ActionFailedException("Failed to move node.", e));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write guide.", e);
            notifyUndoFailed(new ActionFailedException("Failed to write guide.", e));
        }
    }

    @Override
    public String getDescription() {
        return "Schritt/Kapitel verschieben";
    }
    
    
}
