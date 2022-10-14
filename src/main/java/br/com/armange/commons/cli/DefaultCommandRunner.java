/**
 * Copyright (C) 2022 Diego Armange Costa (https://github.com/armange)
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package br.com.armange.commons.cli;

import br.com.armange.commons.cli.util.CollectionsUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class DefaultCommandRunner implements CommandRunner {

    private static final String ERROR_MESSAGE = "An internal error occurred and the last action " +
            "could not be completed.";

    private final File sourceInputString;
    private final File targetOutputStream;
    private final File targetErrorStream;
    private final Integer timeoutValue;
    private final TimeUnit timeoutUnit;

    public DefaultCommandRunner() {
        this.sourceInputString = null;
        this.targetOutputStream = null;
        this.targetErrorStream = null;
        this.timeoutValue = null;
        this.timeoutUnit = null;
    }

    public DefaultCommandRunner(final File sourceInputString,
                                final File targetOutputStream,
                                final File targetErrorStream,
                                final Integer timeoutValue,
                                final TimeUnit timeoutUnit) {
        this.sourceInputString = sourceInputString;
        this.targetOutputStream = targetOutputStream;
        this.targetErrorStream = targetErrorStream;
        this.timeoutValue = timeoutValue;
        this.timeoutUnit = timeoutUnit;
    }

    @Override
    public CommandResult run(final String... command) {
        return run(null, null, command);
    }

    @Override
    public CommandResult run(List<String> outputLines, String... command) {
        return run(null, outputLines, command);
    }

    @Override
    public CommandResult run(final File directory, String... command) {
        return run(directory, null, command);
    }

    @Override
    public CommandResult run(final File directory,
                             final List<String> outputLines,
                             final String... command) {
        try {
            final Process process = startProcess(directory, outputLines, command);
            final boolean result = process.waitFor(
                    Optional.ofNullable(timeoutValue).orElse(10),
                    Optional.ofNullable(timeoutUnit).orElse(TimeUnit.SECONDS));

            if (!result) {
                process.destroy();

                final CommandResult commandResult = extractResultMessage(process, 1);

                commandResult.appendAdditionalMessage(ERROR_MESSAGE);

                return commandResult;
            }

            return extractResultMessage(process, process.exitValue());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);

            throw new UncheckedIOException(e.getMessage(), e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();

            log.error(e.getMessage(), e);

            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    Process startProcess(final File directory,
                                 final List<String> outputLines,
                                 final String[] command) throws IOException {
        final ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);

        Optional.ofNullable(sourceInputString).ifPresent(processBuilder::redirectInput);
        Optional.ofNullable(targetOutputStream).ifPresent(processBuilder::redirectOutput);
        Optional.ofNullable(targetErrorStream).ifPresent(processBuilder::redirectError);
        Optional.ofNullable(directory).ifPresent(processBuilder::directory);

        final Process process = processBuilder.start();

        writeOutput(outputLines, process);

        return process;
    }

    private void writeOutput(final List<String> outputLines,
                             final Process process) throws IOException {
        if (CollectionsUtil.isNotEmpty(outputLines)) {
            try (final OutputStreamWriter writer = new OutputStreamWriter(
                    process.getOutputStream())) {
                for (final String line : outputLines) {
                    writer.write(line);
                    writer.write("\n");
                    writer.flush();
                }
            }
        }
    }

    private CommandResult extractResultMessage(final Process process, final int resultCode) {
        final List<String> inputs = new ArrayList<>();
        final DefaultCommandResult commandResult = DefaultCommandResult
                .newDefaultCommandResult(resultCode, inputs);

        try (
                final var bufferedInputReader = new BufferedReader(new
                        InputStreamReader(process.getInputStream()));
                final var bufferedErrorReader = new BufferedReader(new
                        InputStreamReader(process.getErrorStream()))
        ) {
            addAllLines(bufferedInputReader, inputs, commandResult);
            addAllLines(bufferedErrorReader, inputs, commandResult);
        } catch (final IOException e) {
            commandResult.appendAdditionalMessage(e.getMessage());
            log.error(e.getMessage(), e);
        }

        return commandResult;
    }

    private void addAllLines(final BufferedReader bufferedErrorReader,
                             final List<String> inputs,
                             final DefaultCommandResult commandResult) {
        try {
            inputs.addAll(bufferedErrorReader.lines().collect(Collectors.toList()));
        } catch (final Exception e) {
            commandResult.appendAdditionalMessage(e.getMessage());
            log.error(e.getMessage(), e);
        }
    }

    public static synchronized CommandResult runCmd(final String... command) {
        return new DefaultCommandRunner().run(command);
    }

    public static synchronized CommandResult runCmd(List<String> outputLines, String... command) {
        return new DefaultCommandRunner().run(outputLines, command);
    }

    public static synchronized CommandResult runCmd(final File directory, String... command) {
        return new DefaultCommandRunner().run(directory, command);
    }

    public static synchronized CommandResult runCmd(final File directory,
                                       final List<String> outputLines,
                                       final String... command) {
        return new DefaultCommandRunner().run(directory, outputLines, command);
    }
}
