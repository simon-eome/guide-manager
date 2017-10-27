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
public class IntegerParameter extends Parameter<Integer> {
    
    private Integer minValue;
    private Integer maxValue;
    private String unit;
    
    public static class Builder {
        private final String id;
        private Integer defaultValue;
        private Integer minValue;
        private Integer maxValue;
        private String unit;
        private final Map<String, String> localizedNames;
        
        public Builder(String id) {
            this.id = id;
            this.localizedNames = new HashMap<>();
        }
        
        public Builder setDefaultValue(int defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        public Builder setMinValue(int minValue) {
            this.minValue = minValue;
            return this;
        }
        
        public Builder setMaxValue(int maxValue) {
            this.maxValue = maxValue;
            return this;
        }
        
        public Builder addName(String languageId, String name) {
            localizedNames.put(languageId, name);
            return this;
        }
        
        public Builder setUnit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public IntegerParameter build() throws IllegalArgumentException {
            IntegerParameter param = new IntegerParameter(id, localizedNames, defaultValue);
            param.setMin(minValue);
            param.setMax(maxValue);
            param.setUnit(unit);
            return param;
        }
    }
    
    public IntegerParameter(String id, Map<String, String> localizedNames, int defaultValue) {
        super(Parameter.Type.INT, id, localizedNames, defaultValue);
    }
    
    public IntegerParameter(String id, Map<String, String> localizedNames) {
        super(Parameter.Type.INT, id, localizedNames);
    }
    
    private void setMin(Integer value) {
        minValue = value;
    }
    
    public Integer getMin() {
        return minValue;
    }
    
    private void setMax(Integer value) {
        maxValue = value;
    }
    
    public Integer getMax() {
        return maxValue;
    }
    
    private void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getUnit() {
        return unit;
    }
    
    @Override
    public boolean validateValueString(String valueString) {
        if (isRequired() && (valueString == null || valueString.isEmpty())) {
            return false;
        }
        Integer value;
        try {
            value = Integer.valueOf(valueString);
        } catch (NumberFormatException e) {
            return false;
        }
        if (minValue != null && value < minValue) return false;
        if (maxValue != null && value > maxValue) return false;
        return true;
    }
    
    @Override
    public Element toXML(Namespace ns) {
        Element element = super.toXML(ns);
        element.setAttribute("type", "int");
        if (minValue != null) element.setAttribute("min", String.valueOf(minValue));
        if (maxValue != null) element.setAttribute("max", String.valueOf(maxValue));
        if (unit != null) element.setAttribute("unit", unit);
        return element;
    }
    
    public static IntegerParameter parseXML(Element paramElement) {
        String id = paramElement.getAttributeValue("id");
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Missing attribute [id] in param element.");
        }
        
        Map<String, String> localizedNames = parseNameXML(paramElement);
        
        boolean isRequired = Boolean.valueOf(paramElement.getAttributeValue("required"));
        
        String defaultValueString = paramElement.getAttributeValue("default");
        Integer defaultValue = defaultValueString != null ? Integer.valueOf(defaultValueString) : null;
        
        String minString = paramElement.getAttributeValue("min");
        Integer minValue = minString != null ? Integer.valueOf(minString) : null;
        String maxString = paramElement.getAttributeValue("max");
        Integer maxValue = maxString != null ? Integer.valueOf(maxString) : null;
        String unit = paramElement.getAttributeValue("unit");
        
        IntegerParameter param = defaultValue != null ?
                new IntegerParameter(id, localizedNames, defaultValue) :
                new IntegerParameter(id, localizedNames);
        param.setMin(minValue);
        param.setMax(maxValue);
        param.setRequired(isRequired);
        param.setUnit(unit);
        
        return param;
    }
}
