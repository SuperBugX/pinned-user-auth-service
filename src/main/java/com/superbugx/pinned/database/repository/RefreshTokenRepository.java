package com.superbugx.pinned.database.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.superbugx.pinned.models.RefreshToken;
import com.superbugx.pinned.models.User;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
	//Delete refresh token based on its users id
    void deleteByOwner_Id(ObjectId id);
    default void deleteByOwner_Id(String id) {
        deleteByOwner_Id(new ObjectId(id));
    };
}
