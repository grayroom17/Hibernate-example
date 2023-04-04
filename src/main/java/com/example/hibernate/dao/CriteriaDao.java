package com.example.hibernate.dao;

import com.example.hibernate.dto.CompanyDto;
import com.example.hibernate.entity.*;
import jakarta.persistence.Tuple;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.Session;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CriteriaDao {

    private static final CriteriaDao INSTANCE = new CriteriaDao();

    public List<User> findAll(Session session) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(User.class);
        var user = criteria.from(User.class);
        criteria.select(user);
        return session.createQuery(criteria).list();
    }

    public List<User> findAllByFirstName(Session session, String firstName) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(User.class);
        var user = criteria.from(User.class);
        criteria.select(user).where(builder.equal(user.get(User_.personalInfo).get(PersonalInfo_.FIRSTNAME), firstName));
        return session.createQuery(criteria).list();
    }

    public List<User> findLimitedUsersOrderedByFirstname(Session session, int limit) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(User.class);
        var user = criteria.from(User.class);
        criteria.select(user).orderBy(builder.asc(user.get(User_.personalInfo).get(PersonalInfo_.FIRSTNAME)));
        return session.createQuery(criteria).setMaxResults(limit).list();
    }

    public List<User> findAllByCompanyName(Session session, String companyName) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(User.class);
        var company = criteria.from(Company.class);
        var users = company.join(Company_.users);
        criteria.select(users).where(builder.equal(company.get(Company_.NAME), companyName));
        return session.createQuery(criteria).list();
    }

    public List<Payment> findAllPaymentsByCompanyName(Session session, String companyName) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(Payment.class);
        var payment = criteria.from(Payment.class);
        var receiver = payment.join(Payment_.receiver);
        criteria.select(payment)
                .where(builder.equal(receiver.get(User_.COMPANY).get(Company_.NAME), companyName))
                .orderBy(builder.asc(receiver.get(User_.PERSONAL_INFO).get(PersonalInfo_.FIRSTNAME)),
                        builder.asc(payment.get(Payment_.AMOUNT)));
        return session.createQuery(criteria).list();
    }

    public Double findAveragePaymentAmountByFirstAndLastNames(Session session, String firstName, String lastName) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(Double.class);
        var payment = criteria.from(Payment.class);
        var receiver = payment.join(Payment_.RECEIVER);
        criteria.select(builder.avg(payment.get(Payment_.AMOUNT)))
                .where(
                        builder.equal(receiver.get(User_.PERSONAL_INFO).get(PersonalInfo_.FIRSTNAME), firstName),
                        builder.and(),
                        builder.equal(receiver.get(User_.PERSONAL_INFO).get(PersonalInfo_.LASTNAME), lastName));

        return session.createQuery(criteria).uniqueResult();
    }

    public List<Object[]> findCompanyNamesWithAvgUserPaymentsOrderedByCompanyName(Session session) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(Object[].class);
        var company = criteria.from(Company.class);
        var users = company.join(Company_.users);
        var payment = users.join(User_.payments);
        criteria.multiselect(
                        company.get(Company_.NAME),
                        builder.avg(payment.get(Payment_.AMOUNT)))
                .groupBy(company.get(Company_.NAME))
                .orderBy(builder.asc(company.get(Company_.NAME)));
        return session.createQuery(criteria).list();
    }

    public List<CompanyDto> findCompanyNamesWithAvgUserPaymentsOrderedByCompanyNameAndGetDto(Session session) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(CompanyDto.class);
        var company = criteria.from(Company.class);
        var users = company.join(Company_.users);
        var payment = users.join(User_.payments);

        criteria.multiselect(
                        company.get(Company_.name),
                        builder.avg(payment.get(Payment_.amount))
                )
                .groupBy(company.get(Company_.NAME))
                .orderBy(builder.asc(company.get(Company_.NAME)));

        return session.createQuery(criteria).list();
    }

    public List<Tuple> isItPossible(Session session) {
        var builder = session.getCriteriaBuilder();
        var criteria = builder.createQuery(Tuple.class);
        var users = criteria.from(User.class);
        var payment = users.join(User_.payments);

        var subQuery = criteria.subquery(Double.class);
        var paymentSubQuery = subQuery.from(Payment.class);

        criteria.multiselect(
                        users,
                        builder.avg(payment.get(Payment_.AMOUNT)))
                .groupBy(users.get(BaseEntity_.ID))
                .having(builder.gt(
                        builder.avg(payment.get(Payment_.AMOUNT)),
                        subQuery.select(builder.avg(paymentSubQuery.get(Payment_.AMOUNT)))
                ))
                .orderBy(builder.asc(users.get(User_.PERSONAL_INFO).get(PersonalInfo_.FIRSTNAME)));

        return session.createQuery(criteria).list();
    }


    public static CriteriaDao getInstance() {
        return INSTANCE;
    }
}