package dev.ksaweryr.DbCardGenerator.util;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtils {
    public static <T> Stream<List<T>> chunked(Stream<T> stream, int size) {
        AtomicInteger cnt = new AtomicInteger(0);

        return stream.collect(Collectors.groupingBy(x -> cnt.getAndIncrement() / size))
                .entrySet()
                .stream()
                .sorted(Entry.comparingByKey())
                .map(Entry::getValue);
    }
}
