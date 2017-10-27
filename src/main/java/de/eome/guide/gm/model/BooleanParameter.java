package de.eome.guide.gm.model;

import static de.eome.guide.gm.model.Parameter.parseNameXML;
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 *
 * @author simon.schwantzer(at)im-c.de
 */
public class BooleanParameter extends Parameter<Boolean> {

    public static class Builder {
        private final String id;
        private final Map<String, String> localizedNames;
        private Boolean defaultValue;
        
        public Builder(String id) {
            this.id = id;
            this.localizedNames = new HashMap<>();
        }
        
        public Builder setDefaultValue(Boolean value) {
            this.defaultValue = value;
            return this;
        }
        
        public BooleanParameter build() {
            return new BooleanParameter(id, localizedNames, defaultValue);
        }
    }
    public BooleanParameter(String id, Map<String, String> localizedNames, Boolean defaultValue) {
        super(Parameter.Type.BOOLEAN, id, localizedNames, defaultValue);
    }
    
    @Override
    public boolean validateValueString(String valueString) {
        if (isRequired() && (valueString == null || valueString.isEmpty())) {
            return false;
        }
        
        return "true".equalsIgnoreCase(valueString) || "false".equalsIgnoreCase(valueString);
    }
    
    @Override
    public Element toXML(Namespace ns) {
        Element element = super.toXML(ns);
        element.setAttribute("type", "boolean");
        return element;
    }
    
    public static BooleanParameter parseXML(Element paramElement) {
        String id = paramElement.getAttributeValue("id");
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Missing attribute [id] in param element.");
        }
        
        Map<String, String> localizedNames = parseNameXML(paramElement);
        boolean isRequired = Boolean.valueOf(paramElement.getAttributeValue("required"));
        
        String defaultValueString = paramElement.getAttributeValue("default");
        Boolean defaultValue = defaultValueString != null ? Boolean.valueOf(defaultValueString) : null;
        BooleanParameter param = new BooleanParameter(id, localizedNames, defaultValue);
        param.setRequired(isRequired);
        return param;
    }
    
}
