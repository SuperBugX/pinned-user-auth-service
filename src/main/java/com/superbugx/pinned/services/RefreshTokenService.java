package com.superbugx.pinned.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.superbugx.pinned.database.repository.RefreshTokenRepository;
import com.superbugx.pinned.interfaces.services.IRefreshTokenService;
import com.superbugx.pinned.models.RefreshToken;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RefreshTokenService implements IRefreshTokenService {

	@Autowired
	private RefreshTokenRepository refreshTokenrepo;

	@Override
	public String save(RefreshToken refreshToken) {
		log.debug("Refresh Token Service: Attempt Save");
		return refreshTokenrepo.save(refreshToken).getId();
	}

	@Override
	public boolean deleteById(String id) {
		log.debug("Refresh Token Service: Attempt Delete By ID");
		refreshTokenrepo.deleteById(id);
		return true;
	}

	@Override
	public boolean deleteByUserId(String id) {
		log.debug("Refresh Token Service: Attempt Delete By Owner ID");
		refreshTokenrepo.deleteByOwner_Id(id);
		return true;
	}

	@Override
	public boolean exists(String id) {
		log.debug("Refresh Token Service: Attempt Exists");
		return refreshTokenrepo.existsById(id);
	}
}
