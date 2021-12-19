package Controls;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ResourceBundle;

import Util.InputValidation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class pickDateAndTimeController implements Initializable {

	private Router router;

	private Stage stage;

	private Scene scene;

	private String restaurantName;

	@FXML
	private Rectangle avatar;

	@FXML
	private DatePicker datePicker;

	@FXML
	private Text homePageBtn;

	@FXML
	private ComboBox<String> hourBox;

	@FXML
	private Text itemsCounter;

	@FXML
	private ImageView leftArrowBtn;

	@FXML
	private Text logoutBtn;

	@FXML
	private ComboBox<String> minutesBox;

	@FXML
	private Text profileBtn;

	@FXML
	private Text restaurantsBtn;

	@FXML
	private Label nextOrderStep;

	@FXML
	private Label errorMsg;

	@FXML
	void nextOrderStep(MouseEvent event) {
		if (!checkDate()) {
			return;
		}
		if (!checkTime()) {
			return;
		}
		errorMsg.setText("");
		/** Set date and time for order */
		LocalDate orderDate = datePicker.getValue();
		String hourToOrder = hourBox.getSelectionModel().getSelectedItem();
		String minuteToOrder = minutesBox.getSelectionModel().getSelectedItem();
		String newTime = orderDate.toString() + " " + hourToOrder + ":" + minuteToOrder;
		router.getOrder().setDateTime(newTime);
		errorMsg.setText("");
		changeToDelivery();
	}

	private void changeToDelivery() {
		router = Router.getInstance();
		if (router.getDeliveryMethodController() == null) {
			AnchorPane mainContainer;
			deliveryMethodController controller;
			try {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource("../gui/bitemeSelectDeliveryMethodPage.fxml"));
				mainContainer = loader.load();
				controller = loader.getController();
				controller.setAvatar();
				controller.setItemsCounter();
				controller.createCombo();
				Scene mainScene = new Scene(mainContainer);
				mainScene.getStylesheets().add(getClass().getResource("../gui/style.css").toExternalForm());
				controller.setScene(mainScene);
				stage.setTitle("BiteMe - Select Delivery Method");
				stage.setScene(mainScene);
				stage.show();
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		} else {
			router.getPickDateAndTimeController().setRestaurant(restaurantName);
			router.getPickDateAndTimeController().setAvatar();
			router.getPickDateAndTimeController().setItemsCounter();
			router.getPickDateAndTimeController().createCombos();
			stage.setTitle("BiteMe - Select Delivery Method");
			stage.setScene(router.getDeliveryMethodController().getScene());
			stage.show();
		}
	}

	/**
	 * Private method to check if the input date is today's date.
	 */
	private boolean checkDate() {
		LocalDate orderDate = datePicker.getValue();
		if (orderDate == null) {
			errorMsg.setText("Please pick date");
			return false;
		}
		if (!InputValidation.checkDateNow(orderDate)) {
			errorMsg.setText("Date must be today :" + LocalDate.now().toString());
			return false;
		}
		return true;
	}

	/**
	 * Private method to check if the input time is valid time.
	 */
	private boolean checkTime() {
		String hourToOrder = hourBox.getSelectionModel().getSelectedItem();
		String minuteToOrder = minutesBox.getSelectionModel().getSelectedItem();
		/** If no time selection was made */
		if (!InputValidation.checkValidText(hourToOrder) || !InputValidation.checkValidText(minuteToOrder)) {
			errorMsg.setText("Please pick time for the order.");
			return false;
		}
		if (InputValidation.checkContainCharacters(hourToOrder)
				|| InputValidation.checkContainCharacters(minuteToOrder)) {
			errorMsg.setText("Time should be integers.");
			return false;
		}
		/** Checking if the inputed time is valid. */
		switch (InputValidation.checkTime(Integer.parseInt(hourToOrder), Integer.parseInt(minuteToOrder))) {
		case -1:
		case 1:
			errorMsg.setText("Time of order must be greater or equals to now: " + LocalTime.now().toString());
			return false;
		}
		/** If time selection is before now */
		return true;
	}

	@FXML
	void logoutClicked(MouseEvent event) {
		router.logOut();
	}

	@FXML
	void openProfile(MouseEvent event) {
		router.showProfile();
	}

	@FXML
	void returnToHomePage(MouseEvent event) {
		router.changeSceneToHomePage();
	}

	@FXML
	void returnToMenuOrderPage(MouseEvent event) {
		router.getRestaurantMenuController().setItemsCounter();
		router.getRestaurantMenuController().setAvatar();
		router.getRestaurantMenuController().setRestaurantName(restaurantName);
		stage.setTitle("BiteMe - " + restaurantName + " Menu");
		stage.setScene(router.getRestaurantMenuController().getScene());
		stage.show();
	}

	@FXML
	void returnToRestaurants(MouseEvent event) {
		router.getRestaurantselectionController().setItemsCounter();
		router.getRestaurantMenuController().setAvatar();
		stage.setTitle("BiteMe - Restaurants");
		stage.setScene(router.getRestaurantselectionController().getScene());
		stage.show();
	}

	/**
	 * Setting the avatar image of the user.
	 */
	public void setAvatar() {
		router.setAvatar(avatar);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		router = Router.getInstance();
		router.setPickDateAndTimeController(this);
		setStage(router.getStage());
		/** Setting the time and date to now. */
		datePicker.setValue(LocalDate.now());
		hourBox.getSelectionModel().select(String.format("%02d", LocalTime.now().getHour()));
		minutesBox.getSelectionModel().select(String.format("%02d", LocalTime.now().getMinute()));
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public Scene getScene() {
		return scene;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setRestaurant(String restaurantName) {
		this.restaurantName = restaurantName;
	}

	public void setItemsCounter() {
		itemsCounter.setText(router.getBagItems().size() + "");
	}

	/**
	 * Setting values for the combo boxes. hourBox values from 0-23, minutesBox
	 * values from 0-59.
	 */
	public void createCombos() {
		ObservableList<String> hourOptions = FXCollections.observableArrayList(Arrays.asList(router.generator(24)));
		hourBox.getItems().addAll(hourOptions);
		ObservableList<String> minuteOptions = FXCollections.observableArrayList(Arrays.asList(router.generator(60)));
		minutesBox.getItems().addAll(minuteOptions);
	}
}
