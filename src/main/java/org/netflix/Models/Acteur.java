package org.netflix.Models;

public class Acteur {
    public int id_Acteur;
    public String nom;
    public int age;
    public String acteurImageUrl;
    public Acteur(int id_Acteur, String nom, int age, String acteurImageUrl) {
        this.id_Acteur = id_Acteur;
        this.nom = nom;
        this.age = age;
        this.acteurImageUrl = acteurImageUrl;
    }
    public Acteur(String nom, int age, String acteurImageUrl) {
        this.nom = nom;
        this.age = age;
        this.acteurImageUrl = acteurImageUrl;
    }
    public int getId_Acteur() { return id_Acteur; }
    public String getNom() { return nom; }
    public int getAge() { return age; }
    public String getActeurImageUrl() { return acteurImageUrl; }
    public void setId_Acteur(int id_Acteur) { this.id_Acteur = id_Acteur; }
    public void setNom(String nom) { this.nom = nom; }
    public void setAge(int age) { this.age = age; }
    public void setActeurImageUrl(String acteurImageUrl) { this.acteurImageUrl = acteurImageUrl; }
}
