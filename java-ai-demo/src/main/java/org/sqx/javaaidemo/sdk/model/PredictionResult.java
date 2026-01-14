package org.sqx.javaaidemo.sdk.model;

public class PredictionResult {
    private int classId;
    private float score;
    private long latencyMs;

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    @Override
    public String toString() {
        return "PredictionResult{" +
                "classId=" + classId +
                ", score=" + score +
                ", latencyMs=" + latencyMs +
                '}';
    }
}