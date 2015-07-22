package com.example.henryforce.demoble;

/**
 * Created by Henryforce on 1/20/15.
 */
public interface BLEDataCallback {
    public void writeData(byte[] data);
    public void writeRGBColor(int red, int green, int blue);
    public void writeWarmColor(int warm);
    public void writeAnimation(int animationIndex, int velocity);
    public void writeState(boolean state);
}
