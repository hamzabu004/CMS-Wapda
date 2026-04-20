package testcases;

import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.Region;
import com.electricity.cms.model.User;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.repository.ComplaintRepository;
import com.electricity.cms.repository.ComplaintStatusHistoryRepository;
import com.electricity.cms.repository.ConsumerRepository;
import com.electricity.cms.repository.UserRepository;
import com.electricity.cms.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ComplaintServiceTest {

    private ComplaintRepository complaintRepository;
    private ComplaintStatusHistoryRepository historyRepository;
    private ConsumerRepository consumerRepository;
    private UserRepository userRepository;
    private ComplaintService complaintService;

    @BeforeEach
    void setUp() {
        complaintRepository = mock(ComplaintRepository.class);
        historyRepository = mock(ComplaintStatusHistoryRepository.class);
        consumerRepository = mock(ConsumerRepository.class);
        userRepository = mock(UserRepository.class);

        complaintService = new ComplaintService(
                complaintRepository,
                historyRepository,
                consumerRepository,
                userRepository
        );
    }

    /**
     * ID: TC_ESC_1
     * INPUT: "f193c838-1b32-4fb7-bbb5-0fce06e601d4", "c2000000-0000-0000-0000-000000000001"
     * Setup: Role is REPRESENTATIVE.
     * Type: Branch Coverage
     * Expected: IllegalArgumentException
     */
    @Test
    void testEscalate_RepresentativeRole_ThrowsException() {
        UUID complaintId = UUID.fromString("f193c838-1b32-4fb7-bbb5-0fce06e601d4");
        UUID userId = UUID.fromString("c2000000-0000-0000-0000-000000000001");

        Complaint complaint = new Complaint();
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(complaint));

        User caller = new User();
        caller.setRole(UserRole.REPRESENTATIVE);
        when(userRepository.findById(userId)).thenReturn(Optional.of(caller));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            complaintService.escalate(complaintId, userId);
        });
        assertEquals("Representative escalation requires selecting a technician target.", exception.getMessage());
    }

    /**
     * ID: TC_ESC_2
     * INPUT: "f193c838-1b32-4fb7-bbb5-0fce06e601d4", "c3000000-0000-0000-0000-000000000002"
     * Setup: Role is TECHNICIAN, Region is null.
     * Type: Branch Coverage
     * Expected: IllegalStateException
     */
    @Test
    void testEscalate_TechnicianWithNullRegion_ThrowsException() {
        UUID complaintId = UUID.fromString("f193c838-1b32-4fb7-bbb5-0fce06e601d4");
        UUID userId = UUID.fromString("c3000000-0000-0000-0000-000000000002");

        Complaint complaint = new Complaint();
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(complaint));

        User caller = new User();
        caller.setRole(UserRole.TECHNICIAN);
        caller.setRegion(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(caller));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            complaintService.escalate(complaintId, userId);
        });
        assertEquals("Technician has no region configured.", exception.getMessage());
    }

    /**
     * ID: TC_ESC_3
     * INPUT: "f193c838-1b32-4fb7-bbb5-0fce06e601d4", "c3000000-0000-0000-0000-000000000003"
     * Setup: Role is TECHNICIAN, no Manager in the region.
     * Type: Path Coverage
     * Expected: IllegalStateException
     */
    @Test
    void testEscalate_TechnicianWithNoManager_ThrowsException() throws Exception {
        UUID complaintId = UUID.fromString("f193c838-1b32-4fb7-bbb5-0fce06e601d4");
        UUID userId = UUID.fromString("c3000000-0000-0000-0000-000000000003");
        UUID regionId = UUID.randomUUID();

        Complaint complaint = new Complaint();
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(complaint));

        Region region = new Region();
        TestUtils.setField(region, "id", regionId);



        User caller = new User();
        caller.setRole(UserRole.TECHNICIAN);
        caller.setRegion(region);
        when(userRepository.findById(userId)).thenReturn(Optional.of(caller));

        when(userRepository.findByRole(UserRole.MANAGER)).thenReturn(List.of());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            complaintService.escalate(complaintId, userId);
        });
        assertEquals("No manager in technician region.", exception.getMessage());
    }

    /**
     * ID: TC_ESC_4
     * INPUT: uuid1, uuid2
     * Setup: TECHNICIAN with valid Manager.
     * Type: Path Coverage
     * Expected: Executes createEscalationHistory
     */
    @Test
    void testEscalate_TechnicianWithManager_ExecutesEscalation() throws Exception {
        UUID complaintId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();

        Complaint complaint = new Complaint();
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(complaint));

        Region region = new Region();
        TestUtils.setField(region, "id", regionId);



        User caller = new User();
        caller.setRole(UserRole.TECHNICIAN);
        caller.setRegion(region);
        when(userRepository.findById(userId)).thenReturn(Optional.of(caller));

        User manager = new User();
        manager.setRole(UserRole.MANAGER);
        manager.setRegion(region);
        when(userRepository.findByRole(UserRole.MANAGER)).thenReturn(List.of(manager));

        // Assuming createEscalationHistory ends up saving into historyRepository
        complaintService.escalate(complaintId, userId);

        verify(historyRepository, times(1)).save(any());
        // A direct mock of historyRepository is verified
    }

    /**
     * ID: TC_ESC_5
     * INPUT: "f193c838-1b32-4fb7-bbb5-0fce06e601d4", "c1000000-0000-0000-0000-000000000001"
     * Setup: Role is CUSTOMER.
     * Type: Branch Coverage
     * Expected: IllegalArgumentException
     */
    @Test
    void testEscalate_CustomerRole_ThrowsException() {
        UUID complaintId = UUID.fromString("f193c838-1b32-4fb7-bbb5-0fce06e601d4");
        UUID userId = UUID.fromString("c1000000-0000-0000-0000-000000000001");

        Complaint complaint = new Complaint();
        when(complaintRepository.findById(complaintId)).thenReturn(Optional.of(complaint));

        User caller = new User();
        caller.setRole(UserRole.CUSTOMER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(caller));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            complaintService.escalate(complaintId, userId);
        });
        assertEquals("Escalation is only allowed for representative or technician.", exception.getMessage());
    }
}
