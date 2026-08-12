package com.vn.smart_space.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.vn.smart_space.model.InvalidatedToken;

@Repository
public interface InvalidatedTokenRepository extends CrudRepository<InvalidatedToken, String> {

}
