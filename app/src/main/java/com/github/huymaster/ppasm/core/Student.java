package com.github.huymaster.ppasm.core;

public class Student {
    private final String id;
    private final String name;
    private final int age;
    private final String email;
    private final String phone;
    private final Gender gender;
    private final float grade;

    public Student(String id, String name, int age, String email, String phone, Gender gender, float grade) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.grade = grade;
    }

    public Student(String id, String name, int age, String email, String phone, Gender gender) {
        this(id, name, age, email, phone, gender, -1);
    }

    public float getGrade() {
        return grade;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("id=%s, name=%s, age=%d, email=%s, phone=%s, gender=%s grade=%.2f", id, name, age, email, phone, gender, grade);
    }
}
