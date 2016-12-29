import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class ProjectSearchDialog extends Dialog {

	public ProjectSearchDialog(Window owner){
		super();
		this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Search Projects");
	    this.getDialogPane().setContent(new JoinPane(owner));
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().setAll(cancelBtn);
	}

	private class JoinPane extends GridPane {

		public JoinPane(Window owner) {
			super();

            final TextField listFilter = new TextField();
            listFilter.setPromptText("Search");
            Platform.runLater(() -> listFilter.requestFocus());

            FilteredList<ProjectData> filteredProject = new FilteredList<>(SessionData.projects);
            filteredProject.predicateProperty().bind(Bindings.createObjectBinding(
                    () -> listFilter.getText().trim().isEmpty() ? null :
                            projectData -> projectData.getProjName().contains(listFilter.getText().trim()),
                    listFilter.textProperty()));

			ListView<ProjectData> projectListView = new ListView<>(filteredProject);

	        projectListView.setCellFactory(lv -> {
	            ListCell<ProjectData> cell = new ListCell<>();
	            ContextMenu contextMenu = new ContextMenu();

                MenuItem joinMenuItem = new MenuItem("Join");
	            joinMenuItem.setOnAction(actionEvent -> {
                    ProjectData projData = cell.getItem();
					if (projData.hasPasswd()) {
                        new JoinPassDialog(owner, projData).showAndWait();
                    }
                    System.out.println("TODO: bind join");
	                actionEvent.consume();
	            });

                joinMenuItem.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                    ProjectData projData = cell.getItem();
                    return projData == null ||
                            (projData.getDefPriv() == 0 && !projData.getListedUsers().contains(SessionData.userData));
                }, cell.itemProperty()));


	            MenuItem infoMenuItem = new MenuItem("Info - NYI");
	            infoMenuItem.setOnAction(actionEvent -> {
                    System.out.println("TODO: bind info");
					actionEvent.consume();
	            });

                MenuItem updateMenuItem = new MenuItem("Update");
                updateMenuItem.setOnAction(actionEvent -> {
                    new ProjectDialog(owner, cell.getItem()).showAndWait();
                    actionEvent.consume();
                });
                updateMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
                    ProjectData projData = cell.getItem();
                    return projData != null && projData.getOwner().getId() == SessionData.userData.getId();
                }, cell.itemProperty()));

	            MenuItem deleteMenuItem = new MenuItem("Delete");
	            deleteMenuItem.setOnAction(actionEvent -> {
					((FilteredList<ProjectData>) projectListView.getItems()).getSource().remove(cell.getItem());
					actionEvent.consume();
				});
                deleteMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
                    ProjectData projData = cell.getItem();
                    return projData != null && projData.getOwner().getId() == SessionData.userData.getId();
                }, cell.itemProperty()));

	            contextMenu.getItems().setAll(joinMenuItem, new SeparatorMenuItem(), infoMenuItem, updateMenuItem, deleteMenuItem);

	            cell.textProperty().bind(Bindings.createObjectBinding(
	                    () -> cell.getItem() == null ? null :
                                cell.getItem().getProjName(), cell.itemProperty()));

				cell.contextMenuProperty().bind(Bindings
						.when(cell.emptyProperty())
						.then((ContextMenu) null)
						.otherwise(contextMenu));
	            return cell ;
	        });

			this.setPadding(new Insets(10));
			this.setHgap(10);
			this.setVgap(10);
            this.add(listFilter, 0, 0);
	        this.add(projectListView, 0, 1);
		}
	}

	private class JoinPassDialog extends Dialog {

		public JoinPassDialog(Window owner, ProjectData data) {
			super();
			this.initOwner(owner);
			this.setResizable(false);
			this.setTitle("Join " + data.getProjName());

			DialogPane dialogPane = this.getDialogPane();
			GridPane root = new GridPane();

			CheckBox showPassword = new CheckBox("Unmask");
            
			PasswordField password = new PasswordField();
			password.setSkin(new PasswordFieldSkin(password, showPassword));
			showPassword.selectedProperty().addListener(
					(observable, oldValue, newValue) -> password.setText(password.getText()));

			ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
			ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
			dialogPane.getButtonTypes().setAll(okButton, cancelBtn);

			root.setPadding(new Insets(10));
			root.setHgap(10);
			root.setVgap(10);

			root.add(new Label("Password: "), 0, 0);
			root.add(password, 1, 0);
			root.add(showPassword, 1, 1);
			dialogPane.setContent(root);

			Button okBtn = (Button) dialogPane.lookupButton(okButton);
			okBtn.disableProperty().bind(password.textProperty().isEmpty());
			okBtn.addEventFilter(ActionEvent.ACTION, event -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("TODO: send data to server...");
                alert.showAndWait();
                event.consume();
            });
		}
	}
}