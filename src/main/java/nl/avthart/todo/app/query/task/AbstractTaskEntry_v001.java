package nl.avthart.todo.app.query.task;

import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractTaskEntry_v001 {
    protected String createdHour; // 2020-11-19T13Z
    protected String username;
    protected String title;
    protected boolean completed;
    protected boolean starred;
}