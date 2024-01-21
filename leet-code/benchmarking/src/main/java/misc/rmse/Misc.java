package misc.rmse;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;

public class Misc {
    public static double rootMeanSquaredError(double[] actual, double[] expected) {
        if (actual.length != expected.length) throw new AssertionError();
        double sum = 0.0;
        for (int i = 0; i < actual.length; i++) {
            double a = actual[i];
            double e = expected[i];

            double diff = a - e;
            double squaredError = diff * diff;
            sum += squaredError;
        }

        double meanSquaredError = sum / actual.length;
        return Math.sqrt(meanSquaredError);
    }

    public static double rmseSimd(double[] actual, double[] expected) {
        if (actual.length != expected.length) throw new AssertionError();
        int n = actual.length;

        int nMod4 = n - (n % 4);
        DoubleVector sums = DoubleVector.zero(DoubleVector.SPECIES_256);

        for (int i = 0; i < nMod4; i += 4) {
            DoubleVector va = DoubleVector.fromArray(DoubleVector.SPECIES_256, actual, i);
            DoubleVector vb = DoubleVector.fromArray(DoubleVector.SPECIES_256, expected, i);

            DoubleVector sub = va.sub(vb);
            DoubleVector mul = sub.mul(sub);

            sums = sums.add(mul);
        }

        double sum = sums.reduceLanes(VectorOperators.ADD);

        for (int i = nMod4; i < n; i++) {
            sum += Math.pow(actual[i] - expected[i], 2);
        }

        return Math.sqrt(sum / n);
    }
}
