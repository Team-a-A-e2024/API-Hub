package dat.Service;

import dat.dtos.GameDTO;
import dat.dtos.IgdbGame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {
    @Mock
    private FetchTools fetchTools;

    @InjectMocks
    private GameService gameService;

    @Test
    void fetchPageOfGames() {
        // Arrange
        IgdbGame[] expected = new IgdbGame[]{new IgdbGame(), new IgdbGame()};
        when(fetchTools.postToApi(
                        anyString(),
                        eq(IgdbGame[].class),
                        any(HttpRequest.BodyPublisher.class),
                        any(String[].class)
                )
        ).thenReturn(expected);

        when(fetchTools.postToApi(
                anyString(),
                eq(IgdbAccessTokenService.Token.class)
        )).thenReturn(IgdbAccessTokenService.Token.builder().expires_in(0L).createdAt(LocalDateTime.now()).build());

        // Act
        IgdbGame[] actual = gameService.fetchPageOfGames(0, 0L);

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void fetchCountOfGames() {
        // Arrange
        GameService.IgdbCount expected = new GameService.IgdbCount();
        when(fetchTools.postToApi(
                        anyString(),
                        eq(GameService.IgdbCount.class),
                        any(HttpRequest.BodyPublisher.class),
                        any(String[].class)
                )
        ).thenReturn(expected);

        when(fetchTools.postToApi(
                anyString(),
                eq(IgdbAccessTokenService.Token.class)
        )).thenReturn(IgdbAccessTokenService.Token.builder().expires_in(0L).createdAt(LocalDateTime.now()).build());

        // Act
        GameService.IgdbCount actual = gameService.fetchAmountOfGames(0);

        // Assert
        assertEquals(expected, actual);
    }

    @Test
    void getGames() {
        // Arrange

        List<GameDTO> expected = (Arrays.asList(new GameDTO[]{new GameDTO(), new GameDTO()}));


        when(fetchTools.getFromApiList(
                any(List.class)
        )).thenReturn(Arrays.asList(new IgdbGame[][]{new IgdbGame[]{new IgdbGame()},new IgdbGame[]{new IgdbGame()}}));

        GameService gs = spy(gameService);

        doReturn(new GameService.IgdbCount(501))
                .when(gs).fetchAmountOfGames(anyLong());

        // Act
        List<GameDTO> actual = gs.getGames(0L);

        // Assert
        assertEquals(expected, actual);
    }
}