package dev.ksaweryr.DbCardGenerator.util;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
    R apply(T obj) throws E;

    static <T, R> Function<T, R> wrapper(ThrowingFunction<T, R, ? extends Exception> throwingFunction) {
        return x -> {
            try {
                return throwingFunction.apply(x);
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }
}
