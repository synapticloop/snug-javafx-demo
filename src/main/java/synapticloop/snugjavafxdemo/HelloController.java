package synapticloop.snugjavafxdemo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

public class HelloController {
	private static final String LOGO_URL = "/assets/images/snug-logo.png";
	private static final String LOGO_WAVE_URL = "/assets/images/snug-logo-wave.png";

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
}