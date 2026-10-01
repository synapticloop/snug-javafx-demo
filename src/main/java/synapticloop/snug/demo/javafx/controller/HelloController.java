package synapticloop.snug.demo.javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import synapticloop.snug.demo.javafx.viewmodel.HelloViewModel;

/**
 * View-layer glue for the hello window.
 *
 * <p>Stays intentionally thin: holds the {@code @FXML}-injected scene-graph
 * nodes, binds them to {@link HelloViewModel} properties, and translates user
 * input into ViewModel commands. Pixel-coord hit-testing for the coffee-cup
 * rectangle is a presentational geometry concern and lives here, not in the
 * Model.</p>
 */
public class HelloController {
	// Hot-spot rectangle covering the coffee cup in snug-logo.png (native pixel
	// coordinates of the 256x256 source image). Edit these four values to move
	// or resize the clickable area without touching the hit-test logic below.
	private static final double COFFEE_CUP_X = 95.0;
	private static final double COFFEE_CUP_Y = 155.0;
	private static final double COFFEE_CUP_W = 70.0;
	private static final double COFFEE_CUP_H = 45.0;

	private final HelloViewModel viewModel;

	@FXML
	private Label welcomeText;

	@FXML
	private ImageView logoImage;

	@FXML
	private Button helloButton;

	public HelloController(HelloViewModel viewModel) {
		this.viewModel = viewModel;
	}

	@FXML
	private void initialize() {
		welcomeText.textProperty().bind(viewModel.welcomeMessageProperty());
		logoImage.imageProperty().bind(viewModel.logoImageProperty());
		helloButton.disableProperty().bind(viewModel.helloButtonDisabledProperty());
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
