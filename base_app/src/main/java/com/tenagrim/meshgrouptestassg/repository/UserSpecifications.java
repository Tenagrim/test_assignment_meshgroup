package com.tenagrim.meshgrouptestassg.repository;

import com.tenagrim.meshgrouptestassg.entity.EmailData;
import com.tenagrim.meshgrouptestassg.entity.PhoneData;
import com.tenagrim.meshgrouptestassg.entity.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {}

    public static Specification<User> withFilters(LocalDate dateOfBirth,
                                                  String phone,
                                                  String name,
                                                  String email) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dateOfBirth != null) {
                predicates.add(cb.greaterThan(root.get("dateOfBirth"), dateOfBirth));
            }

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(root.get("name"), name + "%"));
            }

            if (phone != null && !phone.isBlank()) {
                Join<User, PhoneData> phoneJoin = root.join("phones", JoinType.INNER);
                predicates.add(cb.equal(phoneJoin.get("phone"), phone));
            }

            if (email != null && !email.isBlank()) {
                Join<User, EmailData> emailJoin = root.join("emails", JoinType.INNER);
                predicates.add(cb.equal(emailJoin.get("email"), email));
            }

            query.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
