package com.tabaldi.api;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ThreadFilter extends Filter<ILoggingEvent> {

//    private final static Logger logger = LoggerFactory.getLogger(ThreadFilter.class);

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (event.getThreadName().contains("main") || (message != null && message.contains("/files/get/file/"))) {
            return FilterReply.DENY;
        } else {
            return FilterReply.NEUTRAL;
        }
    }
}
