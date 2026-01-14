package org.sqx.javaaidemo.sdk;

import ai.onnxruntime.*;
import org.sqx.javaaidemo.sdk.model.PredictionResult;
import org.sqx.javaaidemo.sdk.model.SDKConfig;
import org.sqx.javaaidemo.sdk.utils.ImagePreprocessor;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AISDK {
    private OrtEnvironment env;
    private OrtSession session;
    private final ImagePreprocessor preprocessor;
    private final SDKConfig config;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private AISDK(SDKConfig config) {
        this.config = config;
        this.preprocessor = new ImagePreprocessor();
    }

    public static AISDK initialize(SDKConfig config) throws Exception {
        AISDK sdk = new AISDK(config);
        sdk.loadModel();
        return sdk;
    }

    private void loadModel() throws Exception {
        lock.writeLock().lock();
        try {
            env = OrtEnvironment.getEnvironment();
            try (InputStream modelStream = config.getModelStream()) {
                byte[] modelBytes = modelStream.readAllBytes();
                session = env.createSession(modelBytes, new OrtSession.SessionOptions());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public PredictionResult predict(BufferedImage image) throws Exception {
        long start = System.currentTimeMillis();

        float[] inputData = preprocessor.preprocess(image);
        float[][][][] inputArray = new float[1][3][224][224];
        for (int c = 0; c < 3; c++) {
            for (int h = 0; h < 224; h++) {
                for (int w = 0; w < 224; w++) {
                    inputArray[0][c][h][w] = inputData[c * 224 * 224 + h * 224 + w];
                }
            }
        }

        lock.readLock().lock();
        try {
            try (OnnxTensor tensor = OnnxTensor.createTensor(env, inputArray)) {
                try (OrtSession.Result results = session.run(Collections.singletonMap("data", tensor))) {
                    float[][] output = (float[][]) results.get(0).getValue();
                    float[] logits = output[0];

                    float[] probabilities = softmax(logits);

                    int maxIndex = 0;
                    float maxProb = probabilities[0];
                    for (int i = 1; i < probabilities.length; i++) {
                        if (probabilities[i] > maxProb) {
                            maxProb = probabilities[i];
                            maxIndex = i;
                        }
                    }

                    long end = System.currentTimeMillis();
                    PredictionResult result = new PredictionResult();
                    result.setClassId(maxIndex);
                    result.setScore(maxProb);
                    result.setLatencyMs(end - start);

                    return result;
                }
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    private float[] softmax(float[] logits) {
        float max = logits[0];
        for (float v : logits) {
            if (v > max) max = v;
        }

        float sum = 0.0f;
        float[] exps = new float[logits.length];
        for (int i = 0; i < logits.length; i++) {
            exps[i] = (float) Math.exp(logits[i] - max);
            sum += exps[i];
        }

        for (int i = 0; i < exps.length; i++) {
            exps[i] /= sum;
        }
        return exps;
    }

    public void close() throws Exception {
        lock.writeLock().lock();
        try {
            if (session != null) {
                session.close();
                session = null;
            }
            if (env != null) {
                env.close();
                env = null;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}