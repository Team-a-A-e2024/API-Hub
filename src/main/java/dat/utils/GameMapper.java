package dat.utils;

import dat.dtos.GameDTO;
import dat.dtos.IgdbGame;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class GameMapper {
    public static GameDTO MapIgdbGameToGame(IgdbGame igdbGame){
        LocalDate release = null;
        if(igdbGame.getFirstReleaseDate() != null){
            release = Instant.ofEpochSecond(igdbGame.getFirstReleaseDate())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }

        return GameDTO.builder()
                .name(igdbGame.getName())
                .firstReleaseDate(release)
                .summary(igdbGame.getSummary())
                .build();
    }
}
