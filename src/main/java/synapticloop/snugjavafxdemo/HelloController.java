package synapticloop.snugjavafxdemo;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

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

	// Per-state duration for the wave animation: 3 logo↔wave cycles, each
	// "in" or "out" lasts this long. Edit to make the wave faster/slower.
	private static final Duration WAVE_STEP = Duration.millis(100);

	// How long the ooh image stays after a click on the logo (outside the coffee
	// rectangle) before auto-reverting to the logo. Each re-click resets this
	// timer, so rapid successive clicks keep the ooh image on until the user
	// idles for a full OOH_DURATION.
	private static final Duration OOH_DURATION = Duration.millis(1000);

	@FXML
	private Label welcomeText;

	@FXML
	private ImageView logoImage;

	@FXML
	private Button helloButton;

	// Guard for the logo click while the wave animation is running, so the
	// user can't swap to the ooh image and fight the timeline. The Hello
	// button itself is disabled via setDisable(...) which both greys it out
	// and blocks its onAction.
	private boolean animationPlaying = false;

	// Auto-revert timer: each click on the logo (outside the coffee rect) swaps
	// to ooh and (re)starts this timer. Successive clicks reset it; once the
	// user stops clicking for OOH_DURATION, the image reverts to logo.
	private PauseTransition oohTimer;

	@FXML
	protected void onHelloButtonClick() {
		welcomeText.setText("Welcome to snug's JavaFX Application!");
		// Cancel any pending ooh timer so it can't fire mid-animation and snap
		// the image back to logo while a wave frame is showing.
		if (oohTimer != null) {
			oohTimer.stop();
			oohTimer = null;
		}
		helloButton.setDisable(true);
		animationPlaying = true;
		playWaveAnimation();
	}

	private void playWaveAnimation() {
		Image logoImg = new Image(LOGO_URL);
		Image waveImg = new Image(LOGO_WAVE_URL);
		Timeline timeline = new Timeline(
				new KeyFrame(Duration.ZERO, e -> logoImage.setImage(logoImg)),
				new KeyFrame(WAVE_STEP, e -> logoImage.setImage(waveImg)),
				new KeyFrame(WAVE_STEP.multiply(2), e -> logoImage.setImage(logoImg)),
				new KeyFrame(WAVE_STEP.multiply(3), e -> logoImage.setImage(waveImg)),
				new KeyFrame(WAVE_STEP.multiply(4), e -> logoImage.setImage(logoImg)),
				new KeyFrame(WAVE_STEP.multiply(5), e -> logoImage.setImage(waveImg)),
				// Final frame: restore the logo, re-enable button and logo interactions.
				new KeyFrame(WAVE_STEP.multiply(6), e -> {
					logoImage.setImage(logoImg);
					animationPlaying = false;
					helloButton.setDisable(false);
				})
		);
		timeline.play();
	}

	@FXML
	protected void onLogoMouseClick(MouseEvent event) {
		if (animationPlaying) {
			return;
		}
		if (isInCoffeeRect(event)) {
			welcomeText.setText("Mmmmm - coffee");
			return;
		}
		// Outside the coffee rectangle: swap to ooh for OOH_DURATION. Stop any
		// previous timer so re-clicks reset the countdown rather than queueing
		// a second auto-revert.
		logoImage.setImage(new Image(LOGO_OOH_URL));
		if (oohTimer != null) {
			oohTimer.stop();
		}
		oohTimer = new PauseTransition(OOH_DURATION);
		oohTimer.setOnFinished(e -> logoImage.setImage(new Image(LOGO_URL)));
		oohTimer.play();
	}

	/**
	 * Translate a MouseEvent on the ImageView into the source artwork's native
	 * pixel space and report whether the point falls inside the editable
	 * coffee-cup rectangle.
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