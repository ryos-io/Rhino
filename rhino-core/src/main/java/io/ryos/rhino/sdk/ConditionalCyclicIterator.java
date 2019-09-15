package io.ryos.rhino.sdk;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ConditionalCyclicIterator<T> extends CyclicIterator<T> {
    private Predicate<T> predicate;

    public ConditionalCyclicIterator(List<T> list, Predicate<T> predicate) {
        super(list);

        this.predicate = Objects.requireNonNull(predicate);
    }



    public T next() {

        T next = super.next();

        if (predicate.test(next)) {
            stop();
        }

        return next;
    }
}
