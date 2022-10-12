package br.com.armange.commons.cli.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionsUtilTest {

    private List<String> collection;

    @Test
    void shouldBeEmpty() {
        collection = new ArrayList<>();

        assertFalse(CollectionsUtil.isNotEmpty(collection));
    }

    @Test
    void shouldBeNull() {
        collection = null;

        assertFalse(CollectionsUtil.isNotEmpty(collection));
    }

    @Test
    void shouldBeNotEmpty() {
        collection = Collections.singletonList("Hello");

        assertTrue(CollectionsUtil.isNotEmpty(collection));
    }
}
