package nl.basmens.imageLoaders;

import java.io.File;
import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;

import nl.benmens.processing.PAppletProxy;
import processing.core.PImage;

public abstract class AbstractImageLoader {
  abstract SimpleMatrix processImage(PImage img);
  public abstract PImage vectorToImage(SimpleMatrix vector);

  private static int mode(int[] arr) {
    Arrays.parallelSort(arr);
    int countStart = -1;
    int maxCount = 0;
    int maxValue = arr[0];
    for (int i = 0; i < arr.length; i++) {
      if (i + 1 < arr.length && arr[i] == arr[i + 1]) continue;
      if (i - countStart > maxCount) {
        maxCount = i - countStart;
        maxValue = arr[i];
      }
      countStart = i;
    }
    return maxValue;
  }
  
  public SimpleMatrix loadImage(File file) {
    return processImage(PAppletProxy.loadImage(file.getAbsolutePath()));
  }

  public SimpleMatrix loadImages(File[] files) {
    PImage[] images = new PImage[files.length];
    int[] widths = new int[files.length];
    int[] heights = new int[files.length];
    for (int i = 0; i < files.length; i++) {
      images[i] = PAppletProxy.loadImage(files[i].getAbsolutePath());
      widths[i] = images[i].width;
      heights[i] = images[i].height;
    }

    int width = mode(widths);
    int height = mode(heights);
    for (int i = 0; i < images.length; i++) {
      if (images[i].width == width && images[i].height == height) continue;
      images[i].resize(width, height);
      System.out.println("Warning: Resized image " + files[i].getName() + " to " + width + "x" + height);
    }

    SimpleMatrix[] columns = new SimpleMatrix[files.length - 1];
    for (int i = 1; i < files.length; i++) {
      columns[i - 1] = processImage(images[i]);
    }
    return processImage(images[0]).concatColumns(columns);
  }
}
