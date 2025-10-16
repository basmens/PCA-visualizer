package nl.basmens.imageLoaders;

import org.ejml.simple.SimpleMatrix;

import nl.benmens.processing.PApplet;
import nl.benmens.processing.PAppletProxy;
import processing.core.PImage;

import static nl.benmens.processing.PAppletProxy.color;
import static nl.benmens.processing.PAppletProxy.createImage;

public class GrayscaleImageLoader extends AbstractImageLoader {
  @Override
  protected SimpleMatrix processImage(PImage img) {
    double[] pixels = new double[img.pixels.length];
    for (int i = 0; i < img.pixels.length; i++) {
      pixels[i] = PAppletProxy.brightness(img.pixels[i]) / 255D;
    }
    return new SimpleMatrix(pixels);
  }

  @Override
  public PImage vectorToImage(SimpleMatrix vector) {
    double[] data = vector.getDDRM().getData();
    double size = Math.sqrt(vector.getNumElements());
    PImage img = createImage((int) Math.ceil(size), (int) Math.floor(size), PApplet.RGB);
    for (int i = 0; i < img.pixels.length; i++)
      img.pixels[i] = color((float) (data[i] * 255));
    img.updatePixels();
    return img;
  }
}
