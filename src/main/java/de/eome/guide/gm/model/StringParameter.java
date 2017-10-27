package de.eome.guide.gm.model;

import java.util.HashMap;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class StringParameter extends Parameter<String> {

    public static class Builder {
        private final String id;
        private final Map<String, String> localizedNames;
        private String defaultValue;
        
        public Builder(String id) {
            this.id = id;
            this.localizedNames = new HashMap<>();
        }
        
        public Builder setDefaultValue(String value) {
            this.defaultValue = value;
            return this;
        }
        
        public StringParameter build() {
            return new StringParameter(id, localizedNames, defaultValue);
        }
    }

    public StringParameter(String id, Map<String, String> localizedNames, String defaultValue) {
        super(Parameter.Type.STRING, id, localizedNames, defaultValue);
    }
    
    @Override
    public boolean validateValueString(String valueString) {
        if (isRequired() && (valueString == null || valueString.isEmpty())) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public Element toXML(Namespace ns) {
        Element element = super.toXML(ns);
        element.setAttribute("type", "string");
        return element;
    }
    
    public static StringParameter parseXML(Element paramElement) throws IllegalArgumentException {
        String id = paramElement.getAttributeValue("id");
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Missing attribute [id] in param element.");
        }
        
        Map<String, String> localizedNames = parseNameXML(paramElement);
        
        boolean isRequired = Boolean.valueOf(paramElement.getAttributeValue("required"));
        
        String defaultValue = paramElement.getAttributeValue("default");
        StringParameter param = new StringParameter(id, localizedNames, defaultValue);
        param.setRequired(isRequired);
        return param;
    }
}
