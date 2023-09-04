package com.givaudan.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.givaudan.domain.Contact} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ContactDTO implements Serializable {

    private Long id;

    private String nom;

    private String prenom;

    private Integer age;

    private String address;

    private Integer codepostal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getCodepostal() {
        return codepostal;
    }

    public void setCodepostal(Integer codepostal) {
        this.codepostal = codepostal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContactDTO)) {
            return false;
        }

        ContactDTO contactDTO = (ContactDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, contactDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ContactDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", prenom='" + getPrenom() + "'" +
            ", age=" + getAge() +
            ", address='" + getAddress() + "'" +
            ", codepostal=" + getCodepostal() +
            "}";
    }
}
