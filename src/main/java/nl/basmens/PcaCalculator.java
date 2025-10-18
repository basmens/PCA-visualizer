package nl.basmens;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;

public class PcaCalculator {
  private final SimpleMatrix dataMatrix;
  private final int featureCount;
  private final int sampleCount;

  private SimpleMatrix mean;
  private SimpleMatrix variance;
  private SimpleMatrix standardDeviation;
  private SimpleMatrix covarianceMatrix;

  private double[] eigenvalues;
  private SimpleMatrix[] eigenvectors;

  public PcaCalculator(SimpleMatrix dataMatrix) {
    this.dataMatrix = dataMatrix;
    this.featureCount = dataMatrix.getNumRows();
    this.sampleCount = dataMatrix.getNumCols();
    calculatePca();
  }

  public void calculatePca() {
    // Extract columns for easier processing
    SimpleMatrix[] cols = new SimpleMatrix[sampleCount];
    for (int i = 0; i < sampleCount; i++) {
      cols[i] = dataMatrix.extractVector(false, i);
    }

    // Calculate mean
    mean = new SimpleMatrix(featureCount, 1);
    for (int i = 0; i < sampleCount; i++) {
      mean = mean.plus(cols[i]);
    }
    mean = mean.divide(sampleCount);

    // Calculate variance and standard deviation, and subtract mean from columns
    variance = new SimpleMatrix(featureCount, 1);
    for (int i = 0; i < sampleCount; i++) {
      cols[i] = cols[i].minus(mean);
      variance = variance.plus(cols[i].elementPower(2));
    }
    variance = variance.divide(sampleCount - 1);
    standardDeviation = variance.elementPower(0.5);

    // Calculate covariance matrix by normalizing data to 0 mean and unit variance first,
    // and then multiplying the normalized data matrix by its transpose
    SimpleMatrix sdNoZeros = new SimpleMatrix(
      Arrays.stream(standardDeviation.getDDRM().data)
        .map(d -> d < 1e-20D ? 1 : d)
        .toArray());
    covarianceMatrix = new SimpleMatrix(featureCount, 0);
    for (int i = 0; i < sampleCount; i++) {
      cols[i] = cols[i].elementDiv(sdNoZeros);
    }
    covarianceMatrix = covarianceMatrix.concatColumns(cols);
    covarianceMatrix = covarianceMatrix.mult(covarianceMatrix.transpose());

    // Calculate eigenvalues and eigenvectors
    SimpleEVD<SimpleMatrix> eigenDecomposition = covarianceMatrix.eig();
    List<EigenPair> eigenPairs = IntStream.range(0, eigenDecomposition.getNumberOfEigenvalues())
      .filter(i -> eigenDecomposition.getEigenvalue(i).real > 0)
      .mapToObj(i -> new EigenPair(eigenDecomposition.getEigenvalue(i).real, eigenDecomposition.getEigenVector(i)))
      .sorted()
      .toList();
    eigenvalues = eigenPairs.stream().mapToDouble(ep -> ep.eigenvalue).toArray();
    eigenvectors = eigenPairs.stream().map(ep -> ep.eigenvector).toArray(SimpleMatrix[]::new);
  }

  public SimpleMatrix reconstructInDimensions(SimpleMatrix data, int dimensions, boolean adjustForMean) {
    // Project data into PCA subspace and back
    SimpleMatrix eigenMatrix = eigenvectors[0].concatColumns(Arrays.copyOfRange(eigenvectors, 1, dimensions));
    SimpleMatrix projected = data.transpose().mult(eigenMatrix);
    SimpleMatrix recovered = projected.mult(eigenMatrix.transpose()).transpose();
    if (!adjustForMean) return recovered;

    // Adjust for mean
    SimpleMatrix projectedMean = mean.transpose().mult(eigenMatrix);
    SimpleMatrix recoveredMean = projectedMean.mult(eigenMatrix.transpose()).transpose();
    SimpleMatrix orthogonalMean = mean.minus(recoveredMean);
    return recovered.plus(orthogonalMean);
  }

  private record EigenPair(double eigenvalue, SimpleMatrix eigenvector) implements Comparable<EigenPair> {
    @Override
    public int compareTo(EigenPair o) {
      return Double.compare(o.eigenvalue, this.eigenvalue);
    }
  }

  public int getFeatureCount() {
    return featureCount;
  }

  public int getSampleCount() {
    return sampleCount;
  }

  public SimpleMatrix getMean() {
    return mean;
  }

  public SimpleMatrix getVariance() {
    return variance;
  }

  public SimpleMatrix getStandardDeviation() {
    return standardDeviation;
  }

  public SimpleMatrix getCovarianceMatrix() {
    return covarianceMatrix;
  }

  public double[] getEigenvalues() {
    return eigenvalues;
  }

  public SimpleMatrix[] getEigenvectors() {
    return eigenvectors;
  }

  public int getIntrinsicDimensionality() {
    return eigenvalues.length;
  }
}
