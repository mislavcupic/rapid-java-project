package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AssignmentResponseDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.ResourceNotFoundException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit testovi za AssignmentController
 * Testira sve endpoint metode koje STVARNO POSTOJE u controlleru
 */
@ExtendWith(MockitoExtension.class)
class AssignmentControllerTest {

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AssignmentController assignmentController;

    private AssignmentResponseDTO responseDTO;
    private AssignmentRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // Setup response DTO
        responseDTO = new AssignmentResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setStartTime(LocalDateTime.of(2025, 1, 15, 8, 0));
        responseDTO.setAssignmentStatus("ACTIVE");

        // Setup request DTO
        requestDTO = new AssignmentRequestDTO();
        requestDTO.setDriverId(1L);
        requestDTO.setVehicleId(2L);
        requestDTO.setShipmentId(3L);
        requestDTO.setStartTime(LocalDateTime.of(2025, 11, 15, 8, 0));
    }

    @Test
    void getAllAssignments_ShouldReturnListOfAssignments() {
        // Arrange
        List<AssignmentResponseDTO> assignments = Arrays.asList(responseDTO);
        when(assignmentService.findAll()).thenReturn(assignments);

        // Act
        ResponseEntity<List<AssignmentResponseDTO>> response = assignmentController.getAllAssignments();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getId()).isEqualTo(1L);
        verify(assignmentService, times(1)).findAll();
    }

    @Test
    void getAllAssignments_WhenNoAssignments_ShouldReturnEmptyList() {
        // Arrange
        when(assignmentService.findAll()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<AssignmentResponseDTO>> response = assignmentController.getAllAssignments();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }


    @Test
    void getAssignmentById_WhenAssignmentExists_ShouldReturnAssignment() {
        // Arrange
        when(assignmentService.findById(1L)).thenReturn(Optional.of(responseDTO));

        // Act
        ResponseEntity<AssignmentResponseDTO> response = assignmentController.getAssignmentById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(assignmentService, times(1)).findById(1L);
    }

    @Test
    void getAssignmentById_WhenAssignmentNotFound_ShouldThrowException() {
        // Arrange
        when(assignmentService.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> assignmentController.getAssignmentById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Assignment")
            .hasMessageContaining("999");

        verify(assignmentService, times(1)).findById(999L);
    }

    @Test
    void createAssignment_WhenValidRequest_ShouldReturnCreatedAssignment() {
        // Arrange
        when(assignmentService.createAssignment(any(AssignmentRequestDTO.class)))
            .thenReturn(responseDTO);

        // Act
        ResponseEntity<AssignmentResponseDTO> response = assignmentController.createAssignment(requestDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        verify(assignmentService, times(1)).createAssignment(requestDTO);
    }

    @Test
    void updateAssignment_WhenValidRequest_ShouldReturnUpdatedAssignment() {
        // Arrange
        AssignmentResponseDTO updatedResponse = new AssignmentResponseDTO();
        updatedResponse.setId(1L);
        updatedResponse.setAssignmentStatus("COMPLETED");

        when(assignmentService.updateAssignment(eq(1L), any(AssignmentRequestDTO.class)))
            .thenReturn(updatedResponse);

        // Act
        ResponseEntity<AssignmentResponseDTO> response = assignmentController.updateAssignment(1L, requestDTO);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getAssignmentStatus()).isEqualTo("COMPLETED");
        verify(assignmentService, times(1)).updateAssignment(1L, requestDTO);
    }

    @Test
    void deleteAssignment_WhenAssignmentExists_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(assignmentService).deleteAssignment(1L);

        // Act
        ResponseEntity<Void> response = assignmentController.deleteAssignment(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(assignmentService, times(1)).deleteAssignment(1L);
    }

    @Test
    void getDriverSchedule_WhenCalled_ShouldReturnDriverAssignments() {
        // Arrange
        List<AssignmentResponseDTO> driverAssignments = Arrays.asList(responseDTO);
        when(assignmentService.findAssignmentsByDriver(any(Long.class)))
            .thenReturn(driverAssignments);
        when(userDetails.getUsername()).thenReturn("testdriver");

        // Act
        ResponseEntity<List<AssignmentResponseDTO>> response = 
            assignmentController.getDriverSchedule(userDetails);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        verify(assignmentService, times(1)).findAssignmentsByDriver(any(Long.class));
    }

    @Test
    void getDriverSchedule_WhenNoAssignments_ShouldReturnEmptyList() {
        // Arrange
        when(assignmentService.findAssignmentsByDriver(any(Long.class)))
            .thenReturn(Arrays.asList());
        when(userDetails.getUsername()).thenReturn("testdriver");

        // Act
        ResponseEntity<List<AssignmentResponseDTO>> response = 
            assignmentController.getDriverSchedule(userDetails);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
