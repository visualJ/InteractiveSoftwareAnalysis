package interactivesoftwareanalysis.userinterface;

import interactivesoftwareanalysis.model.Tag;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.controlsfx.control.PopOver;

import java.util.List;

/**
 * A UI element that displays tags.
 *
 * Tags are displayed as colored labels next to each other.
 * If there are more than 5 tags, only the count is displayed and all tags are accessible via a popup.
 */
public class TagBox extends HBox {

    private StringToColorConverter converter;

    private class TagLabel extends Label{
        public TagLabel(Tag tag) {
            super(tag.getName());
            if (!tag.getDetail().isEmpty()) {
                setTooltip(new Tooltip(tag.getDetail()));
            }
            setPadding(new Insets(0, 5, 0, 5));
            setTextFill(Color.WHITE);
            setFont(Font.font(10));
            setBackground(new Background(new BackgroundFill(converter.getColor(tag.getName()), new CornerRadii(20), Insets.EMPTY)));
        }
    }

    public TagBox() {
        super(5);
        converter = new HashStringToColorConverter();
    }

    /**
     * Sets the tags to display in this tag box
     * @param tags a list of tags to display
     */
    public void setTags(List<Tag> tags){
        getChildren().clear();
        if (tags != null) {
            if (tags.size() <= 5) {
                tags.forEach(tag -> {
                    Label label = new TagLabel(tag);
                    getChildren().add(label);
                });
            } else {
                Label label = new Label(tags.size() + " Tags");
                label.setPadding(new Insets(0, 5, 0, 5));
                label.setTextFill(Color.WHITE);
                label.setFont(Font.font(10));
                label.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, new CornerRadii(20), Insets.EMPTY)));
                getChildren().add(label);
                FlowPane flowPane = new FlowPane();
                flowPane.setPadding(new Insets(5));
                flowPane.setHgap(5);
                flowPane.setVgap(5);
                flowPane.setPrefWrapLength(600);
                tags.forEach(tag -> {
                    Label tagLabel = new TagLabel(tag);
                    flowPane.getChildren().add(tagLabel);
                });
                PopOver popOver = new PopOver(flowPane);
                popOver.setDetachable(false);
                label.setOnMouseClicked(event -> popOver.show(this));
            }
        }
    }
}
