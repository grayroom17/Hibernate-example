package com.example.hibernate.entity;

import com.example.hibernate.validation.ForGroupValidationTest;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfile;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NamedEntityGraph(name = "graphWithAllFields",
        attributeNodes = {
                @NamedAttributeNode(value = "company"),
                @NamedAttributeNode(value = "payments"),
                @NamedAttributeNode(value = "userTeams", subgraph = "graphWithTeams"),
                @NamedAttributeNode(value = "profile")
        },
        subgraphs = {
                @NamedSubgraph(name = "graphWithTeams",
                        attributeNodes = {
                                @NamedAttributeNode(value = "team")
                        })
        })

@NamedEntityGraph(name = "graphWithCompanyAndProfile",
        attributeNodes = {
                @NamedAttributeNode(value = "company"),
                @NamedAttributeNode(value = "profile")
        })
@FetchProfile(name = "withCompanyAndPayments",
        fetchOverrides = {
                @FetchProfile.FetchOverride(entity = User.class,
                        association = "company",
                        mode = FetchMode.JOIN),
                @FetchProfile.FetchOverride(entity = User.class,
                        association = "payments",
                        mode = FetchMode.JOIN)
        })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Audited
@Table(name = "users")
public class User extends BaseEntity<Long> {

    @Column(unique = true)
    @NotNull
    String username;

    @Embedded
    PersonalInfo personalInfo;

    @Enumerated(EnumType.STRING)
    @NotNull
    Role role;

    @NotAudited
    @Type(JsonBinaryType.class)
    String info;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    Company company;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Profile profile;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @NotAudited
    @OneToMany(mappedBy = "user")
    List<UserTeam> userTeams = new ArrayList<>();

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OneToMany(mappedBy = "receiver")
    Set<Payment> payments = new HashSet<>();

    public void addPayment(Payment payment) {
        getPayments().add(payment);
        payment.setReceiver(this);
    }
}