package dat.Service;

import dat.dtos.GameDTO;
import dat.dtos.IgdbGame;
import org.junit.jupiter.api.Tag;
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
@Tag("IntegrationTest")
class GameServiceTest {
    @Mock
    private FetchTools fetchTools;

    @InjectMocks
    private IgdbGameService gameService;

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
        IgdbGameService.IgdbCount expected = new IgdbGameService.IgdbCount();
        when(fetchTools.postToApi(
                        anyString(),
                        eq(IgdbGameService.IgdbCount.class),
                        any(HttpRequest.BodyPublisher.class),
                        any(String[].class)
                )
        ).thenReturn(expected);

        when(fetchTools.postToApi(
                anyString(),
                eq(IgdbAccessTokenService.Token.class)
        )).thenReturn(IgdbAccessTokenService.Token.builder().expires_in(0L).createdAt(LocalDateTime.now()).build());

        // Act
        IgdbGameService.IgdbCount actual = gameService.fetchAmountOfGames(0);

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

        IgdbGameService gs = spy(gameService);

        doReturn(new IgdbGameService.IgdbCount(501))
                .when(gs).fetchAmountOfGames(anyLong());

        // Act
        List<GameDTO> actual = gs.getGames(0L);

        // Assert
        assertEquals(expected, actual);
    }
}