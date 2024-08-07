package com.github.huymaster.ppasm.core;

import java.util.Arrays;

public enum Gender {
    Male(0),
    Female(1),
    Other(-1);

    final int id;

    private Gender(int i) {
        id = i;
    }

    public static Gender getGender(int i) {
        return Arrays.stream(Gender.values()).filter(gender -> gender.id == i).findFirst().orElse(Gender.Other);
    }
}
