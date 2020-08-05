package interactivesoftwareanalysis.modules.parameters;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A parameter that receives a string or a list of strings.
 * It can be configured to have choices, be multiline or have a default value
 */
@ToString @EqualsAndHashCode(callSuper = true) public class StringParameter extends ParameterBase {

    private StringProperty value;
    @Getter @Setter private boolean onlyChoices;
    @Getter @Setter private boolean multiline;
    @Getter @Setter private boolean list;
    @Getter @Setter private String separator = ",";
    private final Supplier<String> defaultValue;
    private final Supplier<List<String>> valueChoices;

    public StringParameter(String name, String description) {
        this(name, description, "");
    }

    public StringParameter(String name, String description, String defaultValue) {
        this(name, description, () -> defaultValue, null);
    }

    public StringParameter(String name, String description, Supplier<String> defaultValue, Supplier<List<String>> valueChoices) {
        this(name, description, defaultValue, valueChoices, false, false);
    }

    public StringParameter(String name, String description, Supplier<String> defaultValue, Supplier<List<String>> valueChoices, boolean onlyChoices, boolean multiline) {
        super(name, description);
        this.value = new SimpleStringProperty();
        this.defaultValue = defaultValue;
        this.valueChoices = valueChoices;
        this.onlyChoices = onlyChoices;
        this.multiline = multiline;
    }

    public String getValue() {
        return value.get();
    }

    public String[] getValues(){
        return getValue().split(getSeparator());
    }

    public String getDefaultValue(){
        return defaultValue.get();
    }

    public List<String> getValueChoices(){
        return valueChoices != null ? valueChoices.get() : new ArrayList<>();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    @Override public void visit(ParameterVisitor parameterVisitor) {
        if (valueChoices == null) {
            parameterVisitor.visitStringParameter(this);
        }else{
            if (onlyChoices) {
                parameterVisitor.visitStringParameterWithOnlyChoices(this);
            }else{
                parameterVisitor.visitStringParameterWithChoices(this);
            }
        }

    }
}
