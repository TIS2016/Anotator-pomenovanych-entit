import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class Controller {

    private static final String CONF_FNAME = ".ape.cfg";
    public static final String P_FONT_SIZE = "font-size";
    public static final String P_USERNAME = "username";
    public static final String P_PASSWORD = "password";
    public static final String P_HOST_NAME = "host";
    public static final String P_PORT_NUMBER = "port";
    public static final String P_PROMPT_EXIT = "show-exit-dialog";
    public static final String P_PROMPT_DEL = "show-del-dialog";
    public static final String P_AUTOCONN = "autoconnect";
    public static final String P_WORD_WRAP = "text-wrap";

    private static final String DEFAULT_EXIT = "true";
    private static final String DEFAULT_DEL = "true";
    private static final String DEFAULT_WORD_WRAP = "true";
    private static final String DEFAULT_FONT_SIZE = "14";

    private static final Set<? extends String> validProperties =
            Arrays.stream(Controller.class.getFields())
                    .filter(field -> field.getName().startsWith("P_"))
                    .map(field -> {
                        try {
                            return (String) field.get(null);
                        } catch (IllegalAccessException e) {
                            return "";
                        }
                    }).collect(Collectors.toSet());

    private static final Properties properties = new Properties();

    public static void writeConfigFile() {
        try (FileOutputStream fis = new FileOutputStream(CONF_FNAME)) {
            properties.store(fis, "This file should not be edited manually!");
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.showAndWait();
        }
    }

    public static void init() {
        readConfigFile();
        //TODO: check for autoconnect!
    }

    private static void setDefaultProperties() {
        properties.setProperty(P_FONT_SIZE, DEFAULT_FONT_SIZE);
        properties.setProperty(P_WORD_WRAP, DEFAULT_WORD_WRAP);
        properties.setProperty(P_USERNAME, "");
        properties.setProperty(P_PASSWORD, "");
        properties.setProperty(P_HOST_NAME, "");
        properties.setProperty(P_PORT_NUMBER, "");
        properties.setProperty(P_AUTOCONN, "");
        properties.setProperty(P_PROMPT_EXIT, DEFAULT_EXIT);
        properties.setProperty(P_PROMPT_DEL, DEFAULT_DEL);
    }

    private static void removeUnknownProperties() {
        for (Iterator<Object> it = properties.keySet().iterator(); it.hasNext(); ) {
            Object object = it.next();
            if (!validProperties.contains(object)) {
                it.remove();
            }
        }
    }

    public static void readConfigFile() {
        try (FileInputStream fis = new FileInputStream(CONF_FNAME)) {
            properties.load(fis);
            //Controller.removeUnknownProperties();
        } catch (IOException e) {
            Controller.setDefaultProperties();
        }
    }

    public static void setDisplayDialog(ConfirmationAlert.Type type, boolean value) {
        if (type == ConfirmationAlert.Type.TREE_DELETE) {
            properties.setProperty(P_PROMPT_DEL, Boolean.toString(value));
        } else if (type == ConfirmationAlert.Type.EXIT) {
            properties.setProperty(P_PROMPT_EXIT, Boolean.toString(value));
        }
    }

    private static void internalDelete(TreeObject<?> ...treeObjects) {
        for (TreeObject<?> treeObject: treeObjects) {
            if (treeObject.getParent() != null) {
                treeObject.getParent().getChildren().remove(treeObject);
            }
            treeObject.clearChildren();
        }
    }

    public static void deleteTreeObject(TreeObject<?> ...treeObject) {
        if (properties.getProperty(P_PROMPT_DEL, DEFAULT_DEL).compareTo(DEFAULT_DEL) == 0) {
            new ConfirmationAlert(ConfirmationAlert.Type.TREE_DELETE)
                    .showAndWait()
                    .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.YES)
                    .ifPresent(buttonType -> Controller.internalDelete(treeObject));
        } else {
            Controller.internalDelete(treeObject);
        }
    }

    public static boolean shutdown() {
        boolean result = true;
        if (properties.getProperty(P_PROMPT_EXIT, DEFAULT_EXIT).compareTo(DEFAULT_EXIT) == 0) {
            result = new ConfirmationAlert(ConfirmationAlert.Type.EXIT)
                    .showAndWait().orElse(ButtonType.NO).getButtonData() == ButtonBar.ButtonData.YES;
        }
        if (result) { //TODO: this is for testing, remove me
            writeConfigFile();
        }
        return result;
    }
}