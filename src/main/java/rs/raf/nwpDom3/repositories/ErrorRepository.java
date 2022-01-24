package rs.raf.nwpDom3.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.raf.nwpDom3.entities.Error;
import rs.raf.nwpDom3.entities.Machine;

import java.util.List;

@Repository
public interface ErrorRepository extends JpaRepository<Error, Long> {

    List<Error> getAllByMachine(Machine machine);

}
