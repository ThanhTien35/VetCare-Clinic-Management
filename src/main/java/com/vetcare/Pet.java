
package com.vetcare;

public class Pet {
    public final int id;
    public final int ownerId;
    public String name;
    public String species;
    public String breed;
    public int age;

    public Pet(int id, int ownerId, String name,
               String species, String breed, int age) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
    }
}
