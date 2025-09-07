package com.pars.financial.entity;

import com.pars.financial.enums.PhoneNumberType;
import jakarta.persistence.*;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_phoneNumber",
                        columnNames = {"number"}
                )
        }
)
public class PhoneNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String number;
    private PhoneNumberType type;

    public PhoneNumberType getType() {
        return type;
    }

    public void setType(PhoneNumberType type) {
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
