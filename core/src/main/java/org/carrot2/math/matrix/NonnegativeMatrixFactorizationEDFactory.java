/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2019, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package org.carrot2.math.matrix;

import org.carrot2.math.mahout.matrix.*;

/** A factory for {@link NonnegativeMatrixFactorizationED}s. */
public class NonnegativeMatrixFactorizationEDFactory extends IterativeMatrixFactorizationFactory {
  public MatrixFactorization factorize(DoubleMatrix2D A) {
    NonnegativeMatrixFactorizationED factorization = new NonnegativeMatrixFactorizationED(A);
    factorization.setK(k);
    factorization.setMaxIterations(maxIterations);
    factorization.setStopThreshold(stopThreshold);
    factorization.setSeedingStrategy(createSeedingStrategy());
    factorization.setOrdered(ordered);

    factorization.compute();

    return factorization;
  }
}