package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.modules.parameters.FileParameter;
import interactivesoftwareanalysis.modules.parameters.Parameter;
import interactivesoftwareanalysis.modules.parameters.ParameterVisitor;
import interactivesoftwareanalysis.modules.parameters.StringParameter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.controlsfx.control.PrefixSelectionComboBox;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds a dialog by visiting parameters. The user can enter values for those parameters.
 * @see ParameterVisitor
 * @see interactivesoftwareanalysis.modules.actions.ModuleAction
 * @see interactivesoftwareanalysis.modules.ExportSubmodule
 */
class BuildUIParameterVisitor implements ParameterVisitor {

    /** The preferred width of controls */
    private static final int PREF_WIDTH = 200;

    /** The number of rows to use in a multiline text field */
    private static final int MULTI_LINE_ROWS = 4;

    /** Spacing between elements */
    private static final int SPACING = 5;

    private Dialog<ButtonType> dialog = new Dialog<>();
    private VBox content = new VBox(SPACING);
    private List<Node> inputs = new ArrayList<>();

    public BuildUIParameterVisitor(String title, String description, Window owner) {

        // create a basic dialog to put parameter control in
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getStylesheets().add("styles/style.css");
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(description);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }

    @Override public void visitStringParameter(StringParameter stringParameter) {
        TextInputControl field;
        if (stringParameter.isMultiline()){
            TextArea textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.setPrefRowCount(MULTI_LINE_ROWS);
            field = textArea;
        }else{
            field = new TextField();
        }
        prepareTextInputControl(stringParameter, field);
        buildRow(stringParameter, field);
    }

    @Override public void visitStringParameterWithChoices(StringParameter stringParameter) {
        TextField field = new TextField();
        if (stringParameter.isList()) {
            TextFields.bindAutoCompletion(field, param -> {
                String[] inputs = param.getUserText().split(stringParameter.getSeparator());
                String last = inputs[inputs.length - 1].trim();
                String first = Arrays.stream(inputs)
                        .map(String::trim)
                        .limit(inputs.length - 1)
                        .collect(Collectors.joining(stringParameter.getSeparator()));
                return stringParameter.getValueChoices().stream()
                        .filter(s -> s.contains(last))
                        .map(s -> first.isEmpty() ? s : String.join(stringParameter.getSeparator(), first, s)).collect(Collectors.toList());
            });
        } else {
            TextFields.bindAutoCompletion(field, stringParameter.getValueChoices());
        }
        prepareTextInputControl(stringParameter, field);
        buildRow(stringParameter, field);
    }

    @Override public void visitStringParameterWithOnlyChoices(StringParameter stringParameter) {
        PrefixSelectionComboBox<String> field = new PrefixSelectionComboBox<>();
        field.setPromptText(stringParameter.getName());
        field.setValue(stringParameter.getDefaultValue());
        field.setItems(FXCollections.observableList(stringParameter.getValueChoices()));
        field.setTooltip(new Tooltip(stringParameter.getDescription()));
        field.setPrefWidth(PREF_WIDTH);
        stringParameter.valueProperty().bind(field.valueProperty());
        buildRow(stringParameter, field);
    }

    @Override public void visitFileParameter(FileParameter fileParameter){
        HBox hBox = new HBox(SPACING);
        hBox.setPrefWidth(PREF_WIDTH);
        Button fileSelectButton = new Button("...");
        TextField fileField = new TextField();
        fileField.setMaxWidth(Double.MAX_VALUE);
        fileField.setDisable(true);
        HBox.setHgrow(fileField, Priority.ALWAYS);
        hBox.getChildren().add(fileField);
        hBox.getChildren().add(fileSelectButton);
        fileSelectButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Datei auswählen");
            fileChooser.getExtensionFilters().addAll(fileParameter.getFileExtensions().stream()
                    .map(s -> new FileChooser.ExtensionFilter(s, s))
                    .collect(Collectors.toList()));
            File file;
            if (fileParameter.isSave()) {
                file = fileChooser.showSaveDialog(null);
            }else{
                file = fileChooser.showOpenDialog(null);
            }
            if (file != null) {
                fileField.setText(file.getName());
                fileParameter.setFile(file);
            }
        });
        buildRow(fileParameter, hBox);
    }

    /**
     * Show the parameter dialog.
     * If there are no parameters, the dialog is not shown and the method returns,
     * as if the user had accepted the dialog.
     * @return false iff the dialog was cancelled by the user.
     */
    public boolean show() {
        if (inputs.size() > 0){
            // show the dialog and set the focus to the first element, so
            // so the user can start to type right away
            Platform.runLater(inputs.get(0)::requestFocus);
            return ButtonType.OK.equals(dialog.showAndWait().get());
        }else{
            return true;
        }
    }

    /**
     * Set some values that all TextInputControls should have in common
     * @param stringParameter the string aprameter the control is generated for
     * @param field the field to prepare
     */
    private void prepareTextInputControl(StringParameter stringParameter, TextInputControl field) {
        field.setPromptText(stringParameter.getName());
        field.setText(stringParameter.getDefaultValue());
        field.setTooltip(new Tooltip(stringParameter.getDescription()));
        field.setPrefHeight(Region.USE_COMPUTED_SIZE);
        field.setMaxHeight(Region.USE_COMPUTED_SIZE);
        field.setPrefWidth(PREF_WIDTH);
        stringParameter.valueProperty().bind(field.textProperty());
    }

    /**
     * Build a row in the dialog with a label and the specified node for the specified parameter.
     * The parameter is not bound to the node here.
     * @param parameter the parameter that a row should be added for
     * @param field the node to add to the row
     */
    private void buildRow(Parameter parameter, Node field) {
        HBox box = new HBox(SPACING);
        box.setAlignment(Pos.TOP_LEFT);
        Label label = new Label(parameter.getName());
        label.setLabelFor(field);
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        box.getChildren().add(label);
        box.getChildren().add(field);
        content.getChildren().add(box);
        inputs.add(field);
    }

}
