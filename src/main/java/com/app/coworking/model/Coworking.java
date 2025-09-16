package com.app.coworking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coworkings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coworking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Coworking name is required")
    @Size(min = 2, max = 100, message = "Coworking name must be between 2 and 100 characters")
    @Column(unique = true, nullable = false)
    private String name;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 200, message = "Address must be between 10 and 200 characters")
    @Column(unique = true, nullable = false)
    private String address;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false) //not unique
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{0,14}$", message = "Invalid phone number format")
    @NotBlank(message = "Phone number is required")
    @Column(name = "phone_number", nullable = false) //not unique
    private String phoneNumber;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "coworking", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Workspace> workspaces = new HashSet<>();
}
