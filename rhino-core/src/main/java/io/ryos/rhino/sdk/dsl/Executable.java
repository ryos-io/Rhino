package io.ryos.rhino.sdk.dsl;

public interface Executable {

    static HttpExecutable http(String name) {
        return new HttpExecutable(name);
    }

    void execute();
}
