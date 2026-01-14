package org.sqx.javaaidemo;

public class TestOrt {
    public static void main(String[] args) {
        try (var env = ai.onnxruntime.OrtEnvironment.getEnvironment()) {
            System.out.println("ONNX Runtime 正常加载");
        }
    }
}