package interactivesoftwareanalysis.modules.filter;

import lombok.*;

/**
 * A base class for decide filters
 */
public abstract class DecideFilterBase implements Filter {

    @Getter protected final String pattern;

    public DecideFilterBase(String pattern){
        this.pattern = pattern;
    }

}
