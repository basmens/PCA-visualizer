package nl.basmens.imageLoaders;

import org.ejml.simple.SimpleMatrix;

import nl.benmens.processing.PApplet;
import nl.benmens.processing.PAppletProxy;
import processing.core.PImage;

import static nl.benmens.processing.PAppletProxy.blue;
import static nl.benmens.processing.PAppletProxy.color;
import static nl.benmens.processing.PAppletProxy.colorMode;
import static nl.benmens.processing.PAppletProxy.createImage;
import static nl.benmens.processing.PAppletProxy.green;
import static nl.benmens.processing.PAppletProxy.red;

public class HsbImageLoader extends AbstractImageLoader {
  @Override
  protected SimpleMatrix processImage(PImage img) {
    double[] pixels = new double[img.pixels.length * 3];
    for (int i = 0; i < img.pixels.length; i++) {
      pixels[i * 3] = PAppletProxy.hue(img.pixels[i]) / 255D;
      pixels[i * 3 + 1] = PAppletProxy.saturation(img.pixels[i]) / 255D;
      pixels[i * 3 + 2] = PAppletProxy.brightness(img.pixels[i]) / 255D;
    }
    return new SimpleMatrix(pixels);
  }

  @Override
  public PImage vectorToImage(SimpleMatrix vector) {
    double[] data = vector.getDDRM().getData();
    double size = Math.sqrt(vector.getNumElements() / 3);
    PImage img = createImage((int) Math.ceil(size), (int) Math.floor(size), PApplet.RGB);
    colorMode(PApplet.HSB);
    for (int i = 0; i < img.pixels.length; i++) {
      int hsb = color(
        (float) (data[i * 3] * 255), 
        (float) (data[i * 3 + 1] * 255), 
        (float) (data[i * 3 + 2] * 255));
      img.pixels[i] = (int) red(hsb) << 16 | (int) green(hsb) << 8 | (int) blue(hsb);
    }
    colorMode(PApplet.RGB);
    img.updatePixels();
    return img;
  }
}
