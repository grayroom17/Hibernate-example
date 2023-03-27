package com.example.hibernate.collections_mapping;

import com.example.hibernate.BaseIT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ElementCollectionIT extends BaseIT {

    @Test
    void elementCollection() {
        try (var session = sessionFactory.openSession()) {
            session.beginTransaction();
            var companyId = 1L;
            var company = session.find(CompanyWithCollectionTable.class, companyId);
            company.getLocales().add(LocaleInfo.of("RU", "Описание на русском"));
            company.getLocales().add(LocaleInfo.of("EN", "English description"));
            session.persist(company);
            session.getTransaction().commit();
            session.clear();

            var foundedCompany = session.find(CompanyWithCollectionTable.class, companyId);
            assertNotNull(foundedCompany);
            assertNotNull(foundedCompany.getLocales());
            assertFalse(foundedCompany.getLocales().isEmpty());
        }
    }
}
