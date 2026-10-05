package synapticloop.snug.demo.javafx.view;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Builds the app's read-only information modals: the menu bar's About box and
 * the one behind each Snug Tools tile.
 *
 * <p>Every modal in this app is the same shape — an INFORMATION {@link Alert}
 * with a leading graphic, a bold heading, a line or two of text, and the
 * window's own theme. Only the wording and artwork change, so that variation
 * is what callers supply; everything that makes a modal look and behave like
 * part of this app lives here instead of in the view controller.</p>
 *
 * <p>Stateless and never reused: each call constructs a fresh {@link Alert}
 * and blocks until the user dismisses it.</p>
 */
public final class InfoModal {

	/**
	 * The window stylesheet, as an absolute classpath path — the same file
	 * hello-view.fxml references as {@code @styles.css}.
	 *
	 * <p>Absolute on purpose: this class sits two packages below the FXML and
	 * the stylesheet it loads, so a package-relative {@code "styles.css"}
	 * would resolve to a {@code .../view/styles.css} that does not exist.</p>
	 */
	private static final String STYLESHEET_RESOURCE = "/synapticloop/snug/demo/javafx/styles.css";

	private InfoModal() {
		// Utility: static entry point only.
	}

	/**
	 * Build, parent, theme and show a modal INFORMATION {@link Alert}, then
	 * block until the user dismisses it.
	 *
	 * <p>Parented to {@code owner} and set APPLICATION_MODAL so it blocks the
	 * window and stays centred over it rather than floating as a stray
	 * task-bar window. {@code showAndWait} keeps the calling handler on the FX
	 * thread until dismissal, which is fine for a small read-only modal and is
	 * never reached from the app's own background work.</p>
	 *
	 * @param owner        the window to parent the modal to
	 * @param title        window title
	 * @param header       bold heading text, or {@code null} for none
	 * @param message      the body text
	 * @param graphicUrl   classpath resource for the leading graphic
	 * @param graphicWidth rendered width of that graphic
	 */
	public static void show(Stage owner, String title, String header, String message,
			String graphicUrl, double graphicWidth) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.initOwner(owner);
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);

		ImageView graphic = new ImageView(new Image(graphicUrl));
		graphic.setFitWidth(graphicWidth);
		graphic.setPreserveRatio(true);
		graphic.setSmooth(true);
		alert.setGraphic(graphic);

		applyAppStyling(alert);
		alert.showAndWait();
	}

	/**
	 * Attach the window's own stylesheet to a modal's Scene.
	 *
	 * <p>A {@link Dialog} builds its own {@link Scene} and does <em>not</em>
	 * inherit the owner's stylesheets, so a modal would otherwise come up in
	 * stock Modena grey against a cream app.</p>
	 *
	 * <p>The Scene is reached through {@link
	 * javafx.scene.control.DialogPane#getScene()}, and it already exists by
	 * the time {@code createDialogPane()} has run — before the dialog is
	 * ever shown. So this is a plain assignment with no showing-event hook
	 * and no listener.</p>
	 *
	 * <p>Two plausible-looking alternatives do NOT work, and are worth
	 * recording so nobody re-derives them: the ON_SHOWING event's source is
	 * the {@link Alert} itself, not its Window, so casting the event source
	 * to a Window always fails; and the pane's {@code sceneProperty()} is
	 * already set before a listener can be attached, so it never fires with
	 * the real Scene — only with null, on teardown.</p>
	 *
	 * <p>Silently does nothing if the stylesheet cannot be found. A themed
	 * dialog is a presentation nicety, and failing to open it over a missing
	 * resource would be a far worse outcome than an unthemed one. Bear in
	 * mind that this guard is also what hides a wrong
	 * {@link #STYLESHEET_RESOURCE} — if the modals come up in stock Modena
	 * grey, that constant is the first thing to check.</p>
	 */
	private static void applyAppStyling(Dialog<?> dialog) {
		URL stylesheet = InfoModal.class.getResource(STYLESHEET_RESOURCE);
		if (stylesheet == null) {
			return;
		}
		Scene scene = dialog.getDialogPane().getScene();
		if (scene != null) {
			String url = stylesheet.toExternalForm();
			if (!scene.getStylesheets().contains(url)) {
				scene.getStylesheets().add(url);
			}
		}
	}
}
