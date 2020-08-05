package interactivesoftwareanalysis.userinterface;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import lombok.Getter;

/**
 * A vertical drag gesture recognizer
 */
class DragVerticalGesture {

    @Getter private final Node node;
    @Getter private final Runnable onDragUp;
    @Getter private final Runnable onDragDown;

    private double oldY;

    public DragVerticalGesture(Node node, Runnable onDragUp, Runnable onDragDown) {
        this.node = node;
        this.onDragUp = onDragUp;
        this.onDragDown = onDragDown;
        node.setOnMousePressed(this::onMousePressed);
        node.setOnMouseReleased(this::onMouseReleased);
    }

    /**
     * Executed, when the mouse is pressed
     * @param event the mouse evetn
     */
    private void onMousePressed(MouseEvent event){
        oldY = event.getY();
    }

    /**
     * executed, when the mouse is released
     * @param event the mouse event
     */
    private void onMouseReleased(MouseEvent event){
        double newY = event.getY();
        if (newY > oldY){
            onDragDown.run();
        }else if(newY < oldY){
            onDragUp.run();
        }
    }
}
