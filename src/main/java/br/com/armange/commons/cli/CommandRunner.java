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

import java.io.File;
import java.util.List;

public interface CommandRunner {

    CommandResult run(String... command);

    CommandResult run(List<String> outputLines, String... command);

    CommandResult run(File directory, String... command);

    CommandResult run(File directory, List<String> outputLines, String... command);
}
