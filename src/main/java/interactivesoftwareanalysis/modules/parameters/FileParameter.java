package interactivesoftwareanalysis.modules.parameters;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A parameter that receives a file for writing or reading.
 */
public class FileParameter extends ParameterBase {

    private ObjectProperty<File> file;
    @Getter @Setter private boolean save;
    @Getter @Setter private List<String> fileExtensions;

    public FileParameter(String name, String description) {
        super(name, description);
        this.file = new SimpleObjectProperty<>();
        this.fileExtensions = new ArrayList<>();
    }

    @Override
    public void visit(ParameterVisitor parameterVisitor) {
        parameterVisitor.visitFileParameter(this);
    }

    public File getFile() {
        return file.get();
    }

    public void setFile(File file) {
        this.file.set(file);
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }
}
