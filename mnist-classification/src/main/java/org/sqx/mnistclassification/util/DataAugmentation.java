package org.sqx.mnistclassification.util;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.Random;

public class DataAugmentation {

    private static final Random random = new Random();

    // 添加随机噪声
    public static INDArray addNoise(INDArray input, double noiseLevel) {
        INDArray noise = Nd4j.randn(input.shape()).mul(noiseLevel);
        return input.add(noise);
    }

    // 随机缩放
    public static INDArray randomScale(INDArray input, double minScale, double maxScale) {
        double scale = minScale + random.nextDouble() * (maxScale - minScale);
        return input.mul(scale);
    }

    // 随机平移
    public static INDArray randomTranslate(INDArray input, int maxPixels) {
        // 将1D数组转换为2D图像
        INDArray image = input.reshape(28, 28);
        INDArray translated = Nd4j.zeros(28, 28);

        int dx = random.nextInt(maxPixels * 2 + 1) - maxPixels;
        int dy = random.nextInt(maxPixels * 2 + 1) - maxPixels;

        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                int newX = x + dx;
                int newY = y + dy;

                if (newX >= 0 && newX < 28 && newY >= 0 && newY < 28) {
                    translated.putScalar(newY, newX, image.getDouble(y, x));
                }
            }
        }

        return translated.reshape(1, 28 * 28);
    }

    // 随机旋转（小角度）
    public static INDArray randomRotate(INDArray input, double maxAngle) {
        // 简化版旋转 - 实际实现可能需要更复杂的插值
        double angle = random.nextDouble() * maxAngle * 2 - maxAngle;
        double cos = Math.cos(Math.toRadians(angle));
        double sin = Math.sin(Math.toRadians(angle));

        INDArray image = input.reshape(28, 28);
        INDArray rotated = Nd4j.zeros(28, 28);

        for (int y = 0; y < 28; y++) {
            for (int x = 0; x < 28; x++) {
                // 绕中心旋转
                double cx = x - 13.5;
                double cy = y - 13.5;

                double newX = cx * cos - cy * sin + 13.5;
                double newY = cx * sin + cy * cos + 13.5;

                if (newX >= 0 && newX < 28 && newY >= 0 && newY < 28) {
                    rotated.putScalar((int)newY, (int)newX, image.getDouble(y, x));
                }
            }
        }

        return rotated.reshape(1, 28 * 28);
    }
}