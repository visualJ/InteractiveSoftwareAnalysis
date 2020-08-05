package interactivesoftwareanalysis.userinterface;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import lombok.Getter;

/**
 * A horizontal drag gesture recognizer
 */
class DragHorizontalGesture {

    @Getter private final Node node;
    @Getter private final Runnable onDragLeft;
    @Getter private final Runnable onDragRight;

    private double oldX;

    public DragHorizontalGesture(Node node, Runnable onDragLeft, Runnable onDragRight) {
        this.node = node;
        this.onDragLeft = onDragLeft;
        this.onDragRight = onDragRight;
        node.setOnMousePressed(this::onMousePressed);
        node.setOnMouseReleased(this::onMouseReleased);
    }

    /**
     * Executed, when the mouse is pressed
     * @param event the mouse evetn
     */
    private void onMousePressed(MouseEvent event){
        oldX = event.getX();
    }

    /**
     * executed, when the mouse is released
     * @param event the mouse event
     */
    private void onMouseReleased(MouseEvent event){
        double newX = event.getX();
        if (newX > oldX){
            onDragRight.run();
        }else if(newX < oldX){
            onDragLeft.run();
        }
    }
}
