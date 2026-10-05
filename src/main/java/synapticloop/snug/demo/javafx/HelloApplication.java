package synapticloop.snug.demo.javafx;

import javafx.application.Application;
import javafx.application.Platform;
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

	/**
	 * Stage size.
	 *
	 * <p>The width is driven by whichever is wider: the bottom icon row (three
	 * 96px tiles at 112px each — artwork + padding + border — 16px apart,
	 * plus 20px insets, comes to 408px) or the content column's 256px logo
	 * plus its own insets (296px). The row wins, so the logo is centred in a
	 * window sized for the tiles. The height covers the menu bar, that
	 * content column, and the row including its heading and 12px captions. It
	 * is deliberately generous: the stage is not resizable, so a window a
	 * few pixels short clips the tiles rather than scrolling.</p>
	 */
	private static final double SCENE_WIDTH = 420;
	private static final double SCENE_HEIGHT = 650;

	/** Exit status used when the user closes the window. */
	private static final int EXIT_CODE_OK = 0;

	@Override
	public void start(Stage stage) throws IOException {
		SnugModel model = new SnugModel();
		HelloViewModel viewModel = new HelloViewModel(model);

		FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(FXML_HELLO_VIEW));
		// Inject the already-wired ViewModel into the controller via a
		// factory. FXMLLoader calls this with the class declared in
		// fx:controller; the factory ignores that and returns our instance.
		//
		// The stage and quit action are handed over the same way: the
		// controller needs the window to parent the About dialog to, and it
		// must not own the exit sequence, so the menu's Quit item and the
		// window's close button both land on shutdown() below.
		fxmlLoader.setControllerFactory(cls -> new HelloController(viewModel, stage, HelloApplication::shutdown));

		Scene scene = new Scene(fxmlLoader.load(), SCENE_WIDTH, SCENE_HEIGHT);

		// Window icon — the snug logo (loaded from the classpath root).
		stage.getIcons().add(new Image(
				Objects.requireNonNull(
						HelloApplication.class.getResourceAsStream(IMAGE_SNUG_LOGO))));

		stage.setTitle(TITLE_WINDOW);
		stage.setResizable(false);
		stage.setScene(scene);

		// Closing the window must end the process, not just the UI. JavaFX's
		// implicit exit (on by default) would stop the FX runtime when the last
		// window goes away, but the JVM would then linger for as long as any
		// non-daemon thread survives — an invisible, unkillable-looking process.
		// Handle the close ourselves so the exit is explicit and unconditional.
		stage.setOnCloseRequest(event -> shutdown());

		stage.show();
	}

	/**
	 * Tear the application down. Called both by the window's close request and
	 * by the menu bar's Quit item, so there is one exit path for the whole app.
	 *
	 * <p>{@link Platform#exit()} asks the JavaFX runtime to stop its own threads
	 * and invokes {@link #stop()}; it does not, on its own, promise the process
	 * ends. {@link System#exit(int)} is the hard guarantee, so a stray non-daemon
	 * thread can never leave a headless JVM running after the window is gone.</p>
	 *
	 * <p>Implicit exit is deliberately left enabled: it still covers windows
	 * closed by other means (e.g. a programmatic {@code stage.hide()}), which do
	 * not fire this handler.</p>
	 */
	private static void shutdown() {
		Platform.exit();
		System.exit(EXIT_CODE_OK);
	}

	/**
	 * Invoked by the JavaFX runtime once the toolkit is shutting down. Calls
	 * {@link Platform#exit()} idempotently so that a teardown started by any
	 * other route also ends up stopping the FX runtime.
	 */
	@Override
	public void stop() {
		Platform.exit();
	}
}
