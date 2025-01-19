package org.airtribe.userservice.repository;

import org.airtribe.userservice.dto.CodeSnippetProjection;
import org.airtribe.userservice.model.CodeSnippet;
import org.airtribe.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeRepository extends JpaRepository<CodeSnippet, Long> {
    List<CodeSnippetProjection> findByUser(User user);
}

