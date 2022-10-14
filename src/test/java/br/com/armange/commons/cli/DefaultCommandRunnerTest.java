package br.com.armange.commons.cli;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultCommandRunnerTest {

    public static final String JAVA = "java";
    public static final String JACOCO = "JaCoCo";
    public static final String PRINT_HELLO = "PrintHello.java";
    public static final String FIXTURES = "fixtures";

    @Test
    void shouldRunCommandWithBaseDirectoryAndWithoutOutputLines() {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(FIXTURES);

        assertNotNull(resource);

        final File directory = new File(resource.getFile());
        final CommandResult commandResult = DefaultCommandRunner
                .runCmd(directory, JAVA, PRINT_HELLO, JACOCO);

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        threadSet.forEach(System.out::println);

//        assertEquals(0, commandResult.getResultCode());
        assertEquals("Hello JaCoCo.\n", commandResult.getResultMessage());
    }

    @Test
    void shouldRunCommandWithoutBaseDirectoryAndWithoutOutputLines() {
        final CommandResult commandResult = DefaultCommandRunner
                .runCmd(JAVA, "./build/resources/test/fixtures/PrintHello.java",
                        "Green Day");

        assertEquals(0, commandResult.getResultCode());
        assertEquals("Hello Green Day.\n", commandResult.getResultMessage());
    }

    @Test
    void shouldRunCommandWithErrorAndWithoutBaseDirectoryAndWithoutOutputLines() {
        final CommandResult commandResult = DefaultCommandRunner
                .runCmd(JAVA, PRINT_HELLO, JACOCO);

        assertEquals(1, commandResult.getResultCode());
        assertEquals("Error: Could not find or load main class PrintHello.java\n" +
                "Caused by: java.lang.ClassNotFoundException: PrintHello.java\n",
                commandResult.getResultMessage());
    }

    @Test
    void shouldRunCommandWithBaseDirectoryAndWithoutOutputLinesAndWithOutputFile() {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(FIXTURES);
        final Path targetOutputPath = assertDoesNotThrow(() -> Files.createTempFile(
                null, null));
        final CommandRunner commandRunner = new DefaultCommandRunner(
                null,
                targetOutputPath.toFile(),
                null,
                null,
                null);

        assertNotNull(resource);

        final File directory = new File(resource.getFile());
        final CommandResult commandResult = commandRunner
                .run(directory, JAVA, PRINT_HELLO, JACOCO);
        final String fileContent = assertDoesNotThrow(() -> String
                .join("", Files.readAllLines(targetOutputPath)));

        assertEquals(0, commandResult.getResultCode());
        assertEquals("Hello JaCoCo.", fileContent);
    }

    @Test
    void shouldRunCommandWithBaseDirectoryAndWithOutputLines() {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(FIXTURES);

        assertNotNull(resource);

        final File directory = new File(resource.getFile());
        final CommandResult commandResult = DefaultCommandRunner
                .runCmd(directory,
                        Collections.singletonList(JACOCO),
                        JAVA, "AskName.java");

        assertEquals(0, commandResult.getResultCode());
        assertEquals("What is your name?\nHello JaCoCo.\n",
                commandResult.getResultMessage());
    }

    @Test
    void shouldRunCommandWithoutBaseDirectoryAndWithOutputLines() {
        final CommandResult commandResult = DefaultCommandRunner
                .runCmd(Collections.singletonList("Green Day"),
                        JAVA, "./build/resources/test/fixtures/AskName.java");

        assertEquals(0, commandResult.getResultCode());
        assertEquals("What is your name?\nHello Green Day.\n",
                commandResult.getResultMessage());
    }

    @Test
    void shouldRunCommandWithErrorAndWithBaseDirectoryAndWithoutOutputLines() {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(FIXTURES);

        assertNotNull(resource);

        final File directory = new File(resource.getFile());
        final CommandResult commandResult = DefaultCommandRunner
                .runCmd(directory,
                        JAVA, "AskName.java");

        assertEquals(1, commandResult.getResultCode());
        assertEquals("java.io.IOException: Stream closed\n" +
                        "java.io.IOException: Stream closed\n" +
                        "An internal error occurred and the last action could not be completed.",
                commandResult.getResultMessage());
    }

    @Test
    void shouldNotCompleteDueToReadOnlyFileInputStream() {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(FIXTURES);
        final Path targetOutputPath = assertDoesNotThrow(() -> Files.createTempFile(
                null, null));

        Set<PosixFilePermission> readOnly = PosixFilePermissions.fromString("r--r--r--");
        assertDoesNotThrow(() -> Files.setPosixFilePermissions(targetOutputPath, readOnly));

        final File targetOutputStream = targetOutputPath.toFile();
        final CommandRunner commandRunner = new DefaultCommandRunner(
                null,
                targetOutputStream,
                null,
                null,
                null);

        assertNotNull(resource);

        final File directory = new File(resource.getFile());

        assertThrows(UncheckedIOException.class, () -> commandRunner
                .run(directory, JAVA, PRINT_HELLO, JACOCO));
    }

    @Test
    void shouldInterruptCmdProcess() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(FIXTURES);

        assertNotNull(resource);

        final File directory = new File(resource.getFile());
        final DefaultCommandRunner commandRunner = spy(new DefaultCommandRunner());

        doAnswer(i -> {
            final Process process = spy((Process) i.callRealMethod());

            doThrow(InterruptedException.class).when(process).waitFor(anyLong(), any());

            return process;
        }).when(commandRunner).startProcess(any(), any(), any());

        assertThrows(IllegalStateException.class,
                () -> commandRunner.run(directory, JAVA, PRINT_HELLO, JACOCO));
    }

    @Test
    void shouldCatchIOExceptionFromInputStreamAndCompleteWithoutExceptions() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(FIXTURES);

        assertNotNull(resource);

        final File directory = new File(resource.getFile());
        final DefaultCommandRunner commandRunner = spy(new DefaultCommandRunner());

        doAnswer(i -> {
            final Process process = spy((Process) i.callRealMethod());

            doAnswer(j -> {
                final InputStream inputStream = spy((InputStream) j.callRealMethod());

                doThrow(IOException.class).when(inputStream).close();

                return inputStream;
            }).when(process).getInputStream();

            return process;
        }).when(commandRunner).startProcess(any(), any(), any());

        final CommandResult commandResult = commandRunner.run(directory, JAVA, PRINT_HELLO, JACOCO);

        assertEquals(1, commandResult.getResultCode());
    }

    @Test
    void shouldCatchIOExceptionFromErrorStreamAndCompleteWithoutExceptions() throws IOException {
        final URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(FIXTURES);

        assertNotNull(resource);

        final File directory = new File(resource.getFile());
        final DefaultCommandRunner commandRunner = spy(new DefaultCommandRunner());

        doAnswer(i -> {
            final Process process = spy((Process) i.callRealMethod());

            doAnswer(j -> {
                final InputStream inputStream = spy((InputStream) j.callRealMethod());

                doThrow(IOException.class).when(inputStream).close();

                return inputStream;
            }).when(process).getErrorStream();

            return process;
        }).when(commandRunner).startProcess(any(), any(), any());

        final CommandResult commandResult = commandRunner
                .run(directory, JAVA, PRINT_HELLO, JACOCO);

        assertEquals(1, commandResult.getResultCode());
    }
}
