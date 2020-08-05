package interactivesoftwareanalysis.userinterface;

import com.google.common.hash.Hashing;
import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.Charset;

/**
 * Converts a String to a color by hashing the string.
 *
 * The saturation will be between {@value MIN_SATURATION} and {@value MAX_SATURATION}.
 * The brightness will be between {@value MIN_BRIGHTNESS} and {@value MAX_BRIGHTNESS}.
 */
@NoArgsConstructor @AllArgsConstructor class HashStringToColorConverter implements StringToColorConverter {

    private static final double MAX_SATURATION = 1;
    private static final double MIN_SATURATION = 0.6;
    private static final double MAX_BRIGHTNESS = 0.85;
    private static final double MIN_BRIGHTNESS = 0.6;

    @Getter @Setter private Double fixedHue;

    @Override public Color getColor(String string) {
        int hash = (Hashing.md5().newHasher().putString(string, Charset.defaultCharset()).hash().asInt() / 2) + (Integer.MAX_VALUE / 2);
        double hue = fixedHue != null ? fixedHue : (hash %  10000.0) / 10000.0 * 360.0;
        double saturation = (hash % 1000000.0) / 1000000.0  * (MAX_SATURATION - MIN_SATURATION) + MIN_SATURATION;
        double brightness = (hash % 100000.0) / 100000.0  * (MAX_BRIGHTNESS - MIN_BRIGHTNESS) + MIN_BRIGHTNESS;
        return Color.hsb(hue, saturation, brightness);
    }

}
