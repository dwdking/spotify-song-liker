package com.dwdking.spotify.spotifysongliker;

import java.io.IOException;
import java.net.URI;

import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private SpotifyApiWrapper spotifyApiWrapper;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApiWrapper.spotifyApi().authorizationCodeUri()
            .scope("user-library-read user-read-email user-library-modify playlist-read-private")
            .show_dialog(true)
            .build();
        
        URI uri = authorizationCodeUriRequest.execute();

        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec("xdg-open " + uri.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    

}
