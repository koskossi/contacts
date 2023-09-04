package com.givaudan.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.givaudan.domain.Contact} entity. This class is used
 * in {@link com.givaudan.web.rest.ContactResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /contacts?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ContactCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter nom;

    private StringFilter prenom;

    private IntegerFilter age;

    private StringFilter address;

    private IntegerFilter codepostal;

    private Boolean distinct;

    public ContactCriteria() {}

    public ContactCriteria(ContactCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.nom = other.nom == null ? null : other.nom.copy();
        this.prenom = other.prenom == null ? null : other.prenom.copy();
        this.age = other.age == null ? null : other.age.copy();
        this.address = other.address == null ? null : other.address.copy();
        this.codepostal = other.codepostal == null ? null : other.codepostal.copy();
        this.distinct = other.distinct;
    }

    @Override
    public ContactCriteria copy() {
        return new ContactCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getNom() {
        return nom;
    }

    public StringFilter nom() {
        if (nom == null) {
            nom = new StringFilter();
        }
        return nom;
    }

    public void setNom(StringFilter nom) {
        this.nom = nom;
    }

    public StringFilter getPrenom() {
        return prenom;
    }

    public StringFilter prenom() {
        if (prenom == null) {
            prenom = new StringFilter();
        }
        return prenom;
    }

    public void setPrenom(StringFilter prenom) {
        this.prenom = prenom;
    }

    public IntegerFilter getAge() {
        return age;
    }

    public IntegerFilter age() {
        if (age == null) {
            age = new IntegerFilter();
        }
        return age;
    }

    public void setAge(IntegerFilter age) {
        this.age = age;
    }

    public StringFilter getAddress() {
        return address;
    }

    public StringFilter address() {
        if (address == null) {
            address = new StringFilter();
        }
        return address;
    }

    public void setAddress(StringFilter address) {
        this.address = address;
    }

    public IntegerFilter getCodepostal() {
        return codepostal;
    }

    public IntegerFilter codepostal() {
        if (codepostal == null) {
            codepostal = new IntegerFilter();
        }
        return codepostal;
    }

    public void setCodepostal(IntegerFilter codepostal) {
        this.codepostal = codepostal;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ContactCriteria that = (ContactCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(nom, that.nom) &&
            Objects.equals(prenom, that.prenom) &&
            Objects.equals(age, that.age) &&
            Objects.equals(address, that.address) &&
            Objects.equals(codepostal, that.codepostal) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, age, address, codepostal, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ContactCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (nom != null ? "nom=" + nom + ", " : "") +
            (prenom != null ? "prenom=" + prenom + ", " : "") +
            (age != null ? "age=" + age + ", " : "") +
            (address != null ? "address=" + address + ", " : "") +
            (codepostal != null ? "codepostal=" + codepostal + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
