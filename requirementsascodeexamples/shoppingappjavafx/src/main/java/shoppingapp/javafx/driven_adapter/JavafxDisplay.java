package shoppingapp.javafx.driven_adapter;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import shoppingapp.boundary.driven_port.Display;
import shoppingapp.boundary.driver_port.IReactToCommands;
import shoppingapp.boundary.internal.domain.Products;
import shoppingapp.boundary.internal.domain.PurchaseOrder;
import shoppingapp.boundary.internal.domain.ShippingInformation;
import shoppingapp.javafx.controller.AbstractController;
import shoppingapp.javafx.controller.DisplayPaymentDetailsFormController;
import shoppingapp.javafx.controller.DisplayProductsController;
import shoppingapp.javafx.controller.DisplayPurchaseOrderSummaryController;
import shoppingapp.javafx.controller.DisplayShippingInformationFormController;

public class JavafxDisplay implements Display {
	private static final String RELATIVE_FXML_PACKAGE_NAME = "fxml";
	private Stage primaryStage;
	private VBox vBox;
	private AbstractController controller;
	private IReactToCommands boundary;

	public JavafxDisplay(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	@Override
	public void displayProducts(Products products, PurchaseOrder purchaseOrder) {
		loadAndDisplay("DisplayProducts.fxml");
		DisplayProductsController displayProductsController = (DisplayProductsController) controller;
		displayProductsController.displayProducts(products, purchaseOrder);
	}

	@Override
	public void displayShippingInformationForm(ShippingInformation shippingInformation) {
		loadAndDisplay("DisplayShippingInformationForm.fxml");
		DisplayShippingInformationFormController displayShippingInformationFormController = (DisplayShippingInformationFormController) controller;
		displayShippingInformationFormController.displayShippingInformationForm(shippingInformation);
	}

	@Override
	public void displayPaymentDetailsForm() {
		loadAndDisplay("DisplayPaymentDetailsForm.fxml");
		DisplayPaymentDetailsFormController displayPaymentDetailsFormController = (DisplayPaymentDetailsFormController) controller;
		displayPaymentDetailsFormController.displayPaymentDetails();
	}

	@Override
	public void displayPurchaseOrderSummary(PurchaseOrder purchaseOrder) {
		loadAndDisplay("DisplayPurchaseOrderSummary.fxml");
		DisplayPurchaseOrderSummaryController displayPurchaseOrderSummaryController = (DisplayPurchaseOrderSummaryController) controller;
		displayPurchaseOrderSummaryController.displayPurchaseOrderSummary(purchaseOrder);
	}

	private void loadAndDisplay(String fxmlFileName) {
		loadFXML(fxmlFileName);
		Scene productsScene = new Scene(vBox);
		primaryStage.setScene(productsScene);
	}

	private void loadFXML(String fxmlFileName) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(RELATIVE_FXML_PACKAGE_NAME + "/" + fxmlFileName));

			vBox = loader.load();
			controller = (AbstractController) loader.getController();
			controller.setBoundary(boundary);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void setBoundary(IReactToCommands boundary) {
		this.boundary = boundary;
	}
}
