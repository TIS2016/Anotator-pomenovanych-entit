import java.io.File;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.fxmisc.wellbehaved.event.Nodes;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;


public class ProjectDialog extends Dialog {

	public ProjectDialog(Window owner){
        super();
        this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Create New Project");
	    this.getDialogPane().setContent(new CreatePane(this, null));
	}

    public ProjectDialog(Window owner, ProjectData projectData){
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("Create New Project");
        this.getDialogPane().setContent(new CreatePane(this, projectData));
    }

	private class CreatePane extends VBox {

        private String trimExtension(final String fileName) {
            int extensionIndex = fileName.lastIndexOf(".");
            return extensionIndex == -1 ? fileName : fileName.substring(0, extensionIndex);
        }

		public CreatePane(ProjectDialog projectDialog,
                          final ProjectData projectData) {
            super();
            DialogPane crtPane = projectDialog.getDialogPane();

            boolean cantEdit = projectData != null;

			GridPane grid = new GridPane();
            VBox userTable = new TableViewBox(projectDialog, projectData);

            final SimpleBooleanProperty invalidProjectName = new SimpleBooleanProperty(true);
	        final SimpleBooleanProperty invalidDocName = new SimpleBooleanProperty(true);
	        final SimpleBooleanProperty fileNotChosen = new SimpleBooleanProperty(true);

			final TextField projectName = new TextField();
			projectName.textProperty().addListener(
			        (ov, oldV, newV) -> invalidProjectName.set(newV.trim().isEmpty()));
            projectName.setDisable(cantEdit);

            final TextField docName = new TextField();
            docName.textProperty().addListener(
                    (observable, oldValue, newValue) -> invalidDocName.set(newValue.trim().isEmpty()));
            docName.setDisable(cantEdit);

			final PasswordField password = new PasswordField();
            final CheckBox showPassword = new CheckBox("Unmask");

            password.setSkin(new PasswordFieldSkin(password, showPassword));
            showPassword.selectedProperty().addListener(
                    (observable, oldValue, newValue) -> password.setText(password.getText()));

			ComboBox<ProjectData> filesBox = new ComboBox<>(SessionData.projects);
            filesBox.setDisable(cantEdit);
            filesBox.setConverter(new StringConverter<ProjectData>() {

                @Override
                public String toString(ProjectData object) {
                    return object.getDocName();
                }

                @Override
                public ProjectData fromString(String string) {
                    return null;
                }
            });

            SimpleObjectProperty<File> fileProperty = new SimpleObjectProperty<>();
            fileProperty.addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    filesBox.getSelectionModel().select(null);
                    docName.textProperty().unbind();
                    docName.setText(this.trimExtension(newValue.getName().trim()));
                    docName.setDisable(false);
                }
            }));

            filesBox.getSelectionModel().selectedItemProperty()
                    .addListener(((observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            fileProperty.set(null);
                            //if project admin can change name of the doc
                            //otherwise rewrite it using String + getter/setter
                            docName.textProperty().set(newValue.getDocName());
                            docName.setDisable(true);
                        }
                    }));
            filesBox.getSelectionModel().select(projectData);

            fileNotChosen.bind(fileProperty.isNull()
                    .and(filesBox.getSelectionModel().selectedItemProperty().isNull()));

			final Button openButton = new Button("Upload File");
            openButton.setDisable(cantEdit);

			fileProperty.addListener((
			        (observable, oldValue, newValue) ->
                            openButton.setText(newValue != null ? newValue.getName() : "Upload File")));

            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            fileChooser.getExtensionFilters().addAll(
						new FileChooser.ExtensionFilter("Text Files *.txt", "*.txt")
            );
			openButton.setOnMouseClicked(actionEvent -> {
				File file = fileChooser.showOpenDialog(((Node) actionEvent.getTarget()).getScene().getWindow());
				if (file != null && file.canRead()) {
                    fileProperty.set(file);
				}
				actionEvent.consume();
			});

            final PrivilegesBox defaultPrivileges = new PrivilegesBox();
            defaultPrivileges.getCheckModel().clearChecks();

			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    crtPane.getButtonTypes().setAll(okButton, cancelBtn);
		    Button okBtn = (Button) crtPane.lookupButton(okButton);
            okBtn.disableProperty().bind(invalidProjectName
                    .or(invalidDocName)
                    .or(fileNotChosen));

		    grid.setPadding(new Insets(10, 0, 10, 0));
            grid.setHgap(10);
            grid.setVgap(10);
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setHalignment(HPos.LEFT);
            c1.setPercentWidth(50); //40
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setHalignment(HPos.LEFT);
            c2.setPercentWidth(50); //60
            grid.getColumnConstraints().addAll(c1, c2);

            grid.add(new Label("Project name: "), 0, 0);
            grid.add(projectName, 1, 0);
			grid.add(new Label("Project password: "), 0, 1);
			grid.add(password, 1, 1);
			grid.add(showPassword, 1, 2);
            grid.add(new Label("Default privileges: "), 0, 3);
            grid.add(defaultPrivileges, 1, 3);
            grid.add(new Label("Existing document: "), 0, 4);
			grid.add(filesBox, 1, 4);
            grid.add(new Label("Upload document: "), 0, 5);
            grid.add(openButton, 1, 5);
            grid.add(new Label("Document name: "), 0, 6);
            grid.add(docName, 1, 6);

            this.getChildren().addAll(grid, userTable);
		}
	}

	private class PrivilegesBox extends CheckComboBox<String> {

        public PrivilegesBox() {
            super();
            this.getItems().setAll("reannot", "annot", "edit");
        }

        public String toSimpleString() {
            ObservableList<String> checkedItems = this.getCheckModel().getCheckedItems();
            return this.getItems()
                    .stream()
                    .map(item -> checkedItems.contains(item) ? item.substring(0, 1) : "-")
                    .reduce("", String::concat);
        }
    }

	private class TableViewBox extends VBox {

		public TableViewBox(ProjectDialog projectDialog, ProjectData projectData) {
            super();

            //TODO: this should be initialized from server when opening this dialog!!!
            FilteredList<String> filteredNames = new FilteredList<>(SessionData.userNames);

            SessionData.userNames.clear();
            SessionData.userNames.addAll("foo", "bar", "baz");

            final ComboBox<String> userNamesBox = new ComboBox<>(filteredNames);
            userNamesBox.getSelectionModel().selectFirst();

            final ListChangeListener<? super String> listener =  c -> {
                if (userNamesBox.getValue() == null) {
                    userNamesBox.getSelectionModel().selectFirst();
                }
            };

            SessionData.userNames.addListener(listener);
            projectDialog.setOnHiding(dialogEvent -> {
                SessionData.userNames.removeListener(listener);
                dialogEvent.consume();
            });

            ObservableList<BaseUserData> users_ = projectData == null ? FXCollections.observableArrayList() :
                    FXCollections.observableArrayList(projectData.getListedUsers());

            final TableView<BaseUserData> userTable = new TableView<>(users_);
            userTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			userTable.setEditable(true);
            userTable.setPrefHeight(200);

            KeyCodeCombination selectAllCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
            Nodes.addInputMap(userTable, consume(keyPressed(selectAllCombination),
                    keyEvent -> userTable.getSelectionModel().selectAll()));

            userTable.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.DELETE) {
                    userTable.getItems().removeAll(userTable.getSelectionModel().getSelectedItems());
                    userTable.getSelectionModel().clearSelection();
                    userTable.refresh();
                }
                keyEvent.consume();
            });

			TableColumn<BaseUserData, String> nameColumn = new TableColumn<>("Username");
			nameColumn.setPrefWidth(100);
			nameColumn.setStyle("-fx-alignment: center;");
            nameColumn.setCellFactory(t -> new ComboCell(SessionData.userNames));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
			nameColumn.setOnEditCommit(t -> {
                t.getRowValue().setUserName(t.getNewValue());
                t.consume();
			});
            nameColumn.setResizable(false);
            nameColumn.prefWidthProperty().bind(userTable.widthProperty().divide(2));

            TableColumn<BaseUserData, String> privilegeColumn = new TableColumn<>("Privileges");
            privilegeColumn.setStyle("-fx-alignment: center;");
            privilegeColumn.setCellFactory(tc -> new PrivilegeCell());
            privilegeColumn.setCellValueFactory(new PropertyValueFactory<>("privileges"));
            privilegeColumn.setOnEditCommit(t -> {
                t.getRowValue().setPrivileges(t.getNewValue());
                t.consume();
            });
            privilegeColumn.setResizable(false);
            privilegeColumn.prefWidthProperty().bind(userTable.widthProperty().divide(2));

            userTable.setRowFactory(tableView -> {
                final TableRow<BaseUserData> tableRow = new TableRow<>();
                tableRow.setTextAlignment(TextAlignment.CENTER);
                final ContextMenu contextMenu = new ContextMenu();

                final MenuItem deleteMenuItem = new MenuItem("Delete");
                deleteMenuItem.setOnAction(actionEvent -> {
                    tableRow.cancelEdit();
                    tableView.getItems().remove(tableRow.getItem());
                    tableView.refresh();
                    actionEvent.consume();
                });

                contextMenu.getItems().setAll(deleteMenuItem);
                // Set context menu on tableRow, but use a binding to make it only show for non-empty tableRows:
                tableRow.contextMenuProperty().bind(
                        Bindings.when(tableRow.emptyProperty())
                                .then((ContextMenu) null)
                                .otherwise(contextMenu)
                );
                return tableRow;
            });
            
            userTable.getColumns().setAll(nameColumn, privilegeColumn);

            final PrivilegesBox privilegesBox = new PrivilegesBox();

            SimpleStringProperty privileges = new SimpleStringProperty();

            ObservableList<String> checkedItems = privilegesBox.getCheckModel().getCheckedItems();
            checkedItems.addListener(new ListChangeListener<String>() {

                @Override
                public void onChanged(Change<? extends String> c) {
                    privileges.set(privilegesBox.toSimpleString());
                }
            });
            privilegesBox.getCheckModel().checkIndices(0, 1); //reannot, annot

			final Button addUserButton = new Button("Add User");
            addUserButton.setPrefWidth(80);
            addUserButton.disableProperty().bind(userNamesBox.valueProperty().isNull());

            addUserButton.setOnAction(actionEvent -> {
                users_.add(new BaseUserData(0, userNamesBox.getValue(), privileges.get())); //TODO: find id by name...
                userTable.refresh();
                userTable.scrollTo(userTable.getItems().size() - 1);
                actionEvent.consume();
            });

            HBox addUserBox = new HBox(userNamesBox, privilegesBox);
            addUserBox.setSpacing(10);

            this.setSpacing(10);
            this.getChildren().setAll(userTable, addUserBox, addUserButton);
		}
	}

	private class ComboCell extends ComboBoxTableCell<BaseUserData, String> {

        private ObservableList<String> userNames;

        public ComboCell(ObservableList<String> userNames) {
            super(userNames);
            this.userNames = userNames;
        }

        @Override
        public void updateItem(String item, boolean empty) {
            String previous = this.getItem();
            super.updateItem(item, empty);
            if (previous == item) {
                return;
            }
            /*if ((previous ==  null && item == null) ||
                (previous != null && item != null && previous.compareTo(item) == 0)) {
                return;
            }*/
            if (previous != null && !this.userNames.contains(previous)) {
                this.userNames.add(previous);
            }
            if (item != null) {
                this.userNames.remove(item);
            }
        }
    }
}