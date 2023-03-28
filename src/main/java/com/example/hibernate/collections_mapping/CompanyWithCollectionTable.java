package com.example.hibernate.collections_mapping;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "company")
public class CompanyWithCollectionTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String name;

    @Builder.Default
    @ElementCollection(targetClass = LocaleInfo.class)
    @CollectionTable(name = "company_locale", joinColumns = @JoinColumn(name = "company_id"))
    List<LocaleInfo> locales = new ArrayList<>();
}
