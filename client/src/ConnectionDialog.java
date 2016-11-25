import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by michal on 11/17/16.
 */
public class ConnectionDialog extends Dialog {

    private class ConnectionPane extends GridPane {

        public ConnectionPane(ConnectionDialog conDialog) {
            DialogPane dialogPane = conDialog.getDialogPane();

            SimpleBooleanProperty invalidHost = new SimpleBooleanProperty();
            invalidHost.set(true);
            SimpleBooleanProperty invalidPort = new SimpleBooleanProperty();
            invalidPort.set(true);

            TextField hostName = new TextField();
            hostName.textProperty().addListener((observable, oldValue, newValue) -> {
                invalidHost.set(newValue.trim().isEmpty());
            });
            Platform.runLater(() -> {
                    hostName.requestFocus();
            });

            TextField portNumber = new TextField();
            portNumber.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    int res = Integer.parseInt(newValue.trim());
                    invalidPort.set(res < 0 || res > 65535);
                } catch (NumberFormatException e) {
                    invalidPort.set(true);
                }
            });

            Label hostLabel = new Label("_Host:");
            hostLabel.setMnemonicParsing(true);
            hostLabel.setLabelFor(hostName);

            Label portLabel = new Label("_Port:");
            portLabel.setMnemonicParsing(true);
            portLabel.setLabelFor(portNumber);

            Alert connectionError = new Alert(Alert.AlertType.ERROR);
            connectionError.setTitle("Connection error");

            final ButtonType connectButton = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
            final ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(cancelButton, connectButton);
            Button connectBtn = (Button) dialogPane.lookupButton(connectButton);

            connectBtn.disableProperty().bind(invalidPort.or(invalidHost));
            connectBtn.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    Socket socket = new Socket(hostName.getText().trim(),
                            Integer.parseInt(portNumber.getText().trim()));
                    LoginDialog loginDialog = new LoginDialog(conDialog.getOwner());
                    boolean ok = loginDialog.showAndWait().orElse(false); //uncomment this
                    if (!ok) { //consume this event if we pressed Cancel in loginDialog
                        event.consume();
                    }
                } catch (IOException e) {
                    connectionError.setHeaderText("CAN'T CREATE SOCKET,");
                    connectionError.setContentText("see source file, TODO: handle me");
                    connectionError.showAndWait();
                    event.consume();
                }
            });

            this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(hostLabel, 0, 0);
            this.add(hostName, 1, 0);
            this.add(portLabel, 0, 1);
            this.add(portNumber, 1, 1);
        }
    }

    public ConnectionDialog(Window owner) {
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("New connection");
        this.getDialogPane().setContent(new ConnectionPane(this));
    }
}
