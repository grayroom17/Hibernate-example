package com.example.hibernate.collections_mapping;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Embeddable
public class LocaleInfo implements Comparable<LocaleInfo> {

    @Column(length = 2, columnDefinition = "bpchar")
    String language;

    String description;

    @Override
    public int compareTo(LocaleInfo o) {
        return language.compareTo(o.getLanguage());
    }
}
