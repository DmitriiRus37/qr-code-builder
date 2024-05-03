package educational.dmitriigurylev.encoders;

public interface Encoder {
    String[] transformToBinaryArray();
    Encoder setValueToTransform(Object ob);
}
