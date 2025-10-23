package dat.Service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IgdbAccessTokenServiceTest {
    @Mock
    private FetchTools fetchTools;

    @InjectMocks
    private IgdbAccessTokenService igdbAccessTokenService;

    @Test
    void fetchToken() {

        // Arrange
        IgdbAccessTokenService.Token expected = IgdbAccessTokenService.Token.builder().expires_in(0L).createdAt(LocalDateTime.now()).build();

        when(fetchTools.postToApi(
                anyString(),
                eq(IgdbAccessTokenService.Token.class)
        )).thenReturn(expected);

        // Act
        IgdbAccessTokenService.Token actual = igdbAccessTokenService.fetchToken();

        // Assert
        assertEquals(expected, actual);

    }
}