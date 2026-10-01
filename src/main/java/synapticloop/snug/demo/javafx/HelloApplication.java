package synapticloop.snug.demo.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import synapticloop.snug.demo.javafx.controller.HelloController;
import synapticloop.snug.demo.javafx.model.SnugModel;
import synapticloop.snug.demo.javafx.viewmodel.HelloViewModel;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX Application entry point. Wires the MVVM stack:
 * {@link SnugModel} → {@link HelloViewModel} → {@link HelloController} (View).
 */
public class HelloApplication extends Application {

	private static final String IMAGE_SNUG_LOGO = "/assets/images/snug-logo.png";
	private static final String FXML_HELLO_VIEW = "hello-view.fxml";
	private static final String TITLE_WINDOW = "Snug";

	@Override
	public void start(Stage stage) throws IOException {
		SnugModel model = new SnugModel();
		HelloViewModel viewModel = new HelloViewModel(model);

		FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(FXML_HELLO_VIEW));
		// Inject the already-wired ViewModel into the controller via a
		// factory. FXMLLoader calls this with the class declared in
		// fx:controller; the factory ignores that and returns our instance.
		fxmlLoader.setControllerFactory(cls -> new HelloController(viewModel));

		Scene scene = new Scene(fxmlLoader.load(), 300, 400);

		// Window icon — the snug logo (loaded from the classpath root).
		stage.getIcons().add(new Image(
				Objects.requireNonNull(
						HelloApplication.class.getResourceAsStream(IMAGE_SNUG_LOGO))));

		stage.setTitle(TITLE_WINDOW);
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
	}
}
