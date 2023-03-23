package michid.animation;

/**
 * michid document
 */
public interface Semiring<T> {
    T zero();
    T one();
    T mul(T x, T y);
    T add(T x, T y);
}
