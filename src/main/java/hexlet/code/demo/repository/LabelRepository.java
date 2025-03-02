package hexlet.code.demo.repository;

import hexlet.code.demo.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    Optional<Label> findByName(String name);
    Set<Label> findByIdIn(Set<Long> labelIds);
}
