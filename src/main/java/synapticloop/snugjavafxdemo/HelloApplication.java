package synapticloop.snugjavafxdemo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 300, 400);

		// Window icon — the snug logo (loaded from the classpath root).
		stage.getIcons().add(new Image(HelloApplication.class.getResourceAsStream("/imageassets/snug-logo.png")));

		stage.setTitle("Snug");
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
	}
}
