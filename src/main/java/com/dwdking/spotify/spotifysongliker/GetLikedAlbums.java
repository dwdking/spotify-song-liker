package com.dwdking.spotify.spotifysongliker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.SavedAlbum;
import com.wrapper.spotify.model_objects.specification.SavedTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.model_objects.specification.User;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import com.wrapper.spotify.requests.data.library.GetUsersSavedTracksRequest;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetLikedAlbums {

    private final Logger logger = LoggerFactory.getLogger(GetLikedAlbums.class);

    @Autowired
    private SpotifyApiWrapper spotifyApiWrapper;

    @GetMapping(value = "getPlayLists")
    public String getPlayLists() throws ParseException, SpotifyWebApiException, IOException {
        SpotifyApi spotifyApi = spotifyApiWrapper.spotifyApi();
        User user = spotifyApi.getCurrentUsersProfile().build().execute();

        Paging<PlaylistSimplified> pagedPlaylists = spotifyApi.getListOfUsersPlaylists(user.getId()).build().execute();

        StringBuilder builder = new StringBuilder();
        for (PlaylistSimplified playlistSimplified : pagedPlaylists.getItems()) {
            builder.append("Id: ").append(playlistSimplified.getId()).append(" --- name: ").append(playlistSimplified.getName()).append("<br/>");
        }

        return builder.toString();
    }

    @GetMapping(value = "likeOneSong")
    public String likeSongs(List<String> songIds) throws ParseException, SpotifyWebApiException, IOException {
        List<List<String>> partitions = Lists.partition(songIds, 50);
        int counter = 1;
        for (List<String> partitionedSongIds : Lists.partition(songIds, 50)) {
            logger.info("Liking songs in partition " + counter + " of " + partitions.size());
            String[] songIdArray = new String[partitionedSongIds.size()];
            songIdArray = partitionedSongIds.toArray(songIdArray);
            spotifyApiWrapper.spotifyApi().saveTracksForUser(songIdArray).build().execute();
            counter++;
        }
        return "";
    }

    @GetMapping(value = "allLikedSongs")
    public List<String> allLikedSongs() throws ParseException, SpotifyWebApiException, IOException {
        List<SavedTrack> savedTracks = new ArrayList<>();
        GetUsersSavedTracksRequest.Builder builder = spotifyApiWrapper.spotifyApi().getUsersSavedTracks();
        Paging<SavedTrack> pagedSavedTracks = builder.build().execute();
        savedTracks.addAll(Arrays.asList(pagedSavedTracks.getItems()));

        while (pagedSavedTracks.getTotal() > savedTracks.size()) {
            logger.info("Requesting next set of songs");
            pagedSavedTracks = builder.offset(savedTracks.size()).build().execute();
            logger.info("Finished request");
            savedTracks.addAll(Arrays.asList(pagedSavedTracks.getItems()));
        }

        return savedTracks.stream().map(SavedTrack::getTrack).map(Track::getId).collect(Collectors.toList());
    }
    
    @GetMapping(value = "likeAllAlbumSongs")
    public String likeAllAlbumSongs() throws ParseException, SpotifyWebApiException, IOException {
        List<SavedAlbum> allSavedAlbums = new ArrayList<>();
        GetCurrentUsersSavedAlbumsRequest.Builder requestBuilder = spotifyApiWrapper.spotifyApi().getCurrentUsersSavedAlbums();
        Paging<SavedAlbum> currentPage = requestBuilder.build().execute();
        allSavedAlbums.addAll(Arrays.asList(currentPage.getItems()));
        
        while (currentPage.getTotal() > allSavedAlbums.size()) {
            logger.info("Requesting next set of albums");
            currentPage = requestBuilder.offset(allSavedAlbums.size()).build().execute();
            logger.info("Finished request");
            allSavedAlbums.addAll(Arrays.asList(currentPage.getItems()));
        }

        List<TrackSimplified> allAlbumTracks = new ArrayList<>();
        Map<SavedAlbum, List<TrackSimplified>> albumsWithTracks = new HashMap<>();

        for (SavedAlbum savedAlbum : allSavedAlbums) {
            logger.info("Requesting tracks for album " + savedAlbum.getAlbum().getName());
            List<TrackSimplified> albumTracks = new ArrayList<>();

            GetAlbumsTracksRequest.Builder trackRequestBuilder = spotifyApiWrapper.spotifyApi().getAlbumsTracks(savedAlbum.getAlbum().getId());
            Paging<TrackSimplified> pagedTracks = trackRequestBuilder.build().execute();
            albumTracks.addAll(Arrays.asList(pagedTracks.getItems()));

            while (pagedTracks.getTotal() > albumTracks.size()) {
                logger.info("Requesting next set of tracks");
                pagedTracks = trackRequestBuilder.offset(albumTracks.size()).build().execute();
                logger.info("Finished request");
                albumTracks.addAll(Arrays.asList(pagedTracks.getItems()));
            }

            albumsWithTracks.put(savedAlbum, albumTracks);
            allAlbumTracks.addAll(albumTracks);
            logger.info("Finished requesting tracks for album " + savedAlbum.getAlbum().getName());
        }

        List<String> allLikedSongIds = allLikedSongs();
        List<String> reducedSongIdsToLike = new ArrayList<>();

        for (TrackSimplified track : allAlbumTracks) {
            if (!allLikedSongIds.contains(track.getId())) {
                reducedSongIdsToLike.add(track.getId());
            }
        }

        likeSongs(reducedSongIdsToLike);

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<SavedAlbum, List<TrackSimplified>> albumTracks : albumsWithTracks.entrySet()) {
            builder.append(albumTracks.getKey().getAlbum().getName()).append("<br/>");
            for (TrackSimplified track : albumTracks.getValue()) {
                builder.append("--->>> id: ").append(track.getId()).append(" --- ").append(track.getName()).append("<br/>");
            }
        }

        return builder.toString();
    }

}
