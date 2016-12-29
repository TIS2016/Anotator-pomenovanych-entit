import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;

/**
 * Created by michal on 12/3/16.
 */
public class PrivilegeCell extends TableCell<BaseUserData, String> {

    private CheckComboBox<String> checkComboBox = new CheckComboBox<>();

    public PrivilegeCell() {
        checkComboBox.getItems().addAll("reannot", "annot", "edit");
        IndexedCheckModel<String> model = checkComboBox.getCheckModel();

        this.setGraphic(checkComboBox);
        this.setContentDisplay(ContentDisplay.TEXT_ONLY);
        this.contentDisplayProperty().bind(Bindings.createObjectBinding(
                () -> this.isEditing() ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.TEXT_ONLY,
                this.editingProperty()));
        this.checkComboBox.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                this.commitEdit(this.getString());
            }
            keyEvent.consume();
        });
        this.itemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                for (int i = 0, length = newValue.length(); i < length; i++) {
                    if (newValue.charAt(i) != '-') {
                        model.check(i);
                    }
                }
            }
        }));
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        this.setText(this.getString());
    }

    @Override
    public void commitEdit(String item) {
        if (!isEditing() && !item.equals(getItem())) {
            TableView<BaseUserData> table = getTableView();
            if (table != null) {
                TableColumn<BaseUserData, String> column = this.getTableColumn();
                TableColumn.CellEditEvent<BaseUserData, String> event = new TableColumn.CellEditEvent<>(table,
                        new TablePosition<>(table, getIndex(), column),
                        TableColumn.editCommitEvent(), item);
                Event.fireEvent(column, event);
            }
        }
        super.commitEdit(item);
    }

    private String getString() {
        IndexedCheckModel<String> checkModel = checkComboBox.getCheckModel();
        return checkComboBox.getItems()
                .stream()
                .map(s -> !checkModel.isChecked(s) ? "-": String.valueOf(s.charAt(0)).toLowerCase())
                .reduce("", String::concat);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            this.setText(this.getString());
        }
    }
}