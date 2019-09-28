package io.ryos.rhino.sdk.providers;

import io.ryos.rhino.sdk.CyclicIterator;
import io.ryos.rhino.sdk.SimulationConfig;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomInMemoryFileProvider implements Provider<RandomInMemoryFile> {

  private CyclicIterator<RandomInMemoryFile> fileIterator;

  public RandomInMemoryFileProvider(int numberOfFiles, int maxSize, List<String> mimeTypes) {
    var cyclicIterator = new CyclicIterator<>(mimeTypes);
    var random = new Random();
    var inMemoryFiles = Stream
        .generate(() -> new RandomInMemoryFile(random.nextInt(maxSize), cyclicIterator.next()))
        .limit(numberOfFiles)
        .collect(Collectors.toList());

    this.fileIterator = new CyclicIterator<>(inMemoryFiles);
  }

  public RandomInMemoryFileProvider() {
    this(SimulationConfig.getInMemoryFileProviderNumberOfFiles(),
        SimulationConfig.getInMemoryFileProviderMaxSize(),
        SimulationConfig.getInMemoryFileProviderMimeTypes());
  }

  @Override
  public RandomInMemoryFile take() {
    return fileIterator.next();
  }
}
