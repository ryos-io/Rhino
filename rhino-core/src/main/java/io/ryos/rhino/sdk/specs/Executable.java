package io.ryos.rhino.sdk.specs;

public interface Executable {

    static HttpExecutable http(String name) {
        return new HttpExecutable(name);
    }

    void execute();
}
