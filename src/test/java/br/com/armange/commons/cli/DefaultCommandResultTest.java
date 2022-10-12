package br.com.armange.commons.cli;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultCommandResultTest {

    public static final String LINE_1 = "Line1";
    public static final String LINE_2 = "Line2";
    public static final List<String> RESULT_LINES = Arrays.asList(LINE_1, LINE_2);

    @Test
    void shouldGetResultMessage() {
        final DefaultCommandResult commandResult = DefaultCommandResult.
                newDefaultCommandResult(0, RESULT_LINES);

        assertEquals("Line1\nLine2\n", commandResult.getResultMessage());
        assertEquals(0, commandResult.getResultCode());
        assertThat(commandResult.getResultLines(), hasItems(LINE_1, LINE_2));
        assertEquals("", commandResult.getAdditionalMessage());
        assertNotNull(commandResult.stream());
        assertThat(commandResult.stream().collect(Collectors.toList()), hasItems(LINE_1, LINE_2));
    }

    @Test
    void shouldAppendAdditionalMessage() {
        final DefaultCommandResult commandResult = DefaultCommandResult.
                newDefaultCommandResult(0, Collections.emptyList());

        assertEquals("", commandResult.getResultMessage());
        assertEquals(0, commandResult.getResultCode());
        assertThat(commandResult.getResultLines(), hasSize(0));
        assertEquals("", commandResult.getAdditionalMessage());
        assertNotNull(commandResult.stream());
        assertThat(commandResult.stream().collect(Collectors.toList()), hasSize(0));

        commandResult.appendAdditionalMessage(LINE_1);
        assertEquals(LINE_1, commandResult.getResultMessage());
        assertEquals(0, commandResult.getResultCode());
        assertThat(commandResult.getResultLines(), hasSize(0));
        assertEquals(LINE_1, commandResult.getAdditionalMessage());
        assertNotNull(commandResult.stream());
        assertThat(commandResult.stream().collect(Collectors.toList()), hasSize(0));

        commandResult.appendAdditionalMessage(LINE_2);
        assertEquals(LINE_1+"\n"+LINE_2, commandResult.getResultMessage());
        assertEquals(0, commandResult.getResultCode());
        assertThat(commandResult.getResultLines(), hasSize(0));
        assertEquals(LINE_1+"\n"+LINE_2, commandResult.getAdditionalMessage());
        assertNotNull(commandResult.stream());
        assertThat(commandResult.stream().collect(Collectors.toList()), hasSize(0));
    }
}
