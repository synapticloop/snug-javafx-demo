package synapticloop.snugjavafxdemo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class HelloController {
	private static final String LOGO_URL = "/assets/images/snug-logo.png";
	private static final String LOGO_WAVE_URL = "/assets/images/snug-logo-wave.png";
	private static final String LOGO_OOH_URL = "/assets/images/snug-logo-ooh.png";

	// Hot-spot rectangle covering the coffee cup in snug-logo.png (native pixel
	// coordinates of the 256x256 source image). Edit these four values to move
	// or resize the clickable area without touching the hit-test logic below.
	private static final double COFFEE_CUP_X = 95.0;
	private static final double COFFEE_CUP_Y = 155.0;
	private static final double COFFEE_CUP_W = 70.0;
	private static final double COFFEE_CUP_H = 45.0;

	@FXML
	private Label welcomeText;

	@FXML
	private ImageView logoImage;

	@FXML
	protected void onHelloButtonClick() {
		welcomeText.setText("Welcome to snug's JavaFX Application!");
	}

	@FXML
	protected void onHelloButtonPress(MouseEvent event) {
		logoImage.setImage(new Image(LOGO_WAVE_URL));
	}

	@FXML
	protected void onHelloButtonRelease(MouseEvent event) {
		logoImage.setImage(new Image(LOGO_URL));
	}

	@FXML
	protected void onLogoMousePress(MouseEvent event) {
		logoImage.setImage(new Image(LOGO_OOH_URL));
	}

	@FXML
	protected void onLogoMouseRelease(MouseEvent event) {
		logoImage.setImage(new Image(LOGO_URL));
	}

	@FXML
	protected void onLogoMouseClick(MouseEvent event) {
		Image img = logoImage.getImage();
		if (img == null || img.getWidth() <= 0) {
			return;
		}
		// Scale the click from ImageView-rendered coords back to the source
		// image's native pixel coords so the rectangle constants stay in
		// 256x256 space regardless of how the ImageView is laid out.
		double scale = logoImage.getBoundsInLocal().getWidth() / img.getWidth();
		double imgX = event.getX() / scale;
		double imgY = event.getY() / scale;
		if (imgX >= COFFEE_CUP_X && imgX <= COFFEE_CUP_X + COFFEE_CUP_W
				&& imgY >= COFFEE_CUP_Y && imgY <= COFFEE_CUP_Y + COFFEE_CUP_H) {
			welcomeText.setText("Mmmmm - coffee");
		}
	}
}