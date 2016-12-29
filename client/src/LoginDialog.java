import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;


/**
 * Created by michal on 11/17/16.
 */
public class LoginDialog extends Dialog<Boolean> {

    public LoginDialog(Window owner, Connection c) {
        super();
        this.initOwner(owner);
        this.setTitle("Login");

        DialogPane dialogPane = this.getDialogPane();
        LoginTab loginTab = new LoginTab();
        RegTab regTab = new RegTab();
        TabPane root = new TabPane(loginTab, regTab);
        root.setPadding(new Insets(0));
        root.getSelectionModel().select(loginTab);
        dialogPane.setContent(root);

        ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().setAll(okButton, cancelBtn);

        Button okBtn = (Button) dialogPane.lookupButton(okButton);
        //okBtn.disableProperty().bind(loginTab.logNotOk.or(regTab.regNotOk));
        okBtn.addEventFilter(ActionEvent.ACTION, event -> {

        });

        this.setResultConverter(buttonType -> buttonType == okButton);

        loginTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setTitle(loginTab.getText());
            } else {
                setTitle(regTab.getText());
            }
        });
    }

    private class LoginTab extends Tab {

        private SimpleBooleanProperty logNotOk = new SimpleBooleanProperty(true);

        public LoginTab() {
            super();
            this.setText("Login");
            this.setClosable(false);
            GridPane root = new GridPane();

            TextField username = new TextField();
            Label usernameLabel = new Label("Username:");
            usernameLabel.setLabelFor(username);
            SimpleBooleanProperty uNameNotOk = new SimpleBooleanProperty(true);
            username.textProperty().addListener((ov, oldV, newV) -> {
                uNameNotOk.set(newV.trim().isEmpty());
            });

            CheckBox showPassword = new CheckBox("Unmask");
            showPassword.setSelected(false);

            PasswordField password = new PasswordField();
            password.setSkin(new PasswordFieldSkin(password, showPassword));

            showPassword.selectedProperty().addListener((observable, oldValue, newValue) -> {
                password.setText(password.getText());
            });

            Label passwordLabel = new Label("Password:");
            usernameLabel.setLabelFor(passwordLabel);

            logNotOk.bind(this.selectedProperty().and(uNameNotOk.
                    or(password.textProperty().isEmpty())));

            root.setPadding(new Insets(10));
            root.setAlignment(Pos.CENTER);
            root.setHgap(10);
            root.setVgap(10);
            root.add(usernameLabel, 0, 0);
            root.add(username, 1, 0);
            root.add(passwordLabel, 0, 1);
            root.add(password, 1, 1);
            root.add(showPassword, 0, 2);

            this.setContent(root);
        }
    }

    private class RegTab extends Tab {

        private SimpleBooleanProperty regNotOk = new SimpleBooleanProperty(true);

        public RegTab() {
            super();
            this.setText("Register");
            this.setClosable(false);

            GridPane root = new GridPane();

            TextField firstname = new TextField();
            firstname.setFocusTraversable(true);
            Label firstnameLabel = new Label("First name:");
            firstnameLabel.setLabelFor(firstname);
            SimpleBooleanProperty fNameNotOk = new SimpleBooleanProperty(true);
            firstname.textProperty().addListener((ov, oldV, newV) -> {
                fNameNotOk.set(newV.trim().isEmpty());
            });

            TextField lastname = new TextField();
            lastname.setFocusTraversable(true);
            Label lastnameLabel = new Label("Last name:");
            lastnameLabel.setLabelFor(lastname);
            SimpleBooleanProperty lNameNotOk = new SimpleBooleanProperty(true);
            lastname.textProperty().addListener((ov, oldV, newV) -> {
                lNameNotOk.set(newV.trim().isEmpty());
            });

            TextField username = new TextField();
            username.setFocusTraversable(true);
            Label usernameLabel = new Label("Username:");
            usernameLabel.setLabelFor(username);
            SimpleBooleanProperty uNameNotOk = new SimpleBooleanProperty(true);
            username.textProperty().addListener((ov, oldV, newV) -> {
                uNameNotOk.set(newV.trim().isEmpty());
            });

            TextField email = new TextField();
            email.setFocusTraversable(true);
            Label emailLabel = new Label("Email:");
            emailLabel.setLabelFor(email);
            SimpleBooleanProperty emailNotOk = new SimpleBooleanProperty(true);
            email.textProperty().addListener((ov, oldV, newV) -> {
                emailNotOk.set(newV.trim().isEmpty());
            });

            CheckBox showPassword = new CheckBox("Unmask");
            showPassword.setFocusTraversable(true);
            showPassword.setSelected(false);

            PasswordField password = new PasswordField();
            password.setFocusTraversable(true);
            password.setSkin(new PasswordFieldSkin(password, showPassword));
            Label passwordLabel = new Label("Password:");
            usernameLabel.setLabelFor(passwordLabel);

            PasswordField confirmPassword = new PasswordField();
            confirmPassword.setFocusTraversable(true);
            confirmPassword.setSkin(new PasswordFieldSkin(confirmPassword, showPassword));
            Label confirmPasswordLabel = new Label("Confirm:");
            confirmPasswordLabel.setLabelFor(confirmPassword);

            showPassword.selectedProperty().addListener((observable, oldValue, newValue) -> {
                password.setText(password.getText());
                confirmPassword.setText(confirmPassword.getText());
            });

            SimpleBooleanProperty passwordNotOk = new SimpleBooleanProperty();
            passwordNotOk.bind(password.textProperty().isNotEqualTo(confirmPassword.textProperty()));

            Label statusLabel = new Label("Passwords do not match");
            statusLabel.visibleProperty().bind(passwordNotOk);
            statusLabel.setTextFill(Color.RED);

            regNotOk.bind(this.selectedProperty().and(fNameNotOk.
                    or(lNameNotOk).
                    or(emailNotOk).
                    or(uNameNotOk).
                    or(password.textProperty().isEmpty()).
                    or(confirmPassword.textProperty().isEmpty()).
                    or(passwordNotOk)));

            root.setPadding(new Insets(10));
            root.setAlignment(Pos.CENTER);
            root.setHgap(10);
            root.setVgap(10);
            root.add(firstnameLabel, 0, 0);
            root.add(firstname, 1, 0);
            root.add(lastnameLabel, 0, 1);
            root.add(lastname, 1, 1);
            root.add(usernameLabel, 0, 2);
            root.add(username, 1, 2);
            root.add(emailLabel, 0, 3);
            root.add(email, 1, 3);
            root.add(passwordLabel, 0, 4);
            root.add(password, 1, 4);
            root.add(confirmPasswordLabel, 0, 5);
            root.add(confirmPassword, 1, 5);
            root.add(showPassword, 0, 6);
            root.add(statusLabel, 1, 6);
            this.setContent(root);
        }
    }
}
