package nl.basmens;

import java.io.File;
import java.util.Arrays;

import org.ejml.simple.SimpleMatrix;

import nl.basmens.imageLoaders.AbstractImageLoader;
import nl.basmens.imageLoaders.ColorImageLoader;
import nl.basmens.imageLoaders.GrayscaleImageLoader;
import nl.basmens.imageLoaders.HsbImageLoader;
import nl.benmens.processing.PApplet;
import nl.benmens.processing.PAppletProxy;
import processing.core.PImage;

public class Main extends PApplet {
  // private static final String[] IMAGES_FOLDER_PATHS = {
  //   // "C:/Users/basme/AppData/Roaming/.minecraft/resourcepacks/1.21.8/assets/minecraft/textures/painting",
  //   // "C:/Users/basme/AppData/Roaming/.minecraft/resourcepacks/1.21.8/assets/minecraft/textures/block"
  //   "C:/Users/basme/AppData/Roaming/.minecraft/resourcepacks/1.21.8/assets/minecraft/textures/item"
  // };
  private static final String[] IMAGES_FOLDER_PATHS = {
    "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/0",
    // "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/1",
    // "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/2",
    // "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/3",
    // "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/4",
    // "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/5",
    // "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/6",
    // "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/7",
    // "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/8",
    "C:/Users/basme/OneDrive/Desktop laptop/PCA visualizer/datasets/mnist_images/testing/9"
  };

  private static final String[] SUPPORTED_IMAGE_EXTENSIONS = new String[] { "JPG", "jpg", "tiff", "bmp", "BMP", "gif",
    "GIF", "WBMP", "png", "PNG", "JPEG", "tif", "TIF", "TIFF", "wbmp", "jpeg" };

  private AbstractImageLoader imageLoader = new GrayscaleImageLoader();
  // private AbstractImageLoader imageLoader = new ColorImageLoader();
  // private AbstractImageLoader imageLoader = new HsbImageLoader();

  private SimpleMatrix dataMatrix;
  private PcaCalculator pcaCalculator;

  private int view = 2;
  private int statView = 0;
  private int eigenView = 0;
  private int reconstructionView = 0;
  private int dimensionCount = 1;
  private boolean reconstructAdjustForMean = true;

  public void settings() {
    size(512, 512, P2D);
  }

  public void setup() {
    loadData();
    pcaCalculator = new PcaCalculator(dataMatrix);

    switch (view) {
      case 0:
        drawStats();
        break;
      case 1:
        drawEigenvector();
        break;
      case 2:
        drawReconstructions();
        break;
      default:
        break;
    }
  }

  public void draw() {
  }

  private PImage upscaleImage(PImage img, int targetWidth, int targetHeight) {
    PImage upscaled = createImage(targetWidth, targetHeight, RGB);
    for (int x = 0; x < targetWidth; x++) {
      for (int y = 0; y < targetHeight; y++) {
        int srcX = x * img.width / targetWidth;
        int srcY = y * img.height / targetHeight;
        upscaled.pixels[y * targetWidth + x] = img.pixels[srcY * img.width + srcX];
      }
    }
    return upscaled;
  }

  private void loadData() {
    // Load images
    File[] imagePaths = Arrays.stream(IMAGES_FOLDER_PATHS)
        .flatMap(f -> Arrays.stream(PAppletProxy.listFiles(f)))
        .filter(f -> f.getName().matches(".*.(JPG|jpg|tiff|bmp|BMP|gif|GIF|WBMP|png|PNG|JPEG|tif|TIF|TIFF|wbmp|jpeg)$"))
        .sorted((a, b) -> a.getName().compareTo(b.getName()))
        .toArray(File[]::new);
    dataMatrix = imageLoader.loadImages(imagePaths);
  }

  public void drawStats() {
    SimpleMatrix statData = switch (statView) {
      case 0 -> pcaCalculator.getMean();
      case 1 -> pcaCalculator.getVariance();
      case 2 -> pcaCalculator.getStandardDeviation();
      default -> throw new IllegalStateException("Unexpected value: " + statView);
    };
    background(upscaleImage(imageLoader.vectorToImage(statData), width, height));

    String stat = switch (statView) {
      case 0 -> "Mean";
      case 1 -> "Variance";
      case 2 -> "Standard Deviation";
      default -> throw new IllegalStateException("Unexpected value: " + statView);
    };
    noStroke();
    fill(0, 30);
    rect(0, 0, width * 2 / 3, 25 * 2 + 10);
    fill(255);
    textAlign(LEFT, TOP);
    textSize(20);
    text("View: stat", 10, 5);
    text("Stat: " + stat, 10, 25 + 5);
  }

  public void drawEigenvector() {
    SimpleMatrix eigenData = pcaCalculator.getEigenvectors()[eigenView];
    background(upscaleImage(imageLoader.vectorToImage(eigenData), width, height));

    noStroke();
    fill(0, 30);
    rect(0, 0, width * 2 / 3, 25 * 3 + 10);
    fill(255);
    textAlign(LEFT, TOP);
    textSize(20);
    text("View: eigenvector", 10, 5);
    text("Num: " + eigenView + "/" + (pcaCalculator.getEigenvectors().length - 1), 10, 25 + 5);
    text(String.format("Value: %.1f", pcaCalculator.getEigenvalues()[eigenView]), 10, 25 * 2 + 5);
  }

  public void drawReconstructions() {
    SimpleMatrix imgToReconstruct = dataMatrix.extractVector(false, reconstructionView);
    SimpleMatrix reconstructionData = pcaCalculator.reconstructInDimensions(imgToReconstruct, dimensionCount,
        reconstructAdjustForMean);
    background(upscaleImage(imageLoader.vectorToImage(reconstructionData), width, height));

    double totalVariance = Arrays.stream(pcaCalculator.getEigenvalues()).sum();
    double explainedVariance = Arrays.stream(pcaCalculator.getEigenvalues(), 0, dimensionCount).sum();
    double explainedVariancePercent = explainedVariance / totalVariance * 100;
    double mse = imgToReconstruct.minus(reconstructionData).elementPower(2).elementSum()
        / pcaCalculator.getFeatureCount();

    noStroke();
    fill(0, 30);
    rect(0, 0, width * 2 / 3, 25 * 6 + 10);
    fill(255);
    textAlign(LEFT, TOP);
    textSize(20);
    text("View: reconstruction", 10, 5);
    text("Num: " + reconstructionView + "/" + (pcaCalculator.getSampleCount() - 1), 10, 25 + 5);
    text("Dim: " + dimensionCount + "/" + pcaCalculator.getIntrinsicDimensionality(), 10, 25 * 2 + 5);
    text("Adjust for mean: " + reconstructAdjustForMean, 10, 25 * 3 + 5);
    text(String.format("Explained var: %.10f", explainedVariancePercent), 10, 25 * 4 + 5);
    text(String.format("MSE: %.5f", mse), 10, 25 * 5 + 5);
  }

  public void mousePressed() {
    view = (view + 1) % 3;
    switch (view) {
      case 0 -> drawStats();
      case 1 -> drawEigenvector();
      case 2 -> drawReconstructions();
      default -> throw new IllegalStateException("Unexpected value: " + view);
    }
  }

  public void keyPressed() {
    if (view == 0 && key == ' ') {
      statView = (statView + 1) % 3;
      drawStats();
    } else if (view == 1 && key == ' ') {
      eigenView = (eigenView + 1) % pcaCalculator.getIntrinsicDimensionality();
      drawEigenvector();
    } else if (view == 2 && (key == ' ' || key == 'm' || (key >= '0' && key <= '9'))) {
      if (key == ' ') {
        reconstructionView = (reconstructionView + 1) % pcaCalculator.getSampleCount();
      } else if (key == 'm') {
        reconstructAdjustForMean = !reconstructAdjustForMean;
      } else {
        int step = (int) Math.pow(10, key - '0');
        dimensionCount = dimensionCount == pcaCalculator.getIntrinsicDimensionality()
            ? 1
            : Math.min(dimensionCount + step, pcaCalculator.getIntrinsicDimensionality());
      }
      drawReconstructions();
    }
  }

  public static void main(String[] args) {
    if (args != null) {
      PApplet.main(new Object() {
      }.getClass().getEnclosingClass(), args);
    } else {
      PApplet.main(new Object() {
      }.getClass().getEnclosingClass());
    }
  }
}
