package org.ape.control;

import org.ape.layout.AnnotationTree;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import org.ape.annotations.treeObjects.AnnotationObject;
import org.ape.annotations.treeObjects.CategoryObject;
import org.ape.annotations.treeObjects.serializable.*;
import org.ape.AppData;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.fxmisc.richtext.model.Paragraph;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public abstract class FileTask<T> extends Task<T> {

    long max;
    final File file;
    final private ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();

    FileTask(@NotNull File file,
             @NotNull String name) {
        this.file = file;
        this.name.set(name);
        Controller.taskMonitor.monitor(this);
    }

    ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }
}

class NewProjectTask extends FileTask {

    NewProjectTask(File file,
                   String name) {
        super(file, name);
        this.max = file.length();
    }

    @Override
    protected Void call() throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            final byte[] buffer = new byte[(int) Math.min(Integer.MAX_VALUE, this.max)];
            int read, offset = 0, limit = Math.min(buffer.length, Controller.READ_BUF_SIZE);
            while (limit > 0 && (read = fis.read(buffer, offset, limit)) != -1) {
                offset += read;
                limit = Math.min(buffer.length - offset, Controller.READ_BUF_SIZE);
                this.updateProgress(offset, this.max);
            }
            Platform.runLater(() -> {
                Controller.cleanup();
                AppData.textArea.replaceText(new String(buffer));
                AppData.tree.setRoot(AnnotationTree.create(new CategoryObject(AppData.id++, null, "Categories", "", Color.WHITE)));
                AppData.isActiveProject.set(true);
                AppData.textArea.moveTo(0);
            });
        }
        this.updateProgress(1, 1);
        return null;
    }
}

class SaveOptionsTask extends FileTask<Void> {

    private String warning;

    SaveOptionsTask(@NotNull File file,
                    @NotNull String name,
                    String warning) {
        super(file, name);
        this.warning = warning;
    }

    @Override
    protected Void call() throws Exception {
        try (FileOutputStream fis = new FileOutputStream(this.file)) {
            Controller.getProperties().store(fis, this.warning);
        }
        return null;
    }
}

class ReadProjectTask extends FileTask<Integer> {

    ReadProjectTask(File file,
                    String name) {
        super(file, name);
        this.max = file.getTotalSpace();
    }

    @Override
    protected Integer call() throws Exception {
        int skipBytes;
        try (CountingInputStream cis = new CountingInputStream(new FileInputStream(file));
             ExplicitHeaderInputStream ois = new ExplicitHeaderInputStream(cis)) {
            cis.resetCount();
            ois.actuallyReadStreamHeader();

            int length = ois.readInt();
            final byte[] buffer = new byte[length];
            int read, offset = 0, limit = Math.min(buffer.length, Controller.READ_BUF_SIZE);
            while (limit > 0 && (read = ois.read(buffer, offset, limit)) != -1) {
                offset += read;
                limit = Math.min(buffer.length - offset, Controller.READ_BUF_SIZE);
                this.updateProgress(offset, this.max);
            }
            skipBytes = cis.getCount();

            final long id = ois.readLong();
            final long anchorId = ois.readLong();
            final long catId = ois.readLong();
            final SerializableCatObj serializedRoot = (SerializableCatObj) ois.readObject();

            Platform.runLater(() -> {
                Controller.cleanup();
                AppData.textArea.replaceText(new String(buffer));
                AppData.id = id;
                AppData.tree.setRoot(AnnotationTree.create(serializedRoot.deserialize(null)));
                AppData.defaultCategory.set((CategoryObject) AppData.categories
                        .stream()
                        .filter(co -> co.getId() == catId)
                        .findFirst()
                        .orElse(null));
                AppData.anchorAnnotation.set((AnnotationObject) AppData.treeObjects
                        .stream()
                        .filter(to -> to.getId() == anchorId)
                        .findFirst()
                        .orElse(null));
                AppData.saveFile.set(this.file);
                AppData.isActiveProject.set(true);
                AppData.textArea.moveTo(0);
            });
            this.updateProgress(1, 1);
        }
        return skipBytes;
    }

    private class ExplicitHeaderInputStream extends ObjectInputStream {

        ExplicitHeaderInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected void readStreamHeader() {}

        final void actuallyReadStreamHeader() throws IOException {
            super.readStreamHeader();
        }

    }
}

class WriteProjectTask extends FileTask<Integer> {

    private SerializableCatObj serializedRoot;
    private long id, catId, anchorId, max;
    private byte[] data;
    private int skipBytes;

    WriteProjectTask(File file,
                     String name,
                     int skipBytes) throws AssertionError {
        super(file, name);
        serializedRoot = new SerializableCatObj((CategoryObject) AppData.treeObjects
                .stream()
                .filter(to -> to.getParent() == null)
                .findFirst()
                .orElse(null), null);
        data = AppData.textArea.getText().getBytes();
        this.id = AppData.id;
        this.catId = AppData.defaultCategory.get() == null ? -1 : AppData.defaultCategory.get().getId();
        this.anchorId = AppData.anchorAnnotation.get() == null ? -1 : AppData.anchorAnnotation.get().getId();
        this.max = data.length;
        this.skipBytes = skipBytes;
    }

    private void writeFully() throws IOException {
        try (CountingOutputStream cos = new CountingOutputStream(new FileOutputStream(this.file));
             ExplicitHeaderObjectOutputStream oos = new ExplicitHeaderObjectOutputStream(cos)) {
            cos.resetCount();
            oos.actuallyWriteStreamHeader();
            oos.writeInt(this.data.length);

            int offset = 0, limit = Math.min(Controller.WRITE_BUF_SIZE, data.length);
            while (limit > 0) {
                oos.write(this.data, offset, limit);
                offset += limit;
                limit = Math.min(Controller.WRITE_BUF_SIZE, data.length - offset);
                this.updateProgress(offset, this.max);
            }
            oos.flush();
            this.skipBytes = cos.getCount();

            oos.writeLong(this.id);
            oos.writeLong(this.anchorId);
            oos.writeLong(this.catId);
            oos.writeObject(serializedRoot);
            oos.flush();
            this.updateProgress(1, 1);
        }
    }

    private void tryWritePartially() throws IOException {
        new FileOutputStream(this.file, true).getChannel().truncate(this.skipBytes).close();
        try (FileOutputStream fos = new FileOutputStream(this.file, true);
             ExplicitHeaderObjectOutputStream oos = new ExplicitHeaderObjectOutputStream(fos)) {
            oos.writeLong(this.id);
            oos.writeLong(this.anchorId);
            oos.writeLong(this.catId);
            oos.writeObject(serializedRoot);
            oos.flush();
            this.updateProgress(1, 1);
        } catch (Exception e) {
            this.updateProgress(0, 1);
            this.writeFully();
        }
    }

    @Override
    protected Integer call() throws Exception {
        if (this.skipBytes == -1) {
            this.writeFully();
        } else {
            this.tryWritePartially();
        }
        return this.skipBytes;
    }

    private class ExplicitHeaderObjectOutputStream extends ObjectOutputStream {

        ExplicitHeaderObjectOutputStream(OutputStream os) throws IOException {
            super(os);
        }

        @Override
        protected void writeStreamHeader() throws IOException {}

        final void actuallyWriteStreamHeader() throws IOException {
            super.writeStreamHeader();
        }
    }
}

class ExportProjectTask extends FileTask<Void> {

    private static final String ANNOT_DELIMITER = " ";

    private final HashMap<Integer, HashSet<SerializableDisTreeObj>> charPosAnnots = new HashMap<>();
    private final List<String> lines = AppData.textArea.getParagraphs()
            .stream()
            .map(Paragraph::getText)
            .collect(Collectors.toList());
    private int charPos, lineNum;
    private final boolean onlySelected, outputId, outputDescription, ignoreInvalid, trimDelims;

    ExportProjectTask(File file, String name,
                      boolean onlySelected,
                      boolean outputId,
                      boolean outputDescription,
                      boolean ignoreInvalid,
                      boolean trimDelims,
                      int startLineNum) {
        super(file, name);
        this.onlySelected = onlySelected;
        this.outputId = outputId;
        this.outputDescription = outputDescription;
        this.ignoreInvalid = ignoreInvalid;
        this.trimDelims  = trimDelims;
        this.lineNum = startLineNum;
        new SerializableCatObj((CategoryObject) AppData.treeObjects
                .stream()
                .filter(to -> to.getParent() == null)
                .findFirst()
                .orElse(null), charPosAnnots);
        this.max = lines.size() * 2;
    }

    @Override
    protected Void call() throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            final List<Token> tokens = new ArrayList<>();
            this.charPos = 0;
            long i = 0;
            for (String line : lines) {
                this.partitionLine(tokens, line);
                this.updateProgress(i++, this.max);
            }
            final Set<SerializableDisTreeObj> invalidAnnotations = this.charPosAnnots.values()
                    .parallelStream()
                    .flatMap(Set::stream)
                    .filter(sdto -> (!this.onlySelected || sdto.isSelected()) && sdto.isInvalid())
                    .collect(Collectors.toSet());
            if (invalidAnnotations.size() > 0 && !this.ignoreInvalid) {
                throw new Exception(
                        "Found " + invalidAnnotations.size() +
                        " invalid annotation/s at following positions:\n" +
                                invalidAnnotations.stream()
                                        .map(SerializableDisTreeObj::toPosition)
                                        .collect(Collectors.joining(" "))
                );
            }
            this.writeAndFlushRecords(bw, tokens, invalidAnnotations);
        }
        this.updateProgress(1, 1);
        return null;
    }

    private void setLineIndices(int start, int end) {
        HashSet<SerializableDisTreeObj> startSto = charPosAnnots.get(start);
        if (startSto != null) {
            startSto.forEach(sdto -> {
                if (sdto.isStart(start)) {
                    sdto.setIsOkStart();
                }
                sdto.addLineNumber(this.lineNum);
            });
        }
        HashSet<SerializableDisTreeObj> endSdto = charPosAnnots.get(end);
        if (endSdto != null) {
            endSdto.forEach(sdto -> {
                if (sdto.isEnd(end)) {
                    sdto.setIsOkEnd();
                }
            });
        }
    }

    private void partitionLine(final List<Token> tokens, final String line) {
        final Matcher matcher = Controller.getTokenPattern().matcher(line);
        String delimiter;
        int start, end = 0, lastEnd;
        while (matcher.find()) {
            lastEnd = end;
            start = matcher.start();
            end = matcher.end();

            delimiter = line.substring(lastEnd, start);
            this.charPos += delimiter.length();
            if (this.trimDelims) {
                delimiter = delimiter.trim();
            }
            if (!delimiter.isEmpty()) {
                tokens.add(new Token(-1, this.lineNum++, delimiter));
            }

            final int startPos = this.charPos;
            this.charPos += end - start;
            this.setLineIndices(startPos, this.charPos);
            tokens.add(new Token(startPos, this.lineNum++, matcher.group()));
        }
        delimiter = line.substring(end);
        this.charPos += delimiter.length();
        if (this.trimDelims) {
            delimiter = delimiter.trim();
        }
        if (!delimiter.isEmpty()) {
            tokens.add(new Token(-1, this.lineNum++, delimiter));
        }
        this.charPos++;
    }

    private void writeAndFlushRecords(BufferedWriter bw, List<Token> tokens,
                                      Set<SerializableDisTreeObj> invalidAnnotations) throws IOException {
        long i = Math.floorDiv(this.max, 2);
        for (final Token token : tokens) {
            bw.write(token.lineNum + ANNOT_DELIMITER +  token.data + ANNOT_DELIMITER);
            final HashSet<SerializableDisTreeObj> annotations = this.charPosAnnots.get(token.charPos);
            if (annotations != null) {
                bw.write(annotations
                        .stream()
                        .filter(sdto -> (!this.onlySelected || sdto.isSelected()) && !invalidAnnotations.contains(sdto))
                        //at this point, ignoreInvalid is always true, see call() method
                        .map(sdto -> sdto.toRecord(token.charPos, outputId, outputDescription))
                        .collect(Collectors.joining(ANNOT_DELIMITER)));
            }
            bw.newLine();
            this.updateProgress(i++, this.max);
        }
        bw.flush();
    }

    private class Token {

        int charPos, lineNum;
        String data;

        Token(int charPos,
              int lineNum,
              String data
        ) {
            this.charPos = charPos;
            this.lineNum = lineNum;
            this.data = data;
        }
    }
}

class ExportLogTask extends FileTask {

    private final List<String> data;

    ExportLogTask(File file, String name, List<String> data) {
        super(file, name);
        this.max = data.size();
        this.data = data;
    }

    @Override
    protected Void call() throws Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.file))) {
            int i = 0;
            for (String record: this.data) {
                bw.write(record);
                bw.newLine();
                this.updateProgress(i++, this.max);
            }
        }
        return null;
    }
}