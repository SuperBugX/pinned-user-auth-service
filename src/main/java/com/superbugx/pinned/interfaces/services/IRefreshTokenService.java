package com.superbugx.pinned.interfaces.services;

import com.superbugx.pinned.models.RefreshToken;

public interface IRefreshTokenService {

	/**
	 * Store refresh token in storage
	 * 
	 * @param refreshToken - Token to be saved
	 * @return ID of stored token
	 */
	public String save(RefreshToken refreshToken);

	/**
	 * Delete stored refresh token
	 * 
	 * @param id - ID of refresh token
	 * @return boolean for success
	 */
	public boolean deleteById(String id);
	
	/**
	 * Delete stored refresh token based on its users ID
	 * 
	 * @param id - ID of user owner
	 * @return boolean for success
	 */
	public boolean deleteByUserId(String id);

	/**
	 * Check if refresh token exists in storage
	 * 
	 * @param id - ID of refresh token
	 * @return true if it exists
	 */
	public boolean exists(String id);
}
