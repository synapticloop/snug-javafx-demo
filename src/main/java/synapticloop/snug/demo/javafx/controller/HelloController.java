package synapticloop.snug.demo.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import synapticloop.snug.demo.javafx.view.InfoModal;
import synapticloop.snug.demo.javafx.viewmodel.HelloViewModel;

import java.util.Locale;

/**
 * View-layer glue for the hello window.
 *
 * <p>Stays intentionally thin: holds the {@code @FXML}-injected scene-graph
 * nodes, binds them to {@link HelloViewModel} properties, and translates user
 * input into ViewModel commands. Pixel-coord hit-testing for the coffee-cup
 * rectangle is a presentational geometry concern and lives here, not in the
 * Model.</p>
 *
 * <p>Also owns the window's modal dialogs: the menu bar's About item and the
 * three icon tiles along the bottom (Snug Build, Build with Snug, Snug
 * Preview). All are the same read-only information modal, so the handlers
 * only supply wording and artwork and hand off to {@link InfoModal}, which
 * owns building and theming them. Quit likewise hands off to the
 * {@code quitAction} supplied by the Application, which stays the single
 * owner of how the process exits. On macOS the bar itself is handed to the
 * system menu bar, so it renders in the Apple menu bar rather than in the
 * window.</p>
 */
public class HelloController {
	// Hot-spot rectangle covering the coffee cup in snug-logo.png (native pixel
	// coordinates of the 256x256 source image). Edit these four values to move
	// or resize the clickable area without touching the hit-test logic below.
	private static final double COFFEE_CUP_X = 95.0;
	private static final double COFFEE_CUP_Y = 155.0;
	private static final double COFFEE_CUP_W = 70.0;
	private static final double COFFEE_CUP_H = 45.0;

	/** App name shown in the About dialog. Keep in sync with the menu title in hello-view.fxml. */
	private static final String APP_NAME = "Snug JavaFX Demo";

	/** Keep in sync with the `version` property in build.gradle.kts. */
	private static final String APP_VERSION = "1.0.0";

	private static final String ABOUT_BLURB =
			"A tiny JavaFX app used as a packaging demo target for snug, "
					+ "the snug application packager for Windows and macOS.";

	/**
	 * The static logo, loaded directly rather than taken from the on-screen
	 * ImageView: that one may be showing the wave or ooh frame, which would
	 * make the About box's artwork depend on whatever was last clicked.
	 */
	private static final String LOGO_URL = "/assets/images/snug-logo.png";

	/** Rendered width of the About dialog's logo graphic. */
	private static final double ABOUT_LOGO_WIDTH = 64.0;

	/**
	 * Rendered width of the icon tiles' artwork, and of the graphic in the
	 * modal each tile opens. Kept equal to the {@code fitWidth} of the three
	 * ImageViews in hello-view.fxml so the dialog shows the same icon the
	 * user just clicked, at the size they clicked it.
	 */
	private static final double ICON_WIDTH = 96.0;

	// --- The three bottom-row icon tiles. Each opens a modal explaining what
	// the snug tool does, so the text lives beside the icon it describes. ---

	/** Artwork shown on the Build tile and in the modal it opens. */
	private static final String ICON_BUILD_URL = "/assets/images/snug-runner.png";

	private static final String BUILD_TITLE = "Snug Build";
	private static final String BUILD_BLURB =
			"Snug Builder builds your application for your operating System (available for MacOS and Windows).";

	/** Artwork shown on the dropper tile and in the modal it opens. */
	private static final String ICON_DROPPER_URL = "/assets/images/snug-dropper.png";

	private static final String DROPPER_TITLE = "Build with Snug";
	private static final String DROPPER_BLURB =
			"A quick drag-and-drop utility that builds an example application "
					+ "to test out your JAR file(s)";

	/** Artwork shown on the Preview tile and in the modal it opens. */
	private static final String ICON_PREVIEW_URL = "/assets/images/snug-preview.png";

	private static final String PREVIEW_TITLE = "Snug Preview";
	private static final String PREVIEW_BLURB =
			"Snug Preview allows you to preview the dialog windows that may be "
					+ "displayed when launching the Snug packaged Application. This makes it "
					+ "straight-forward to customise your application the way you want.";

	private final HelloViewModel viewModel;

	/**
	 * The window this controller draws into. Needed only to parent the modal
	 * dialogs (About, and the three tile modals) to it, so they stay modal and
	 * centred on the app. The View knows about the window; that is
	 * presentation state, not
	 * domain state.
	 */
	private final Stage stage;

	/** Runs when the user picks Quit (or hits the accelerator). */
	private final Runnable quitAction;

	@FXML
	private Label welcomeText;

	@FXML
	private ImageView logoImage;

	@FXML
	private Button helloButton;

	/**
	 * The window's menu bar. Injected so the bar can be handed over to the
	 * macOS system menu bar below; the items themselves are wired purely
	 * through the {@code onAction} handlers in hello-view.fxml.
	 */
	@FXML
	private MenuBar menuBar;

	public HelloController(HelloViewModel viewModel, Stage stage, Runnable quitAction) {
		this.viewModel = viewModel;
		this.stage = stage;
		this.quitAction = quitAction;
	}

	@FXML
	private void initialize() {
		welcomeText.textProperty().bind(viewModel.welcomeMessageProperty());
		logoImage.imageProperty().bind(viewModel.logoImageProperty());
		helloButton.disableProperty().bind(viewModel.helloButtonDisabledProperty());

		// On macOS, move the menu bar out of the window and into the Apple
		// menu bar at the top of the screen. JavaFX implements this by
		// converting the MenuItems to a native NSMenu, including the
		// accelerators, so ⌘Q stays a real key equivalent rather than a
		// JavaFX-internal shortcut handler.
		//
		// Guarded by an explicit OS check on purpose: Glass only reports
		// "system menu bar supported" for macOS, so the flag is a no-op
		// anywhere else, but saying so in code keeps the intent obvious and
		// keeps the snug-packaged Windows build on the in-window bar. Same
		// os.name check the Gradle build uses to pick its JavaFX classifier.
		//
		// Note: JavaFX silently ignores the request if any menu contains a
		// CustomMenuItem (logging a warning), so the FXML must stick to
		// MenuItem / SeparatorMenuItem. Ours does.
		if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")) {
			menuBar.setUseSystemMenuBar(true);
		}
	}

	@FXML
	protected void onHelloButtonClick() {
		viewModel.onHelloButtonClicked();
	}

	@FXML
	protected void onLogoMouseClick(MouseEvent event) {
		viewModel.onLogoClicked(isInCoffeeRect(event));
	}

	/**
	 * Show the About dialog. Parented to the stage so it blocks the window and
	 * stays centred over it rather than floating as a stray task-bar window.
	 * {@code showAndWait} also keeps the handler on the FX thread until the
	 * user dismisses it — appropriate for a small modal, and never reached
	 * from the app's own background work.
	 */
	@FXML
	protected void onAbout() {
		InfoModal.show(stage, "About " + APP_NAME, APP_NAME,
				"Version " + APP_VERSION + "\n\n" + ABOUT_BLURB, LOGO_URL, ABOUT_LOGO_WIDTH);
	}

	/** The Build tile: what Snug Builder does. */
	@FXML
	protected void onBuildIconClick() {
		InfoModal.show(stage, BUILD_TITLE, BUILD_TITLE, BUILD_BLURB, ICON_BUILD_URL, ICON_WIDTH);
	}

	/** The dropper tile: the quickest way in — drag a JAR, get a demo app. */
	@FXML
	protected void onDropperIconClick() {
		InfoModal.show(stage, DROPPER_TITLE, DROPPER_TITLE, DROPPER_BLURB, ICON_DROPPER_URL, ICON_WIDTH);
	}

	/** The Preview tile: what Snug Preview does. */
	@FXML
	protected void onPreviewIconClick() {
		InfoModal.show(stage, PREVIEW_TITLE, PREVIEW_TITLE, PREVIEW_BLURB, ICON_PREVIEW_URL, ICON_WIDTH);
	}

	/**
	 * Quit. Deliberately delegates instead of calling {@code Platform.exit()}
	 * / {@code System.exit()} itself, so the menu and the window's close
	 * button run identical shutdown logic.
	 */
	@FXML
	protected void onQuit() {
		quitAction.run();
	}

	/**
	 * Translate a MouseEvent on the ImageView into the source artwork's
	 * native pixel space and report whether the point falls inside the
	 * editable coffee-cup rectangle. Pure presentational geometry — the
	 * Model only receives the boolean outcome.
	 */
	private boolean isInCoffeeRect(MouseEvent event) {
		Image img = logoImage.getImage();
		if (img == null || img.getWidth() <= 0) {
			return false;
		}
		double scale = logoImage.getBoundsInLocal().getWidth() / img.getWidth();
		double imgX = event.getX() / scale;
		double imgY = event.getY() / scale;
		return imgX >= COFFEE_CUP_X && imgX <= COFFEE_CUP_X + COFFEE_CUP_W
				&& imgY >= COFFEE_CUP_Y && imgY <= COFFEE_CUP_Y + COFFEE_CUP_H;
	}
}
