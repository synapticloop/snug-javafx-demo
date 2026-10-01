package synapticloop.snug.demo.javafx.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Domain model for the snug demo window.
 *
 * <p>Owns the application's domain state and rules:
 * <ul>
 *   <li>The current {@link LogoState} of the logo.</li>
 *   <li>Whether the wave animation is currently playing, and which frame it
 *       is on (0..{@link #WAVE_FRAMES}-1).</li>
 *   <li>The welcome-label text.</li>
 * </ul>
 *
 * <p>Pure Java: no JavaFX imports. Animation timing lives in the ViewModel;
 * pixel-coord hit-testing lives in the View. The Model only knows that
 * "outside the coffee rect → swap to ooh and ask the VM to start a revert
 * timer", not how that timer is implemented.</p>
 */
public class SnugModel {
	/** Total frames in one wave animation (logo, wave, logo, wave, logo, wave, logo). */
	public static final int WAVE_FRAMES = 6;

	private LogoState logoState = LogoState.LOGO;
	private int waveFrame = 0;
	private boolean wavePlaying = false;
	private String welcomeMessage = "";

	private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();

	public void addListener(ModelListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ModelListener listener) {
		listeners.remove(listener);
	}

	public LogoState getLogoState() {
		return logoState;
	}

	public int getWaveFrame() {
		return waveFrame;
	}

	public boolean isWavePlaying() {
		return wavePlaying;
	}

	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	/**
	 * User clicked the "Hello!" button. Domain rule: ignored while a wave is
	 * already playing.
	 */
	public void requestHello() {
		if (wavePlaying) {
			return;
		}
		welcomeMessage = "Welcome to snug's JavaFX Application!";
		wavePlaying = true;
		waveFrame = 0;
		logoState = LogoState.WAVE;
		fireWelcomeMessageChanged();
		fireLogoStateChanged();
	}

	/**
	 * Advance the wave animation by one frame. The ViewModel's
	 * {@link javafx.animation.Timeline} drives this; the Model decides when
	 * the animation ends (frame index reaches {@link #WAVE_FRAMES}).
	 */
	public void advanceWave() {
		if (!wavePlaying) {
			return;
		}
		waveFrame++;
		if (waveFrame >= WAVE_FRAMES) {
			wavePlaying = false;
			waveFrame = 0;
			logoState = LogoState.LOGO;
		}
		fireLogoStateChanged();
	}

	/**
	 * User clicked somewhere on the logo. Domain rule:
	 * <ul>
	 *   <li>During a wave: ignore (the View also disables the hello button,
	 *       which blocks its onAction; this catches logo clicks).</li>
	 *   <li>Inside the coffee rect: set the welcome message.</li>
	 *   <li>Outside the coffee rect: swap to ooh and signal the ViewModel to
	 *       start its auto-revert timer. Fires {@link ModelListener#onOohTimerReset()}
	 *       even when already in ooh state, so successive clicks reset the
	 *       countdown rather than queueing a second revert.</li>
	 * </ul>
	 */
	public void logoClicked(boolean isInCoffeeRect) {
		if (wavePlaying) {
			return;
		}
		if (isInCoffeeRect) {
			welcomeMessage = "Mmmmm - coffee";
			fireWelcomeMessageChanged();
			return;
		}
		logoState = LogoState.OOH;
		fireLogoStateChanged();
		fireOohTimerReset();
	}

	/**
	 * The auto-revert timer the ViewModel was running has elapsed. Domain
	 * rule: revert to {@link LogoState#LOGO}, unless a wave is in progress
	 * (the timer was supposed to be cancelled by then, but guard anyway).
	 */
	public void oohTimerExpired() {
		if (wavePlaying) {
			return;
		}
		logoState = LogoState.LOGO;
		fireLogoStateChanged();
	}

	private void fireWelcomeMessageChanged() {
		for (ModelListener l : listeners) {
			l.onWelcomeMessageChanged(welcomeMessage);
		}
	}

	private void fireLogoStateChanged() {
		for (ModelListener l : listeners) {
			l.onLogoStateChanged(logoState, waveFrame, wavePlaying);
		}
	}

	private void fireOohTimerReset() {
		for (ModelListener l : listeners) {
			l.onOohTimerReset();
		}
	}
}
