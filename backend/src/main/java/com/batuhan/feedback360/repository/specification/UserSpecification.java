package com.batuhan.feedback360.repository.specification;

import com.batuhan.feedback360.model.entitiy.User;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    public static Specification<User> filterUsers(Long companyId, Boolean active, String name) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), companyId));
            }
            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), active));
            }
            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstName")),
                    "%" + name.toLowerCase() + "%"
                ));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) ->
            companyId == null ? null : criteriaBuilder.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<User> hasActiveStatus(Boolean active) {
        return (root, query, criteriaBuilder) ->
            active == null ? null : criteriaBuilder.equal(root.get("active"), active);
    }

    public static Specification<User> hasNameContaining(String name) {
        return (root, query, criteriaBuilder) ->
            StringUtils.hasText(name) ?
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%") :
                null;
    }
}
