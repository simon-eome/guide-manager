/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.eome.guide.gm;

/**
 * Exception thrown if a validation fails.
 * @author simon.schwantzer(at)im-c.de
 */
public class ValidationFailedException extends RuntimeException {
    private final String display;
    
    public ValidationFailedException(String display) {
        super();
        this.display = display;
    }
    
    public String getDisplay() {
        return display;
    }
}
