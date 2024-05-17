package com.softwareEngineeringLab1.structure;

public class Pair {
    public Integer idx;
    public Integer weight;

    public Pair(Integer idx) {
        this.idx = idx;
        this.weight = 1;
    }

    public Pair(Integer idx,Integer weight) {
        this.idx = idx;
        this.weight = 1;
    }


    public Integer getIdx() {
        return idx;
    }


    public Integer getWeight() {
        return weight;
    }

    public void increaseWeight() {
        this.weight = weight + 1;
    }
}
