package io.ryos.rhino.sdk.specs;

import org.junit.Test;

import static io.ryos.rhino.sdk.specs.Executable.http;

public class ExecSpecTest {


    @Test
    public void testExecSpec() {
        var execSpec =
                SpecCatalog.httpSpec().exec(session -> http("name"));

    }
}
