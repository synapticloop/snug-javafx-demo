module synapticloop.snug.demo.javafx {
	requires javafx.controls;
	requires javafx.fxml;


	// FXMLLoader uses reflection to inject @FXML fields and call @FXML
	// methods on the controller, so the controller's package must be open.
	opens synapticloop.snug.demo.javafx.controller to javafx.fxml;

	exports synapticloop.snug.demo.javafx;
}
