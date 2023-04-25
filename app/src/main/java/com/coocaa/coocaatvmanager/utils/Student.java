package com.coocaa.coocaatvmanager.utils;

/**
 * Created by Chengrui on 2015/6/29.
 * @Description 配合测试类
 */
public class Student {

    private int id;
    private String name;
    private int score;

    public int getId() {
        return id;
    }

    public Student setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Student setName(String name) {
        this.name = name;
        return this;
    }

    public int getScore() {
        return score;
    }

    public Student setScore(int score) {
        this.score = score;
        return this;
    }
}
