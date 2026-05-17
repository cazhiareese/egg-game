package com.eggame.scene;

public class AvatarState {
    private int headIndex = 0; // 0 to 3
    private int hatIndex = 0;  // 0 to 3

    public AvatarState() {
        this.headIndex = 0;
        this.hatIndex = 0;
    }

    public AvatarState(int headIndex, int hatIndex) {
        this.headIndex = headIndex;
        this.hatIndex = hatIndex;
    }

    public int getHeadIndex() {
        return headIndex;
    }

    public void setHeadIndex(int headIndex) {
        this.headIndex = headIndex;
    }

    public int getHatIndex() {
        return hatIndex;
    }

    public void setHatIndex(int hatIndex) {
        this.hatIndex = hatIndex;
    }
}
