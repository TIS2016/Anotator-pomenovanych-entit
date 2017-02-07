package org.ape.layout;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import org.ape.control.SimpleLogger;
import org.ape.control.SimpleLogRecord;

import java.text.SimpleDateFormat;

public class LogListView extends ListView<SimpleLogRecord> {

    private static final int MAX_ENTRIES = 100000;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    private final ObservableList<SimpleLogRecord> logItems = FXCollections.observableArrayList();
    private final Timeline logTransfer = new Timeline();
    private final SimpleLogger simpleLogger;

    public static SimpleDateFormat getFormatter() {
        return LogListView.formatter;
    }

    public void clearLog() {
        simpleLogger.clear();
        logItems.clear();
    }

    public void pause() {
        logTransfer.pause();
    }

    public void play() {
        logTransfer.play();
    }

    public void stop() {
        logTransfer.stop();
    }

    public LogListView(final SimpleLogger simpleLogger) {
        this.simpleLogger = simpleLogger;
        this.setEditable(false);
        this.setItems(logItems);
        logTransfer.getKeyFrames().add(new KeyFrame(Duration.seconds(1),  event -> {
            int oldSize = logItems.size();
            simpleLogger.drainTo(logItems);
            if (logItems.size() > MAX_ENTRIES) {
                logItems.remove(0, logItems.size() - MAX_ENTRIES);
            }
            int newSize = logItems.size();
            if (oldSize != newSize) {
                this.scrollTo(newSize - 1);
            }
            event.consume();
        }));
        logTransfer.setCycleCount(Timeline.INDEFINITE);

        this.parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                logTransfer.pause();
            } else {
                logTransfer.play();
            }
        });

        setCellFactory(param -> new ListCell<SimpleLogRecord>() {

            @Override
            protected void updateItem(SimpleLogRecord item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    this.setText(null);
                    return;
                }
                String context = item.getContext() == null ? "" : item.getContext() + " ";
                this.setText(formatter.format(item.getDate()) + " " + context + item.getMessage());
            }
        });
    }
}
