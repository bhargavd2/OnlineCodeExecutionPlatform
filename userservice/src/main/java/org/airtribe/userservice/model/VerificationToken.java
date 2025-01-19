package org.airtribe.userservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String token;

    private Date expirationTime;

    @OneToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(nullable = false, name = "userId")
    private User user;

    @JsonProperty("userId")
    public Long getCreatedById() {
        return user != null ? user.getUserId() : null;
    }

    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expirationTime = calculateExpirationTime();
    }

    public VerificationToken(String token) {
        this.token = token;
        this.expirationTime = calculateExpirationTime();
    }

    public static Date calculateExpirationTime() {
        long expirationTimeInMinutes = 10;
        long expirationTimeInMilliseconds = expirationTimeInMinutes * 60 * 1000;
        return new Date(System.currentTimeMillis() + expirationTimeInMilliseconds);
    }

}


