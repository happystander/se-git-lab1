package com.softwareEngineeringLab1.structure;

import java.util.*;

public class DirectedMap {
    public ArrayList<HashSet<Integer>> map;//[set(),set()]

    public ArrayList<ArrayList<Pair>> edge;


    public int nodeNumber;
    private Map<String, String> exchangeDict;
    public Map<String, Integer> dict; //存储下标
    public HashSet<String> wordSet; // 存储word的集合

    public DirectedMap() {
        wordSet = new HashSet<>();
        map = new ArrayList<>();
        nodeNumber = 0;
        dict = new HashMap<>();
        edge = new ArrayList<>();
    }
    public Map<String, String> getExchangeDict() {
        return exchangeDict;
    }

    public void setExchangeDict(Map<String, String> exchangeDict) {
        this.exchangeDict = exchangeDict;
    }
}

