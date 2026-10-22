package com.tenagrim.meshgrouptestassg.entity;

import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 500, nullable = false)
    private String name;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "password", length = 500, nullable = false)
    private String password;

    //connection to account removwd because we dont need it here, and it causes +1 request in search
    // if we will need this connection better to store fk in user, not in account

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmailData> emails = new ArrayList<>();

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhoneData> phones = new ArrayList<>();

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<EmailData> getEmails() { return emails; }
    public void setEmails(List<EmailData> emails) { this.emails = emails; }

    public List<PhoneData> getPhones() { return phones; }
    public void setPhones(List<PhoneData> phones) { this.phones = phones; }
}