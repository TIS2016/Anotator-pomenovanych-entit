package org.ape.control;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.ape.layout.dialogs.FixedExceptionDialog;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

public class TaskMonitor {

    private final ReadOnlyObjectWrapper<FileTask> currentTask = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper currentTaskName = new ReadOnlyStringWrapper();
    private final ReadOnlyDoubleWrapper currentTaskProgress = new ReadOnlyDoubleWrapper();

    public TaskMonitor() {
        final Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            if (!currentTaskName.isBound()) {
                currentTaskName.set("");
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        currentTaskName.addListener(((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                timeline.stop();
            } else {
                timeline.playFromStart();
            }
        }));
    }

    public void monitor(final FileTask<?> task) {
        task.stateProperty().addListener(new ChangeListener<Task.State>() {
            @Override
            public void changed(ObservableValue<? extends Task.State> observableValue, Task.State oldValue, Task.State newValue) {
                switch (newValue) {
                    case RUNNING:
                        currentTask.set(task);
                        currentTaskProgress.unbind();
                        currentTaskProgress.set(task.progressProperty().get());
                        currentTaskProgress.bind(task.progressProperty());
                        currentTaskName.unbind();
                        currentTaskName.set(task.nameProperty().get());
                        currentTaskName.bind(task.nameProperty());
                        break;
                    case SUCCEEDED:
                    case CANCELLED:
                    case FAILED:
                        if (newValue == Worker.State.FAILED) {
                            final Throwable throwable = task.getException();
                            final String taskName = currentTaskName.get();
                            Platform.runLater(() -> new FixedExceptionDialog(throwable, taskName).showAndWait());
                        }
                        currentTaskProgress.unbind();
                        currentTaskProgress.set(0);
                        currentTaskName.unbind();
                        currentTaskName.set(currentTaskName.get() + " " + newValue.toString().toLowerCase());
                        task.stateProperty().removeListener(this);
                        break;
                }
            }
        });
    }

    public ReadOnlyStringProperty currentTaskNameProperty() {
        return currentTaskName.getReadOnlyProperty();
    }

    public ReadOnlyDoubleProperty currentTaskProgressProperty() {
        return currentTaskProgress.getReadOnlyProperty();
    }
}
