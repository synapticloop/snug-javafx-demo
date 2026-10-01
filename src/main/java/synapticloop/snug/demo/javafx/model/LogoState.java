package synapticloop.snug.demo.javafx.model;

/**
 * Domain-level state of the snug logo. The model speaks in these states; the
 * ViewModel resolves a state into an actual {@link javafx.scene.image.Image}
 * for the View to render.
 */
public enum LogoState {
	/** Default resting image. */
	LOGO,
	/** Mid-wave animation. The current frame is tracked separately on the model. */
	WAVE,
	/** "Surprised" image shown briefly after a click on the logo outside the coffee cup. */
	OOH
}
