package io.ryos.rhino.sdk.providers;

import static io.ryos.rhino.sdk.converter.TypeConverter.asInt;
import static io.ryos.rhino.sdk.converter.TypeConverter.asList;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.exceptions.NotReadyException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provider instance to create random in-memory files which can be used in tests e.g upload files.
 *
 * @author Erhan Bagdemir
 */
public class RandomInMemoryFileProvider extends AbstractProvider<RandomInMemoryFile> {

  private static final String MIME_TYPES = "mimeTypes";
  private static final String MAX_SIZE = "maxSize";
  private static final String NUMBER_OF_FILES = "numberOfFiles";
  private List<String> mimeTypes;
  private int maxSize;
  private int numberOfFiles;
  private Random randomizer = new Random();
  private CyclicIterator<RandomInMemoryFile> fileIterator;

  public RandomInMemoryFileProvider() {
    try {
      this.mimeTypes = getConfig(MIME_TYPES, asList());
      this.maxSize = getConfig(MAX_SIZE, asInt());
      this.numberOfFiles = getConfig(NUMBER_OF_FILES, asInt());
      init();
    } catch (NotReadyException e) {
      // Not ready, safe to ignore this exception.
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
    var inMemoryFiles = Stream
        .generate(() -> new RandomInMemoryFile(randomizer.nextInt(maxSize), cyclicIterator.next()))
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
