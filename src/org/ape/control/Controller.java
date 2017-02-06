package org.ape.control;

import com.sun.istack.internal.NotNull;
import org.ape.annotations.treeObjects.TreeObject;
import org.ape.layout.dialogs.ConfirmationAlert;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.ape.AppData;
import org.ape.layout.dialogs.FixedExceptionDialog;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.TwoDimensional;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public final class Controller {

    private static final String P_FONT_FAMILY = "font-family";
    private static final String P_FONT_SIZE = "font-size";
    private static final String P_WORD_WRAP = "text-wrap";
    private static final String P_AUTO_SELECT = "auto-select";
    private static final String P_DELIMS = "delimiters";
    private static final String P_TREE_POS = "treeObjects-pos";
    private static final String P_LOG_POS = "log-pos";
    private static final String P_START_FILE = "start-file";
    private static final String P_PROMPT_EXIT = "prompt-exit";
    private static final String P_PROMPT_DEL = "prompt-delete";
    private static final String P_WINDOW_WIDTH = "window-width";
    private static final String P_WINDOW_HEIGHT = "window-height";

    private static final String D_FONT_FAMILY = "Monospaced";
    private static final String D_FONT_SIZE = "14";
    private static final String D_WORD_WRAP = "false";
    private static final String D_AUTO_SELECT = "always";
    private static final String D_DELIMS = "\\p{Punct}|\\s";
    private static final String D_TREE_POS = "right";
    private static final String D_LOG_POS = "tab";
    private static final String D_PROMP_EXIT = "true";
    private static final String D_PROMP_DEL = "true";
    private static final String D_WINDOW_WIDTH = "800";
    private static final String D_WINDOW_HEIGHT = "600";
    private static final String D_EMPTY = "";

    private static final String WARNING = "This file should not be edited manually!";

    private static final String CREATE_TASK_NAME = "Creating project";
    private static final String LOAD_TASK_NAME = "Loading project";
    private static final String SAVE_TASK_NAME = "Saving project";
    private static final String EXPORT_PROJ_TASK_NAME = "Exporting project";
    private static final String EXPORT_LOG_TASK_NAME = "Exporting log";
    private static final String SETTINGS_TASK_NAME = "Saving settings";

    private static final Properties properties = new Properties();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final SimpleObjectProperty<Pattern> delimPattern = new SimpleObjectProperty<>();

    private static final String CONF_FNAME = System.getProperty("user.home") + File.separator + ".apeconfig";
    private static final String STYLE_FORMAT = "-fx-font-family: %s; -fx-font-size: %s;";

    private static final double MIN_INIT_WINDOW_WIDTH = 300;
    private static final double MIN_INIT_WINDOW_HEIGHT = 200;

    static int READ_BUF_SIZE = 4096;
    static int WRITE_BUF_SIZE = 4096;

    private static int skipNBytes = -1;
    private static HostServices hostServices;

    public static TaskMonitor taskMonitor = new TaskMonitor();
    public static SimpleLogger logger = new SimpleLogger();

    public static void init(HostServices hostServices) {
        Controller.hostServices = hostServices;
        Controller.setDefaultProperties();
        try (FileInputStream fis = new FileInputStream(CONF_FNAME)) {
            properties.load(fis);
            int fontSize = Integer.valueOf(properties.getProperty(P_FONT_SIZE));
            if (fontSize < AppData.MIN_FONT_SIZE || fontSize > AppData.MAX_FONT_SIZE) {
                properties.putIfAbsent(P_FONT_SIZE, D_FONT_SIZE);
            }
            Controller.delimPattern.set(Pattern.compile(properties.getProperty(P_DELIMS), Pattern.UNICODE_CHARACTER_CLASS));
        } catch (IOException | PatternSyntaxException e) {
            Controller.delimPattern.set(Pattern.compile(D_DELIMS, Pattern.UNICODE_CHARACTER_CLASS));
        }

        Controller.initTextArea();
        final String pathName = Controller.getStartFilePath();
        if (!pathName.isEmpty()) {
            Controller.executor.submit(new ReadProjectTask(new File(pathName), LOAD_TASK_NAME));
        }
    }

    static Properties getProperties(){
        return Controller.properties;
    }

    private static void initTextArea() {
        AppData.textArea.setEditable(false);
        AppData.textArea.setUseInitialStyleForInsertion(true);
        AppData.textArea.disableProperty().bind(AppData.isActiveProject.not());
        AppData.textArea.setParagraphGraphicFactory(LineNumberFactory.get(AppData.textArea));
        AppData.textArea.showCaretProperty().bind(Bindings
                .when(AppData.isActiveProject)
                .then(StyledTextArea.CaretVisibility.ON)
                .otherwise(StyledTextArea.CaretVisibility.OFF)
        );
        Controller.updateStyleTextArea();
    }

    private static void setDefaultProperties() {
        properties.setProperty(P_FONT_FAMILY, D_FONT_FAMILY);
        properties.setProperty(P_FONT_SIZE, D_FONT_SIZE);
        properties.setProperty(P_WORD_WRAP, D_WORD_WRAP);
        properties.setProperty(P_AUTO_SELECT, D_AUTO_SELECT);
        properties.setProperty(P_DELIMS, D_DELIMS);
        properties.setProperty(P_TREE_POS, D_TREE_POS);
        properties.setProperty(P_LOG_POS, D_LOG_POS);
        properties.setProperty(P_START_FILE, D_EMPTY);
        properties.setProperty(P_PROMPT_EXIT, D_PROMP_DEL);
        properties.setProperty(P_PROMPT_DEL, D_PROMP_EXIT);
        properties.setProperty(P_WINDOW_WIDTH, D_WINDOW_WIDTH);
        properties.setProperty(P_WINDOW_HEIGHT, D_WINDOW_HEIGHT);
    }

    public static HostServices getHostServices() {
        return Controller.hostServices;
    }

    public static double getWindowWidth() {
        try {
            double res = Double.valueOf(properties.getProperty(P_WINDOW_WIDTH, D_WINDOW_WIDTH));
            if (res >= MIN_INIT_WINDOW_WIDTH) {
                return res;
            }
        } catch (NumberFormatException e) {
        }
        return MIN_INIT_WINDOW_WIDTH;
    }

    public static double getWindowHeight() {
        try {
            double res = Double.valueOf(properties.getProperty(P_WINDOW_HEIGHT, D_WINDOW_HEIGHT));
            if (res >= MIN_INIT_WINDOW_HEIGHT) {
                return res;
            }
        } catch (NumberFormatException e) {
        }
        return MIN_INIT_WINDOW_HEIGHT;
    }

    public static String getFontFamily() {
        return properties.getProperty(P_FONT_FAMILY);
    }

    public static int getFontSize() {
        return Integer.valueOf(properties.getProperty(P_FONT_SIZE, D_FONT_SIZE));
    }

    public static boolean isAutoSelectAlways() {
        return properties.getProperty(P_AUTO_SELECT, D_AUTO_SELECT).compareTo("always") == 0;
    }

    public static Pattern getDelimiterPattern() {
        return Controller.delimPattern.get();
    }

    public static void setDelimitersPattern(@NotNull Pattern pattern) {
        properties.setProperty(P_DELIMS, pattern.pattern());
        Controller.delimPattern.set(pattern);
    }

    public static boolean isWrap() {
        return Boolean.valueOf(properties.getProperty(P_WORD_WRAP, D_WORD_WRAP));
    }

    public static String getTreePos() {
        return properties.getProperty(P_TREE_POS, D_TREE_POS);
    }

    public static void setTreePos(String position) {
        properties.setProperty(P_TREE_POS, position);
    }

    public static String getLogPos() {
        return properties.getProperty(P_LOG_POS, D_LOG_POS);
    }

    public static void setLogPosProperty(String position) {
        properties.setProperty(P_LOG_POS, position);
    }

    public static String getStartFilePath() {
        return properties.getProperty(P_START_FILE, D_EMPTY);
    }

    public static boolean isPromptDel() {
        return Boolean.valueOf(properties.getProperty(P_PROMPT_DEL, D_PROMP_EXIT));
    }

    public static boolean isPromptExit() {
        return Boolean.valueOf(properties.getProperty(P_PROMPT_EXIT, D_PROMP_DEL));
    }

    private static void updateStyleTextArea() {
        Platform.runLater(() -> {
            AppData.textArea.setWrapText(Controller.isWrap());
            AppData.textArea.setStyle(String.format(
                    STYLE_FORMAT,
                    properties.getProperty(P_FONT_FAMILY),
                    properties.getProperty(P_FONT_SIZE)
            ));
            AppData.textArea.selectAll();
            AppData.textArea.deselect();
        });
    }

    private static void flushChanges() {
        executor.submit(new SaveOptionsTask(new File(Controller.CONF_FNAME), SETTINGS_TASK_NAME, WARNING));
    }

    public static boolean shouldAdjustSelection() {
        return Controller.isAutoSelectAlways() || AppData.textArea.getSelection().getLength() == 0;
    }

    public static void updateProperties(String fontFamily, Integer fontSize, Pattern pattern,
                                        String fileName, String autoSelect, boolean isWrap,
                                        boolean isPromptExit, boolean isPromptDel) {
        properties.setProperty(P_FONT_FAMILY, fontFamily);
        properties.setProperty(P_FONT_SIZE, fontSize.toString());
        properties.setProperty(P_AUTO_SELECT, autoSelect);
        properties.setProperty(P_WORD_WRAP, Boolean.toString(isWrap));
        properties.setProperty(P_PROMPT_DEL, Boolean.toString(isPromptDel));
        properties.setProperty(P_PROMPT_EXIT, Boolean.toString(isPromptExit));
        properties.setProperty(P_START_FILE, fileName);
        Controller.setDelimitersPattern(pattern);
        Controller.updateStyleTextArea();
        Controller.flushChanges();
    }

    public static void setShouldDisplayDialog(ConfirmationAlert.Type type, boolean value) {
        if (type == ConfirmationAlert.Type.TREE_DELETE) {
            properties.setProperty(P_PROMPT_DEL, Boolean.toString(value));
        } else if (type == ConfirmationAlert.Type.APP_EXIT) {
            properties.setProperty(P_PROMPT_EXIT, Boolean.toString(value));
        }
    }

    private static void internalDelete(TreeObject<?>...treeObjects) {
        for (final TreeObject<?> treeObject: treeObjects) {
            if (treeObject.getParent() != null) {
                treeObject.getParent().remove(treeObject);
                AppData.treeObjects.remove(treeObject);
            } else {
                treeObject.getChildren().clear();
                break;
            }
        }
    }

    public static void deleteTreeObject(TreeObject<?>...treeObject) {
        if (Controller.isPromptDel()) {
            new ConfirmationAlert(ConfirmationAlert.Type.TREE_DELETE)
                    .showAndWait()
                    .filter(buttonType -> buttonType.getButtonData() == ButtonBar.ButtonData.YES)
                    .ifPresent(buttonType -> Controller.internalDelete(treeObject));
        } else {
            Controller.internalDelete(treeObject);
        }
    }

    public static void adjustSelection() {
        final int anchorPos = AppData.textArea.offsetToPosition(AppData.textArea.getAnchor(), TwoDimensional.Bias.Forward).getMinor();
        final int caretPos = AppData.textArea.offsetToPosition(AppData.textArea.getCaretPosition(), TwoDimensional.Bias.Forward).getMinor();
        final int offset = AppData.textArea.getCaretPosition() - AppData.textArea.getCaretColumn();
        AppData.textArea.selectLine();
        final String line = AppData.textArea.getSelectedText();
        final Matcher matcher = Controller.delimPattern.get().matcher(line);
        int validStart = 0, validEnd;
        while (matcher.find()) {
            validEnd = matcher.start();
            if (validEnd >= caretPos) {
                AppData.textArea.selectRange(validStart + offset, validEnd + offset);
                return;
            }
            if (matcher.end() <= anchorPos) {
                validStart = matcher.end();
            }
        }
        AppData.textArea.deselect();
    }

    public static void newProject() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("New Project");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files *.txt", "*.txt"));
        final File file = fileChooser.showOpenDialog(AppData.owner);
        if (file == null) {
            return;
        }
        if (file.canRead() || file.setReadable(true)) {
            Controller.executor.submit(new NewProjectTask(file, CREATE_TASK_NAME));
        } else {
            new FixedExceptionDialog(new Throwable("Permission denied: " + file), CREATE_TASK_NAME).showAndWait();
        }
    }

    public static void saveProject() {
        if (AppData.saveFile.get() == null) {
            Controller.saveProjectAs();
        } else {
            Controller.writeProjectFile(AppData.saveFile.get(), Controller.skipNBytes);
        }
    }

    public static void saveProjectAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Project");
        final File file = fileChooser.showSaveDialog(AppData.owner);
        if (file == null) {
            return;
        }
        if (!file.exists() || file.canWrite() || file.setWritable(true)) {
            Controller.writeProjectFile(file, -1);
        } else {
            new FixedExceptionDialog(new Throwable("Permission denied: " + file), SAVE_TASK_NAME).showAndWait();
        }
    }

    private static void writeProjectFile(final File file, int skipBytes) {
        final WriteProjectTask task = new WriteProjectTask(file, SAVE_TASK_NAME, skipBytes);
        task.setOnSucceeded(wse -> {
            AppData.saveFile.set(file);
            Controller.skipNBytes = task.getValue();
            wse.consume();
        });
        Controller.executor.submit(task);
    }

    public static void openProject() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Project");
        final File file = fileChooser.showOpenDialog(AppData.owner);
        if (file == null) {
            return;
        }
        if (file.canRead() || file.setReadable(true)) {
            Controller.readProjectFile(file);
        } else {
            new FixedExceptionDialog(new Throwable("Permission denied: " + file), LOAD_TASK_NAME).showAndWait();
        }
    }

    private static void readProjectFile(final File file) {
        final ReadProjectTask task = new ReadProjectTask(file, LOAD_TASK_NAME);
        task.setOnSucceeded(wse -> {
            AppData.saveFile.set(file);
            Controller.skipNBytes = task.getValue();
            wse.consume();
        });
        Controller.executor.submit(task);
    }

    public static void exportProject(final File file, boolean onlySelected,
                                     boolean outputId, boolean outputDescription,
                                     int startLineNum) {
        final Task<Void> exportTask = new ExportProjectTask(
                file, EXPORT_PROJ_TASK_NAME, onlySelected,
                outputId, outputDescription, startLineNum);
        exportTask.setOnSucceeded(workerStateEvent -> {
            AppData.exportPath.set(file.getAbsolutePath());
            workerStateEvent.consume();
        });
        Controller.executor.submit(exportTask);
    }

    public static void exportLog() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export log");
        final File file = fileChooser.showSaveDialog(AppData.owner);
        if (file == null) {
            return;
        }
        if (!file.exists() || file.canWrite() || file.setWritable(true)) {
            executor.submit(new ExportLogTask(file, EXPORT_LOG_TASK_NAME,
                    AppData.logListView.getItems().stream()
                            .map(Object::toString)
                            .collect(Collectors.toList())));
        } else {
            new FixedExceptionDialog(new Throwable("Permission denied: " + file), EXPORT_LOG_TASK_NAME).showAndWait();
        }
    }

    static void cleanup() {
        AppData.logListView.clearLog();
        AppData.id = 0;
        AppData.treeObjects.clear();
        AppData.colorObjects.clear();
        AppData.textArea.clear();
        AppData.defaultCategory.set(null);
        AppData.anchorAnnotation.set(null);
        AppData.saveFile.set(null);
    }

    public static boolean canShutdown() {
        return !Controller.isPromptExit() ||
                new ConfirmationAlert(ConfirmationAlert.Type.APP_EXIT)
                        .showAndWait()
                        .orElse(ButtonType.NO).getButtonData() == ButtonBar.ButtonData.YES;
    }

    public static void shutdown() {
        AppData.logListView.stop();
        properties.setProperty(P_WINDOW_WIDTH, String.valueOf(AppData.owner.getScene().getWidth()));
        properties.setProperty(P_WINDOW_HEIGHT, String.valueOf(AppData.owner.getScene().getHeight()));
        Controller.flushChanges();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Platform.exit();
    }
}