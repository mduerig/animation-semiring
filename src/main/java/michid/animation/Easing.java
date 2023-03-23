package michid.animation;

import static java.lang.Math.exp;

import java.util.function.Function;

public final class Easing {

    public static Function<Double, Double> easeOut(double c) {
        return easeIn(1 / c);
    }

    public static Function<Double, Double> easeIn(double c) {
        return t -> 1 - Math.pow(1 - t, 1/c);
    }

    public static Function<Double, Double> easeInOut(double c) {
        return t -> expInv(c, t) / (expInv(c, t) + expInv(c, 1.0 - t));
    }

    private static double expInv(double c, double t) {
        return t <= 0 ? 0 : exp(-c / t);
    }

}
