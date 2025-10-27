package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {

    @Mock
    private ShipmentService shipmentService;

    @InjectMocks
    private ShipmentController shipmentController;

    private ShipmentResponse responseDTO;
    private ShipmentRequest requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new ShipmentResponse();
        responseDTO.setId(1L);

        requestDTO = new ShipmentRequest();
    }

    @Test
    void getAllShipments_ShouldReturnList() {
        when(shipmentService.findAll()).thenReturn(Arrays.asList(responseDTO));
        ResponseEntity<List<ShipmentResponse>> response = shipmentController.getAllShipments();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getShipmentById_WhenExists_ShouldReturnShipment() {
        when(shipmentService.findById(1L)).thenReturn(Optional.of(responseDTO));
        ResponseEntity<ShipmentResponse> response = shipmentController.getShipmentById(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createShipment_ShouldReturnCreatedShipment() {
        when(shipmentService.createShipment(any())).thenReturn(responseDTO);
        ResponseEntity<ShipmentResponse> response = shipmentController.createShipment(requestDTO);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void deleteShipment_ShouldReturnNoContent() {
        doNothing().when(shipmentService).deleteShipment(1L);
        ResponseEntity<Void> response = shipmentController.deleteShipment(1L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
