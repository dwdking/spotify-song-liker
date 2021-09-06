package com.dwdking.spotify.spotifysongliker;

import javax.annotation.PostConstruct;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpotifyApiWrapper {

    private SpotifyApi spotifyApi;

    @Autowired
    private ApplicationCredentials applicationCredentials;

    @PostConstruct
    public void after() {
        spotifyApi = new SpotifyApi.Builder()
            .setClientId(applicationCredentials.getClientId())
            .setClientSecret(applicationCredentials.getClientSecret())
            .setAccessToken(applicationCredentials.getAccessToken())
            .setRefreshToken(applicationCredentials.getRefreshToken())
            .setRedirectUri(SpotifyHttpManager.makeUri("http://localhost:8080/spotifyCallback"))
            .build();

        spotifyApi.authorizationCode(applicationCredentials.getAuthorizationCode());
    }

    public SpotifyApi spotifyApi() {
        return spotifyApi;
    }

}
