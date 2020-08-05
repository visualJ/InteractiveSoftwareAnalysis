package interactivesoftwareanalysis;

import com.gluonhq.ignite.DIContext;
import com.gluonhq.ignite.guice.GuiceContext;
import interactivesoftwareanalysis.dependencyinjection.GuiceModule;
import interactivesoftwareanalysis.userinterface.UIManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Collections;

/**
 * The main class that starts the application, initializes a DI context and injects
 * the applications main components.
 */
public class Main extends Application {

    @Override public void start(Stage primaryStage) throws Exception {

        // Initialize the DI context, so dependency injection can be used
        GuiceModule module = new GuiceModule();
        DIContext context = new GuiceContext(this, () -> Collections.singletonList(module));
        module.addDIContext(context);
        context.init();

        // inject the singleton ui manager and initialize it, so it can create the ui
        // the ui manager has dependencies that need to be injected
        UIManager uiManager = context.getInstance(UIManager.class);
        uiManager.initialize(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
