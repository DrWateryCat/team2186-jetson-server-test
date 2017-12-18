package jetson.vision.interfaces;

public interface InverseInterpolable<T> {
    double inverseInterpolate(T upper, T lower);
}
