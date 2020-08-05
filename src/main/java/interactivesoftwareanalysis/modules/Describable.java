package interactivesoftwareanalysis.modules;

/**
 * Created by benedikt.ringlein on 06.09.2016.
 */
public interface Describable {
    /**
     * A name that can be displayed in the ui to identify this module.
     * This should be a short an descriptive name that shows this modules domain.
     * @return the modules name
     */
    String getName();

    /**
     * A short description of what the nodule does, what its propose is and how it should be used.
     * @return the module description
     */
    String getDescription();
}
