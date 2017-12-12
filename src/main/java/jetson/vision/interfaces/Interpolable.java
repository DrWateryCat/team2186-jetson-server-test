package jetson.vision.interfaces;

public interface Interpolable<T> {
    T interpolate(T other, double x);
}
