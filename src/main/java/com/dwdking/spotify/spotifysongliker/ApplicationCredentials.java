package com.dwdking.spotify.spotifysongliker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConfigurationProperties("com.dwdking.spotify.credentials")
public class ApplicationCredentials {

    private final static String CLIENT_ID_KEY = "com.dwdking.spotify.credentials.clientId";

    private final static String CLIENT_SECRET_KEY = "com.dwdking.spotify.credentials.clientSecret";

    private final static String AUTHORIZATION_CODE_KEY = "com.dwdking.spotify.credentials.authorizationCode";

    private final static String ACCESS_TOKEN_KEY = "com.dwdking.spotify.credentials.accessToken";

    private final static String REFRESH_TOKEN_KEY = "com.dwdking.spotify.credentials.refreshToken";

    private String clientId;

    private String clientSecret;

    private String authorizationCode;

    private String accessToken;

    private String refreshToken;

    public boolean needsAuthentication() {
        return !StringUtils.hasLength(clientId) &&
            !StringUtils.hasLength(clientSecret) &&
            !StringUtils.hasLength(authorizationCode) &&
            !StringUtils.hasLength(accessToken) &&
            !StringUtils.hasLength(refreshToken);
    }

    public void persist() {
        File authorizationFile = new File(System.getenv("HOME") + "/.spotify-song-liker", "application.properties");
        if (!authorizationFile.exists()) {
            throw new RuntimeException("Authorization file doesn't exist.");
        }

        try (FileWriter writer = new FileWriter(authorizationFile, false)) {
            writer.write(CLIENT_ID_KEY + "=" + clientId + "\n");
            writer.write(CLIENT_SECRET_KEY + "=" + clientSecret + "\n");
            writer.write(AUTHORIZATION_CODE_KEY + "=" + authorizationCode + "\n");
            writer.write(ACCESS_TOKEN_KEY + "=" + accessToken + "\n");
            writer.write(REFRESH_TOKEN_KEY + "=" + refreshToken + "\n");
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save configuration file.", e);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    
}
