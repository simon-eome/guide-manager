package de.eome.guide.gm;

/**
 * An exception thrown when the change could not be performed.
 * @author simon.schwantzer(at)im-c.de
 */
public class ActionFailedException extends Exception {

    public ActionFailedException(String message) {
        super(message);
    }

    public ActionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
