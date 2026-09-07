package com.lemongo.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generates request ids such as REQ-20260907183000001.
 */
public final class RequestIdGenerator {

    private static final DateTimeFormatter SECOND_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static String currentSecond = "";
    private static int sequence;

    private RequestIdGenerator() {
    }

    public static synchronized String next() {
        String second = LocalDateTime.now().format(SECOND_FORMAT);
        if (!second.equals(currentSecond)) {
            currentSecond = second;
            sequence = 0;
        } else {
            sequence = (sequence + 1) % 1000;
        }
        return "REQ-" + second + String.format("%03d", sequence);
    }
}

