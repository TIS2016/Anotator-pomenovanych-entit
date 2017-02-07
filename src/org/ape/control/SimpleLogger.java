package org.ape.control;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class SimpleLogger {

    private static final int MAX_LOG_ENTRIES = 100000;
    private static final String NEW_CONTEXT = "[NEW]";
    private static final String UPDATE_CONTEXT = "[UPDATE]";
    private static final String DELETE_CONTEXT = "[DELETE]";

    private final BlockingDeque<SimpleLogRecord> log = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);

    private void logRecord(SimpleLogRecord record) {
        log.offer(record);
    }

    public void logNew(String message) {
        this.logRecord(new SimpleLogRecord(NEW_CONTEXT, message));
    }

    public void logUpdate(String message) {
        this.logRecord(new SimpleLogRecord(UPDATE_CONTEXT, message));
    }

    public void logDelete(String message) {
        this.logRecord(new SimpleLogRecord(DELETE_CONTEXT, message));
    }

    public void drainTo(Collection<? super SimpleLogRecord> collection) {
        log.drainTo(collection);
    }

    public void clear() {
        log.clear();
    }
}