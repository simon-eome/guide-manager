package de.eome.guide.gm.controller;

import de.eome.guide.gm.model.BooleanParameter;
import de.eome.guide.gm.model.IntegerParameter;
import de.eome.guide.gm.model.Parameter;
import java.util.logging.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * Controler for items in an paramter list.
 * @author simon.schwantzer(at)im-c.de
 */
public class ParameterItem extends BorderPane {
    private static final Logger LOGGER = Logger.getLogger(ParameterItem.class.getName());
    
    private final Label nameLabel;
    private final Node inputNode;
    
    private final Parameter<?> param;
    public ParameterItem(Parameter<?> param) {
        this.param = param;
        nameLabel = new Label(param.getName());
        nameLabel.setPrefWidth(100.0);
        BorderPane.setAlignment(nameLabel, Pos.CENTER_LEFT);
        this.setLeft(nameLabel);
        
        switch (param.getType()) {
            case BOOLEAN:
                BooleanParameter booleanParam = (BooleanParameter) param; 
                CheckBox paramCheck = new CheckBox();
                if (booleanParam.getDefaultValue() != null) {
                    paramCheck.setSelected(booleanParam.getDefaultValue());
                }
                inputNode = paramCheck;
                break;
            case INT:
                IntegerParameter intParam = (IntegerParameter) param;
                String unit = intParam.getUnit();
                if (unit != null) {
                    Label unitLabel = new Label(unit);
                    BorderPane.setMargin(unitLabel, new Insets(0, 0, 0, 5));
                    BorderPane.setAlignment(unitLabel, Pos.CENTER_LEFT);
                    this.setRight(unitLabel);
                }
            default:
                Object defaultValue = param.getDefaultValue();
                TextField paramInput = new TextField();
                if (defaultValue != null) paramInput.setText(defaultValue.toString());
                inputNode = paramInput;
        }
        BorderPane.setAlignment(inputNode, Pos.TOP_LEFT);
        this.setCenter(inputNode);
    }
    
    public void setValue(String valueString) {
        switch (param.getType()) {
            case BOOLEAN:
                CheckBox paramCheck = (CheckBox) inputNode;
                paramCheck.setSelected(Boolean.valueOf(valueString));
                break;
            default:
                TextField paramInput = (TextField) inputNode;
                paramInput.setText(valueString);
        }
    }
    
    public Parameter<?> getParameter() {
        return param;
    }
    
    public String getValueAsString() {
        switch (param.getType()) {
            case BOOLEAN:
                CheckBox inputCheckBox = (CheckBox) inputNode;
                return Boolean.toString(inputCheckBox.isSelected());
            default:
                TextField inputTextField = (TextField) inputNode;
                return inputTextField.getText();
        }
    }
}
