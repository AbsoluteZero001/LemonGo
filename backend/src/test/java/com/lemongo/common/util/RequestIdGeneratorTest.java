package com.lemongo.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RequestIdGeneratorTest {

    @Test
    void generatesPrefixedUniqueIds() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(RequestIdGenerator.next());
        }

        assertEquals(1000, ids.size());
        assertTrue(ids.stream().allMatch(id -> id.startsWith("REQ-")
                && id.length() >= "REQ-20260907183000001".length()));
    }
}

