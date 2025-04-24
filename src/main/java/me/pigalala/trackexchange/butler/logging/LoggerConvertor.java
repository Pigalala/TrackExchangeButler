package me.pigalala.trackexchange.butler.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public final class LoggerConvertor extends ClassicConverter {
    
    private static final String ANSI_YELLOW = "\033[38;5;214m";
    private static final String ANSI_RED = "\033[38;5;196m";

    @Override
    public String convert(ILoggingEvent event) {
        return switch (event.getLevel().toInt()) {
            case Level.WARN_INT -> ANSI_YELLOW;
            case Level.ERROR_INT -> ANSI_RED;
            default -> "";
        };
    }
}
