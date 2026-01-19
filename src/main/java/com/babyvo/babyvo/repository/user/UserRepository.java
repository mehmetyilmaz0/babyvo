package com.babyvo.babyvo.repository.user;

import com.babyvo.babyvo.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> { }