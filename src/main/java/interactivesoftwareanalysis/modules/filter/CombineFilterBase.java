package interactivesoftwareanalysis.modules.filter;

import lombok.Getter;

import java.util.List;

/**
 * A base class for filters that combine the results of other filters
 */
public abstract class CombineFilterBase implements Filter {

    @Getter protected final List<Filter> filters;

    public CombineFilterBase(List<Filter> filters){
        this.filters = filters;
    }

}
