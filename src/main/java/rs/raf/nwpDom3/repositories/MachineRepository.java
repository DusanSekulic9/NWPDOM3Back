package rs.raf.nwpDom3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.raf.nwpDom3.entities.Machine;
import rs.raf.nwpDom3.entities.MachineStatus;
import rs.raf.nwpDom3.entities.User;

import java.util.Date;
import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {

    List<Machine> getAllByCreatedByAndActive(User user, boolean active);

    List<Machine> getAllByCreatedBy(User user);

    List<Machine> getAllByCreatedByAndNameContainingAndActive(User user, String name, boolean active);

    List<Machine> getAllByCreatedByAndNameContainingAndStatusAndActive(User user, String name, MachineStatus status, boolean active);

    List<Machine> getAllByCreatedByAndNameContainingAndStatusAndCreationBetweenAndActive(User user, String name, MachineStatus status, Date from, Date to, boolean active);

    List<Machine> getAllByCreatedByAndNameContainingAndCreationBetweenAndActive(User user, String name, Date from, Date to, boolean active);

    List<Machine> getAllByCreatedByAndCreationBetweenAndActive(User user, Date from, Date to, boolean active);

    List<Machine> getAllByCreatedByAndStatusAndActive(User user, MachineStatus status, boolean active);

    List<Machine> getAllByCreatedByAndStatusAndCreationBetweenAndActive(User user, MachineStatus status, Date from, Date to, boolean active);

    Machine findByIdAndVersion(Long id, Integer version);

    Machine getById(Long id);
}
