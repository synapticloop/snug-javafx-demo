package synapticloop.snug.demo.javafx.viewmodel;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import javafx.util.Duration;
import synapticloop.snug.demo.javafx.model.LogoState;
import synapticloop.snug.demo.javafx.model.ModelListener;
import synapticloop.snug.demo.javafx.model.SnugModel;

/**
 * ViewModel for the hello window. Bridges the pure-Java {@link SnugModel} and
 * the JavaFX-backed View.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Owns the animation timing primitives ({@link Timeline} for the wave,
 *       {@link PauseTransition} for the ooh auto-revert). These are JavaFX
 *       objects and therefore live on this side of the boundary.</li>
 *   <li>Exposes JavaFX observable properties ({@link #welcomeMessageProperty()},
 *       {@link #logoImageProperty()}, {@link #helloButtonDisabledProperty()})
 *       that the View binds to.</li>
 *   <li>Subscribes to {@link SnugModel} events and translates them into
 *       property updates plus animation start/stop calls.</li>
 *   <li>Resolves the model's {@link LogoState} + wave frame into a concrete
 *       {@link Image} for the View to render.</li>
 * </ul>
 */
public class HelloViewModel {
	private static final String LOGO_URL = "/assets/images/snug-logo.png";
	private static final String LOGO_WAVE_URL = "/assets/images/snug-logo-wave.png";
	private static final String LOGO_OOH_URL = "/assets/images/snug-logo-ooh.png";

	/** Per-state duration for the wave animation. Edit to make the wave faster/slower. */
	private static final Duration WAVE_STEP = Duration.millis(100);

	/**
	 * How long the ooh image stays after a click on the logo (outside the
	 * coffee rectangle) before auto-reverting to the logo. Each re-click
	 * resets this timer, so rapid successive clicks keep the ooh image on
	 * until the user idles for a full OOH_DURATION.
	 */
	private static final Duration OOH_DURATION = Duration.millis(1000);

	private final SnugModel model;

	private final StringProperty welcomeMessage = new SimpleStringProperty(this, "welcomeMessage");
	private final ObjectProperty<Image> logoImage = new SimpleObjectProperty<>(this, "logoImage");
	private final BooleanProperty helloButtonDisabled = new SimpleBooleanProperty(this, "helloButtonDisabled", false);

	private Timeline waveTimeline;
	private PauseTransition oohTimer;

	public HelloViewModel(SnugModel model) {
		this.model = model;

		// Seed exposed properties from initial model state so the View never
		// has to handle "uninitialised" values.
		welcomeMessage.set(model.getWelcomeMessage());
		logoImage.set(resolveImage(model.getLogoState(), model.getWaveFrame(), model.isWavePlaying()));
		helloButtonDisabled.set(model.isWavePlaying());

		model.addListener(new ModelListener() {
			@Override
			public void onWelcomeMessageChanged(String newMessage) {
				welcomeMessage.set(newMessage);
			}

			@Override
			public void onLogoStateChanged(LogoState newState, int waveFrame, boolean wavePlaying) {
				logoImage.set(resolveImage(newState, waveFrame, wavePlaying));
				helloButtonDisabled.set(wavePlaying);
				if (wavePlaying) {
					startWaveAnimation();
				} else {
					stopWaveAnimation();
				}
				// Wave start cancels any pending ooh auto-revert (it would
				// otherwise fire mid-animation and snap the image back to
				// logo while a wave frame is showing).
				if (newState != LogoState.OOH) {
					stopOohTimer();
				}
			}

			@Override
			public void onOohTimerReset() {
				startOohTimer();
			}
		});
	}

	/**
	 * Map (state, waveFrame, wavePlaying) to the image the View should render.
	 * The wave alternates logo↔wave on each frame (even = logo, odd = wave),
	 * matching the original controller's Timeline keyframes.
	 */
	private Image resolveImage(LogoState state, int waveFrame, boolean wavePlaying) {
		return switch (state) {
			case LOGO -> new Image(LOGO_URL);
			case WAVE -> (waveFrame % 2 == 0) ? new Image(LOGO_URL) : new Image(LOGO_WAVE_URL);
			case OOH -> new Image(LOGO_OOH_URL);
		};
	}

	private void startWaveAnimation() {
		if (waveTimeline != null) {
			waveTimeline.stop();
		}
		waveTimeline = new Timeline(
				new KeyFrame(WAVE_STEP.multiply(1), e -> model.advanceWave()),
				new KeyFrame(WAVE_STEP.multiply(2), e -> model.advanceWave()),
				new KeyFrame(WAVE_STEP.multiply(3), e -> model.advanceWave()),
				new KeyFrame(WAVE_STEP.multiply(4), e -> model.advanceWave()),
				new KeyFrame(WAVE_STEP.multiply(5), e -> model.advanceWave()),
				// Final frame: advanceWave() transitions state back to LOGO
				// and re-enables the hello button via the listener above.
				new KeyFrame(WAVE_STEP.multiply(6), e -> model.advanceWave())
		);
		waveTimeline.play();
	}

	private void stopWaveAnimation() {
		if (waveTimeline != null) {
			waveTimeline.stop();
			waveTimeline = null;
		}
	}

	private void startOohTimer() {
		if (oohTimer != null) {
			oohTimer.stop();
		}
		oohTimer = new PauseTransition(OOH_DURATION);
		oohTimer.setOnFinished(e -> model.oohTimerExpired());
		oohTimer.play();
	}

	private void stopOohTimer() {
		if (oohTimer != null) {
			oohTimer.stop();
			oohTimer = null;
		}
	}

	// --- Commands called by the View ---

	public void onHelloButtonClicked() {
		model.requestHello();
	}

	public void onLogoClicked(boolean isInCoffeeRect) {
		model.logoClicked(isInCoffeeRect);
	}

	// --- Properties exposed to the View ---

	public StringProperty welcomeMessageProperty() {
		return welcomeMessage;
	}

	public ObjectProperty<Image> logoImageProperty() {
		return logoImage;
	}

	public BooleanProperty helloButtonDisabledProperty() {
		return helloButtonDisabled;
	}
}
