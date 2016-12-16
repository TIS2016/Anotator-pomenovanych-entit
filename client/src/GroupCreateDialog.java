
import java.io.File;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;



public class GroupCreateDialog extends Dialog {
	final ObservableList<Person> data = FXCollections.observableArrayList(
			new Person("default", "1", "1","1")
	);
	public GroupCreateDialog(Window owner){
		this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Create New Project");
	    this.getDialogPane().setContent(new CreatePane(this));
		
	}
	private class CreatePane extends VBox{

		public CreatePane(GroupCreateDialog groupCreateDialog) {
			DialogPane crtPane = groupCreateDialog.getDialogPane();

			GridPane grid = new GridPane();

			SimpleBooleanProperty invalidProjName = new SimpleBooleanProperty(true);
	        SimpleBooleanProperty invalidDoc = new SimpleBooleanProperty(true);
			SimpleBooleanProperty invalidDocName = new SimpleBooleanProperty(true);
	        SimpleBooleanProperty existingFileChosen = new SimpleBooleanProperty();
			
			final TextField projectName = new TextField("");
			projectName.textProperty().addListener((ov, oldV, newV) -> {
               invalidProjName.set(newV.trim().isEmpty());
			});


			final TextField  password= new TextField("");

			ComboBox filesBox = new ComboBox(FXCollections.observableArrayList("New File","Existing File Name"));
			filesBox.getSelectionModel().selectFirst();
			//categoryBox.getItems().addAll("...");  Add items to the list.

			final Button openButton = new Button("Upload File");

			final TextField docName = new TextField("");
			docName.textProperty().addListener((ov, oldV, newV) -> {
				invalidDocName.set(newV.trim().isEmpty());
			});

			Path path = new Path();
			File selectedFile;
			filesBox.valueProperty().addListener(new ChangeListener<String>() {
				@Override public void changed(ObservableValue ov, String t, String t1) {
					if (!t1.equals("New File"))
						docName.setText(t1);
					else
						if(path.getFile()!=null)

							docName.setText(path.getFile().getName().substring(0,path.getFile().getName().length()-4));
						else
							docName.clear();
					existingFileChosen.set(!t1.equals("New File"));
					/*System.out.println(ov);
					System.out.println(t);
					System.out.println(t1);*/
				}
			});



			openButton.disableProperty().bind(existingFileChosen);
			openButton.setOnMouseClicked(e->{
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Resource File");
				fileChooser.getExtensionFilters().addAll(
						new FileChooser.ExtensionFilter("Text Files *.txt", "*.txt")
				);
				File file = fileChooser.showOpenDialog(((Node)e.getTarget()).getScene().getWindow());
				if (file.exists()) {
					invalidDoc.set(!file.exists());
					path.setPath(file.getPath());
					path.setFile(file);
					openButton.setText(file.getName());
					docName.setText(file.getName().substring(0,file.getName().length()-4));
				}
			});

			VBox table = new TableViewBox();
			
			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    crtPane.getButtonTypes().setAll(okButton, cancelBtn);
		    Button okBtn = (Button) crtPane.lookupButton(okButton);
		    okBtn.disableProperty().bind(invalidProjName.or(invalidDocName).or(invalidDoc.and(existingFileChosen.not())));
		    okBtn.addEventFilter(ActionEvent.ACTION, event -> {
		    	
		    	
		    	
		    	
		    	
            });
			
			
			
		    grid.setPadding(new Insets(10));
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label("Project Name: "), 0, 0);
            grid.add(projectName, 1, 0);
			grid.add(new Label("Project Password: "), 0, 1);
			grid.add(password, 1, 1);
			grid.add(new Label("Choose an existing Document: "),0,2);
			grid.add(filesBox, 1, 2);
            grid.add(new Label("Upload Document: "),0,3);
            grid.add(openButton, 1, 3);
            //this.add(new Label("Set User Privileges: "), 0, 3);
            //this.add(privileges, 1, 2);
            grid.add(new Label("Document Name: "), 0, 4);
            grid.add(docName, 1, 4);



            this.setSpacing(5);
            this.getChildren().addAll(grid,table);
		}


	}

	private class TableViewBox extends VBox{
		public TableViewBox(){
			TableView<Person> table = new TableView();
			table.setEditable(true);

			TableColumn uName = new TableColumn("Username");
			uName.setMinWidth(100);
			uName.setStyle( "-fx-alignment: CENTER;");
			uName.setCellValueFactory(
					new PropertyValueFactory<Person, String>("name"));

			TableColumn annot = new TableColumn("Annotation");
			annot.setMinWidth(100);
			annot.setStyle( "-fx-alignment: CENTER;");
			annot.setCellValueFactory(
					new PropertyValueFactory<Person, String>("annotation"));

			TableColumn reannot = new TableColumn("Reannotation");
			TableColumn edit = new TableColumn("Editation");
			reannot.setMinWidth(100);
			reannot.setStyle( "-fx-alignment: CENTER;");
			reannot.setCellValueFactory(
					new PropertyValueFactory<Person, String>("reannotation"));

			table.setItems(data);
			table.getColumns().addAll(uName, annot, reannot, edit);
			edit.setMinWidth(100);
			edit.setStyle( "-fx-alignment: CENTER;");
			edit.setCellValueFactory(
					new PropertyValueFactory<Person, String>("editation"));

			table.setRowFactory(new Callback<TableView<Person>, TableRow<Person>>() {
				@Override
				public TableRow<Person> call(TableView<Person> tableView) {
					final TableRow<Person> row = new TableRow<>();
					final ContextMenu contextMenu = new ContextMenu();
					final MenuItem removeMenuItem = new MenuItem("Remove");
					removeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							if (!row.getItem().getName().equals("default"))
								table.getItems().remove(row.getItem());
						}
					});
					contextMenu.getItems().add(removeMenuItem);
					// Set context menu on row, but use a binding to make it only show for non-empty rows:
					row.contextMenuProperty().bind(
							Bindings.when(row.emptyProperty())
									.then((ContextMenu)null)
									.otherwise(contextMenu)
					);
					return row ;
				}
			});

			uName.setCellFactory(TextFieldTableCell.forTableColumn());
			uName.setOnEditCommit(
					new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
						@Override
						public void handle(TableColumn.CellEditEvent<Person, String> t) {
							if (!t.getOldValue().equals("default"))
							((Person) t.getTableView().getItems().get(
									t.getTablePosition().getRow())
							).setName(t.getNewValue());
							else{
								((Person) t.getTableView().getItems().get(
										t.getTablePosition().getRow())
								).setName(t.getOldValue());
							}

							table.refresh();
						}
					}
			);

			annot.setCellFactory(TextFieldTableCell.forTableColumn());
			annot.setOnEditCommit(
					new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
						@Override
						public void handle(TableColumn.CellEditEvent<Person, String> t) {
							if (checkInput(t.getNewValue())) {
								((Person) t.getTableView().getItems().get(
										t.getTablePosition().getRow())
								).setAnnotation((t.getNewValue()));
							}else {
								((Person) t.getTableView().getItems().get(
										t.getTablePosition().getRow())
								).setAnnotation(t.getOldValue());
							}
							table.refresh();
						}
					}
			);

			reannot.setCellFactory(TextFieldTableCell.forTableColumn());
			reannot.setOnEditCommit(
					new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
						@Override
						public void handle(TableColumn.CellEditEvent<Person, String> t) {
							if (checkInput(t.getNewValue())) {
								((Person) t.getTableView().getItems().get(
										t.getTablePosition().getRow())
								).setReannotation((t.getNewValue()));
							}else {
								((Person) t.getTableView().getItems().get(
										t.getTablePosition().getRow())
								).setReannotation(t.getOldValue());
							}
							table.refresh();
						}
					}
			);

			edit.setCellFactory(TextFieldTableCell.forTableColumn());
			edit.setOnEditCommit(
					new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
						@Override
						public void handle(TableColumn.CellEditEvent<Person, String> t) {
							if (checkInput(t.getNewValue())) {
								((Person) t.getTableView().getItems().get(
										t.getTablePosition().getRow())
								).setEditation((t.getNewValue()));
							}else {
								((Person) t.getTableView().getItems().get(
										t.getTablePosition().getRow())
								).setEditation(t.getOldValue());
							}
							table.refresh();
						}
					}
			);

			final TextField addName = new TextField();
			addName.setPromptText("Username");
			addName.setMaxWidth(uName.getPrefWidth());
			SimpleBooleanProperty nameNotOk = new SimpleBooleanProperty(true);
			addName.textProperty().addListener((ov, oldV, newV) -> {
				nameNotOk.set(newV.trim().isEmpty());
			});

			final TextField addAnnot = new TextField();
			addAnnot.setMaxWidth(annot.getPrefWidth());
			addAnnot.setPromptText("Annotation");
			SimpleBooleanProperty annotNotOk = new SimpleBooleanProperty(true);
			addAnnot.textProperty().addListener((ov, oldV, newV) -> {
				annotNotOk.set(!checkInput(newV.trim()));
			});

			final TextField addReannot = new TextField();
			addReannot.setMaxWidth(reannot.getPrefWidth());
			addReannot.setPromptText("Reannotation");
			SimpleBooleanProperty reannotNotOk = new SimpleBooleanProperty(true);
			addReannot.textProperty().addListener((ov, oldV, newV) -> {
				reannotNotOk.set(!checkInput(newV.trim()));
			});

			final TextField addEditation = new TextField();
			addEditation.setMaxWidth(reannot.getPrefWidth());
			addEditation.setPromptText("Editation");
			SimpleBooleanProperty editNotOk = new SimpleBooleanProperty(true);
			addEditation.textProperty().addListener((ov, oldV, newV) -> {
				editNotOk.set(!checkInput(newV.trim()));
			});

			final Button addButton = new Button("Add");
			addButton.disableProperty().bind(editNotOk.or(annotNotOk.or(reannotNotOk.or(nameNotOk))));
			addButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					Person u = new Person(
							addName.getText().trim(),
							(addAnnot.getText()),
							(addReannot.getText()),
							(addEditation.getText()));
					boolean isIn = false;
					for (Person person: data){
						if (person.getName().equals(u.getName())){
							isIn = true;
							break;
						}
					}
					if (!isIn) {
						data.add(u);
					}
					addName.clear();
					addAnnot.clear();
					addReannot.clear();
					addEditation.clear();
				}
			});
			HBox hb= new HBox(10);
			hb.getChildren().addAll(addName,addAnnot,addReannot,addEditation,addButton);
			this.setSpacing(5);
			this.getChildren().addAll(new Label("Privileges : "),hb,table);

		}

	}

	private boolean checkInput(String input){
		return (input.trim().equals("0") || input.trim().equals("1"));
	}

	private class Path{
		private File file;
		private SimpleStringProperty path;
		public Path(){
			file=null;
			path=new SimpleStringProperty();
		}
		public void setPath(String p){
			path.set(p);
		}
		public String getPath(){
			return path.get();
		}
		public void setFile(File f){
			file= f;
		}
		public File getFile(){
			return file;
		}
	}

	public static class Person {


		private final SimpleStringProperty name;
		private final SimpleStringProperty annotation, reannotation, editation;


		public Person(String n, String a, String r, String e) {
			this.name = new SimpleStringProperty(n);
			this.annotation = new SimpleStringProperty(a);
			this.reannotation = new SimpleStringProperty(r);
			this.editation = new SimpleStringProperty(e);
		}

		public String getName() {
			return name.get();
		}

		public void setName(String fName) {
			name.set(fName);
		}

		public String getAnnotation() {
			return annotation.get();
		}

		public void setAnnotation(String fName) {
			annotation.set(fName);
		}

		public String getReannotation() {
			return reannotation.get();
		}

		public void setReannotation(String fName) {
			reannotation.set(fName);
		}

		public String getEditation() {
			return editation.get();
		}

		public void setEditation(String fName) {
			editation.set(fName);
		}

	}
}

