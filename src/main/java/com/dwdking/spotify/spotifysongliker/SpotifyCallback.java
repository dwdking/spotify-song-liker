package com.dwdking.spotify.spotifysongliker;

import java.io.IOException;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpotifyCallback {

	@Autowired
	private ApplicationCredentials applicationCredentials;

	@Autowired
	private SpotifyApiWrapper spotifyApiWrapper;
    
    @GetMapping(value = "spotifyCallback")
	public String spotifyCallback(String code) throws ParseException, SpotifyWebApiException, IOException {
		applicationCredentials.setAuthorizationCode(code);

		SpotifyApi spotifyApi = spotifyApiWrapper.spotifyApi();
		AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();

		AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();

		applicationCredentials.setAccessToken(authorizationCodeCredentials.getAccessToken());
		applicationCredentials.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

		spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
        spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

		applicationCredentials.persist();

		return "Authorization provided to shopify";
	}

}
