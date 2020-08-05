package interactivesoftwareanalysis.modules;

/**
 * A submodule that imports data into the model
 */
public interface ImportSubmodule extends Describable {

    /**
     * Import data into the model.
     * @param progress the progress object to report the import progress to
     * @return true, iff the import was successful
     */
    boolean doImport(Progress progress);
}
