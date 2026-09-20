module synapticloop.snugjavafxdemo {
	requires javafx.controls;
	requires javafx.fxml;


	opens synapticloop.snugjavafxdemo to javafx.fxml;
	exports synapticloop.snugjavafxdemo;
}