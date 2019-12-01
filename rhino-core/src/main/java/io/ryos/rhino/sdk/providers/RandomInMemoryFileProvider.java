package io.ryos.rhino.sdk.providers;

import static io.ryos.rhino.sdk.converter.TypeConverter.asInt;
import static io.ryos.rhino.sdk.converter.TypeConverter.asList;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.exceptions.NotReadyException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomInMemoryFileProvider extends AbstractProvider<RandomInMemoryFile> {

  private List<String> mimeTypes;
  private int maxSize;
  private int numberOfFiles;

  private CyclicIterator<RandomInMemoryFile> fileIterator;

  public RandomInMemoryFileProvider() {
    try {
      this.mimeTypes = getConfig("mimeTypes", asList());
      this.maxSize = getConfig("maxSize", asInt());
      this.numberOfFiles = getConfig("numberOfFiles", asInt());

      init();
    } catch (NotReadyException e) {
      // Not ready
    }
  }

  public RandomInMemoryFileProvider(List<String> mimeTypes, int maxSize, int numberOfFiles) {

    this.maxSize = maxSize;
    this.mimeTypes = mimeTypes;
    this.numberOfFiles = numberOfFiles;

    init();
  }

  private void init() {
    var cyclicIterator = new CyclicIterator<>(mimeTypes);
    var random = new Random();
    var inMemoryFiles = Stream
        .generate(() -> new RandomInMemoryFile(random.nextInt(maxSize), cyclicIterator.next()))
        .limit(numberOfFiles)
        .collect(Collectors.toList());

    this.fileIterator = new CyclicIterator<>(inMemoryFiles);
  }

  @Override
  public RandomInMemoryFile take() {
    return fileIterator.next();
  }

  @Override
  public String name() {
    return getClass().getSimpleName();
  }
}
