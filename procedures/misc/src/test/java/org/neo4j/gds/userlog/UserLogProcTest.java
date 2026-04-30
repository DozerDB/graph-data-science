/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.gds.userlog;

import org.junit.jupiter.api.Test;
import org.neo4j.gds.user.log.UserLogEntry;
import org.neo4j.gds.procedures.GraphDataScienceProcedures;
import org.neo4j.gds.procedures.operations.OperationsProcedureFacade;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserLogProcTest {
    @Test
    void shouldLogUserWarnings() {
        var facade = mock(GraphDataScienceProcedures.class);
        var userLogProc = new UserLogProc();
        userLogProc.facade = facade;

        var expectedWarnings = Stream.of(
            UserLogEntry.create("lt", "going once", 1),
            UserLogEntry.create("lt", "going twice...", 2),
            UserLogEntry.create("lt", "gone!", 3)
        );
        var operationsProcedureFacade = mock(OperationsProcedureFacade.class);
        when(facade.operations()).thenReturn(operationsProcedureFacade);
        when(operationsProcedureFacade.queryUserLog("unused")).thenReturn(expectedWarnings);
        var actualWarnings = userLogProc.queryUserLog("unused");

        assertThat(actualWarnings).isSameAs(expectedWarnings);
    }
}
