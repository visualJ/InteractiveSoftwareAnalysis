package interactivesoftwareanalysis.userinterface;

import javafx.scene.paint.Color;

/**
 * A converter for converting a string to a color.
 */
@FunctionalInterface public interface StringToColorConverter {

    /**
     * Get the color this converter associates with the string.
     * The implementation can decide, how this is done. There are no guarantees that or how
     * the string is used for the conversion or that the same string yields the same
     * color each time.
     * @param string the input to get the color for
     * @return a color
     */
    Color getColor(String string);
}
