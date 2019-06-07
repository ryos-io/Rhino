package io.ryos.rhino.sdk.io;

import io.ryos.rhino.sdk.exceptions.ExceptionUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

/**
 * File resource reference.
 * <p>
 *
 * @author Erhan Bagdemir
 */
public class FileResource {

  public enum Type {

    /**
     * Classpath resource type, e.g classpath:///rhino.properties
     */
    CLASSPATH,

    /**
     * File resource type, e.g file:///user/home/rhino.properties
     */
    FILE}

  private static final String SEPARATOR = "://";
  private final String sourceURI;
  private final Function<String, InputStream> classpathResourcePointer =
      path -> getClass().getResourceAsStream(path);

  private final Function<String, InputStream> fileResourcePointer = path -> {
    var filePath = Path.of(path);
    var file = filePath.toFile();
    if (file.isFile() &&
        file.exists() &&
        file.canRead()) {

      try {
        return new FileInputStream(file);
      } catch (FileNotFoundException e) {
        ExceptionUtils.rethrow(e, RuntimeException.class,
            "Unexpected file not found in path:" + filePath.toString());
      }
    }
    return null;
  };

  public FileResource(String sourceURI) {
    this.sourceURI = Objects.requireNonNull(sourceURI);
  }

  public InputStream getInputStream() {
    final String[] split = sourceURI.split(SEPARATOR);
    var sourceType = split[0].toUpperCase();
    var pathToSource = split[1];
    switch (Type.valueOf(sourceType)) {
      case CLASSPATH: return classpathResourcePointer.apply(pathToSource);
      case FILE: return fileResourcePointer.apply(pathToSource);
      default: throw new UnsupportedOperationException("Rhino knows only about classpath files or"
            + " files on file system.");
    }
  }
}
