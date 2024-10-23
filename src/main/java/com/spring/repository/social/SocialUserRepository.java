package com.spring.repository.social;

import com.spring.model.social_entity.SocialUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialUserRepository extends JpaRepository<SocialUserEntity, Long> {

    SocialUserEntity findByUsername(String username);
}
