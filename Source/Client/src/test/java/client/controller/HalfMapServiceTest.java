package client.controller;

import client.controller.network.service.NetworkCenter;
import client.exception.GameCommunicationException;
import client.exception.GameStateException;
import client.model.common.Notification;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.generator.MapGenerator;
import client.model.mapper.validator.MapValidator;
import client.view.GameOutput;
import client.view.MapGenerationView;
import client.view.MapValidationInternalsView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests {@link HalfMapService} behavior for generating, validating, and sending half maps (including error reporting).
 */
class HalfMapServiceTest {

    @Test
    void generateAndSendHalfMap_whenPlayerIdMissing_throwsGameStateException() {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        GameOutput output = mock(GameOutput.class);

        HalfMapService service = new HalfMapService(
                networkCenter,
                output,
                mock(MapGenerator.class),
                mock(MapValidator.class),
                mock(MapGenerationView.class),
                mock(MapValidationInternalsView.class)
        );

        assertThrows(GameStateException.class, () -> service.generateAndSendHalfMap("  ", "game"));
    }

    @Test
    void generateAndSendHalfMap_whenSendHalfMapThrows_wrapsAsGameCommunicationException() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        GameOutput output = mock(GameOutput.class);
        MapGenerator generator = mock(MapGenerator.class);
        MapValidator validator = mock(MapValidator.class);
        MapGenerationView mapView = mock(MapGenerationView.class);
        MapValidationInternalsView internalsView = mock(MapValidationInternalsView.class);

        PlayerHalfMap map = new PlayerHalfMap("p1");
        when(generator.generateMap(anyInt(), anyInt(), eq("p1"))).thenReturn(map);
        when(validator.validate(any(PlayerHalfMap.class))).thenReturn(new Notification());

        doThrow(new RuntimeException("boom"))
                .when(networkCenter).sendHalfMap(any(PlayerHalfMap.class));

        HalfMapService service = new HalfMapService(networkCenter, output, generator, validator, mapView, internalsView);

        GameCommunicationException ex = assertThrows(GameCommunicationException.class,
                () -> service.generateAndSendHalfMap("p1", "game"));
        assertTrue(ex.getMessage().toLowerCase().contains("failed to send half map"));

        verify(output).showMapValidationOk();
        verify(mapView).printHalfMap(eq(map), anyString());
    }

    @Test
    void generateAndSendHalfMap_whenValidationNeverPasses_throwsGameStateExceptionAfterRetries() throws Exception {
        NetworkCenter networkCenter = mock(NetworkCenter.class);
        GameOutput output = mock(GameOutput.class);
        MapGenerator generator = mock(MapGenerator.class);
        MapValidator validator = mock(MapValidator.class);
        MapGenerationView mapView = mock(MapGenerationView.class);
        MapValidationInternalsView internalsView = mock(MapValidationInternalsView.class);

        when(generator.generateMap(anyInt(), anyInt(), eq("p1"))).thenReturn(new PlayerHalfMap("p1"));

        Notification alwaysFail = new Notification();
        alwaysFail.addError("invalid");
        when(validator.validate(any(PlayerHalfMap.class))).thenReturn(alwaysFail);

        HalfMapService service = new HalfMapService(networkCenter, output, generator, validator, mapView, internalsView);

        assertThrows(GameStateException.class, () -> service.generateAndSendHalfMap("p1", "game"));

        verify(output, atLeastOnce()).showMapValidationFailed(contains("Attempt"));
        verify(internalsView, atLeastOnce()).report(any(Notification.class));
        verify(networkCenter, never()).sendHalfMap(any());
    }
}
