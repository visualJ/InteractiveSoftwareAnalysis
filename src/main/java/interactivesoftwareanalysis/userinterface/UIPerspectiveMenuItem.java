package interactivesoftwareanalysis.userinterface;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.NonNull;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * A menu item that represents a ui perspective.
 * It displays the perspective name and the number of views in the perspective.
 */
public class UIPerspectiveMenuItem extends CustomMenuItem {

    private final HBox hBox;
    private final Label nameLabel;
    private final Label viewCountLabel;
    private final Button closeButton;

    public UIPerspectiveMenuItem(UIPerspective uiPerspective, @NonNull Consumer<UIPerspective> onClose){
        hBox = new HBox();
        nameLabel = new Label();
        viewCountLabel = new Label();
        closeButton = new Button("\uF00D");

        closeButton.getStyleClass().addAll(Arrays.asList("close-button", "glyph"));
        closeButton.setOnAction(event -> {
            event.consume();
            onClose.accept(uiPerspective);
        });

        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(Arrays.asList(viewCountLabel, nameLabel, closeButton));

        viewCountLabel.setPadding(Insets.EMPTY);
        viewCountLabel.getStyleClass().add("number-label");

        setContent(hBox);

        nameProperty().bind(Bindings.format("%s", uiPerspective.nameProperty()));
        viewCountProperty().bind(Bindings.format("%s", uiPerspective.uiViewsProperty().sizeProperty()));
    }

    /**
     * Retrieve the property of the displayed name
     * @return the name property
     */
    public StringProperty nameProperty(){
        return nameLabel.textProperty();
    }

    /**
     * Retrieve the property of the displayed view count
     * @return the view conut property
     */
    public StringProperty viewCountProperty(){
        return viewCountLabel.textProperty();
    }


}
