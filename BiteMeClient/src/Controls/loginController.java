package Controls;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Entities.User;
import client.ClientGUI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * Login form controller.
 * 
 * @author Shaked
 * @author Natali
 * 
 * @version December 05 2021, v1.0
 */
public class loginController implements Initializable {

	private Router router;

	private Stage stage;

	private Scene scene;

	@FXML
	private ImageView back;

	@FXML
	private Label errorMsg;

	@FXML
	private Label loginBtn;

	@FXML
	private PasswordField passwordTxt;

	@FXML
	private TextField usernameTxt;

	@FXML
	private Rectangle avatar;

	/**
	 * Onclick event handler, returning to homepage.
	 * 
	 * @param MouseEvent event
	 */
	@FXML
	void backToHomePage(MouseEvent event) {
		changeScenes();
	}

	/**
	 * Onclick event handler, checking for valid username and password inputs and
	 * requesting a login function from the server. On success login switch scene to
	 * homepage. On failed login display a message.
	 * 
	 * @param MouseEvent event.
	 */
	@FXML
	void loginClicked(MouseEvent event) {
		String userName = usernameTxt.getText();
		String password = passwordTxt.getText();
		if(!CheckUserInput(userName, password)) {
			return;
		}
		ClientGUI.client.login(userName, password);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (ClientGUI.monitor) {
					try {
						ClientGUI.monitor.wait();
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			}
		});
		t.start();
		try {
			t.join();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if(!checkServerResponse()) {
			return;
		}
		changeScenes();

	}

	/**
	 * checks the user information received from Server.
	 * display relevant information.
	 */
	private boolean checkServerResponse() {
		if(ClientGUI.client.getUser() == null) {
			return false;
		}
		switch (ClientGUI.client.getUser().getMsg().toLowerCase()) {
		case "already logged in":
			errorMsg.setText("This user is already logged in");
			return false;
		case "not found":
			errorMsg.setText("Username or password are incorrect");
			return false;
		case "not authorized":
			errorMsg.setText("User not authorized to use the system");
			return false;
		case "internal error":
			errorMsg.setText("Server error, can't logged in");
			return false;
		default:
			usernameTxt.clear();
			passwordTxt.clear();
			return true;
		}
	}

	private boolean CheckUserInput(String userName, String password) {
		if (!checkValidText(userName)) {
			errorMsg.setText("Must fill username");
			return false;
		}
		if (checkSpecialCharacters(userName)) {
			errorMsg.setText("Special characters aren't allowed in username");
			return false;
		}
		if (!checkValidText(password)) {
			errorMsg.setText("Must fill password");
			return false;
		}
		if (checkSpecialCharacters(password)) {
			errorMsg.setText("Special characters aren't allowed in password");
			return false;
		}
		errorMsg.setText("");
		return true;
	}

	/**
	 * Changing the current scene to homepage.
	 */
	private void changeScenes() {
		if (ClientGUI.client.getUser() != null) {
			router.getHomePageController().setProfile(true);
		}else {
			router.getHomePageController().setProfile(false);
		}
		stage.setTitle("BiteMe - HomePage");
		stage.setScene(router.getHomePageController().getScene());
		stage.show();
	}

	/**
	 * Checks if the string is not empty.
	 * 
	 * @param String text
	 * @return boolean
	 */
	private boolean checkValidText(String input) {
		if (input == null || input.equals("")) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if the string contains special characters.
	 * 
	 * @param String input
	 * @return boolean
	 */
	public boolean checkSpecialCharacters(String input) {
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(input);
		return m.find();
	}

	/**
	 * Setting the stage instance.
	 * 
	 * @param Stage stage
	 */
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setAvatar() {
		router.setAvatar(avatar);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		router = Router.getInstance();
		router.setLogincontroller(this);
		setStage(router.getStage());
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public Scene getScene() {
		return scene;
	}
	
	@FXML
	public void onEnter(ActionEvent ae){
		loginClicked(null);
	}
}
