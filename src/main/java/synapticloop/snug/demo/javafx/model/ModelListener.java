package synapticloop.snug.demo.javafx.model;

/**
 * Callback surface for the Model → ViewModel boundary.
 *
 * <p>The Model fires these on any state change; the ViewModel translates them
 * into JavaFX property updates for the View. Splitting into discrete methods
 * (rather than a single "everything changed" event) lets the ViewModel decide
 * per-event whether to start/stop a Timeline or PauseTransition.</p>
 */
public interface ModelListener {
	/** Welcome-label text changed. {@code newMessage} is the new value. */
	void onWelcomeMessageChanged(String newMessage);

	/**
	 * Logo state changed. Fired on every transition (including frame advances
	 * during the wave animation and the final transition back to {@link LogoState#LOGO}).
	 */
	void onLogoStateChanged(LogoState newState, int waveFrame, boolean wavePlaying);

	/**
	 * A click on the logo (outside the coffee rect) was registered and the
	 * ViewModel should (re)start its auto-revert timer. Fired even when the
	 * state is already {@link LogoState#OOH}, so rapid successive clicks reset
	 * the countdown rather than queueing a second revert.
	 */
	void onOohTimerReset();
}
