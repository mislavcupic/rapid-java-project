//package hr.algebra.rapid.logisticsandfleetmanagementsystem.service;
//
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.Shipment;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentRequest;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.ShipmentResponse;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.ShipmentRepository;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RouteService;
//import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl.ShipmentServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ShipmentServiceTest {
//
//    @Mock
//    private ShipmentRepository shipmentRepository;
//
//    @Mock
//    private RouteService routeService;
//
//    private ShipmentService shipmentService;
//    private Shipment shipment;
//    private ShipmentRequest requestDTO;
//
//    @BeforeEach
//    void setUp() {
//        shipmentService = new ShipmentServiceImpl(shipmentRepository, routeService);
//
//        shipment = new Shipment();
//        shipment.setId(1L);
//        shipment.setTrackingNumber("SHIP-001");
//
//        requestDTO = new ShipmentRequest();
//        requestDTO.setTrackingNumber("SHIP-001");
//    }
//
//    @Test
//    void findAll_ShouldReturnList() {
//        when(shipmentRepository.findAll()).thenReturn(Arrays.asList(shipment));
//        List<ShipmentResponse> result = shipmentService.findAll();
//        assertThat(result).isNotEmpty();
//    }
//
//    @Test
//    void findById_WhenExists_ShouldReturnShipment() {
//        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
//        Optional<ShipmentResponse> result = shipmentService.findById(1L);
//        assertThat(result).isPresent();
//    }
//
//    @Test
//    void createShipment_ShouldCreateShipment() {
//        when(shipmentRepository.save(any())).thenReturn(shipment);
//        ShipmentResponse result = shipmentService.createShipment(requestDTO);
//        assertThat(result).isNotNull();
//    }
//
//    @Test
//    void deleteShipment_ShouldDelete() {
//        when(shipmentRepository.existsById(1L)).thenReturn(true);
//        shipmentService.deleteShipment(1L);
//        verify(shipmentRepository, times(1)).deleteById(1L);
//    }
//}
