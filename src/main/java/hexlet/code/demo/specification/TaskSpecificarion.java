package hexlet.code.demo.specification;

import hexlet.code.demo.dto.TaskDTO.TaskQueryParamsDTO;
import hexlet.code.demo.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecificarion {
    public Specification<Task> build(TaskQueryParamsDTO taskQueryParamsDTO) {
        return withTitle(taskQueryParamsDTO.getTitleCont())
                .and(withAssigneeId(taskQueryParamsDTO.getAssigneeId()))
                .and(withStatus(taskQueryParamsDTO.getStatus()))
                .and(withLabelId(taskQueryParamsDTO.getLabelId()));
    }

    private Specification<Task> withTitle(String title) {
        return ((root, query, cb) ->
                title == null ? cb.conjunction() : cb.like(cb.lower(root.get("name")),
                        "%" + title.toLowerCase() + "%"));
    }

    private Specification<Task> withAssigneeId(Long assigneeId) {
        return (root, query, cb) ->
                assigneeId == null ? cb.conjunction() : cb.equal(root.join("assignee").get("id"), assigneeId);
    }

    private Specification<Task> withStatus(String status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.join("taskStatus").get("slug"), status);
    }

    private Specification<Task> withLabelId(Long labelId) {
        return (root, query, cb) ->
                labelId == null ? cb.conjunction() : cb.equal(root.join("labels").get("id"), labelId);
    }
}
