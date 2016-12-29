import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PasswordFieldSkin extends TextFieldSkin {

    private SimpleBooleanProperty isSkinSet = new SimpleBooleanProperty();
    public static final char BULLET = '\u2022';

    public PasswordFieldSkin(TextField textField, CheckBox noSkinSet) {
        super(textField);
        isSkinSet.bind(noSkinSet.selectedProperty().not());
    }

    @Override
    protected String maskText(String txt) {
        TextField textField = this.getSkinnable();
        if (isSkinSet != null && this.isSkinSet.get() && textField instanceof PasswordField) {
            int n = textField.getLength();
            StringBuilder pwBuilder = new StringBuilder(n);
            for (int i = 0; i < n; i++)
                pwBuilder.append(BULLET);
            return pwBuilder.toString();
        }
        return txt;
    }
}
