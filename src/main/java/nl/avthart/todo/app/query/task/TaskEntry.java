package nl.avthart.todo.app.query.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@ToString
@EqualsAndHashCode(of = { "id" })
@SuppressWarnings("JpaDataSourceORMInspection")
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"username", "createdHour", "title"})})
public class TaskEntry {

	@Id
	private String id;

	private String createdHour; // 2020-11-19T13Z

	private String username;

	@Version
	@Setter
	private Long version;

	@Setter
	private String title;
	
	@Setter
	private boolean completed;
	
	@Setter
	private boolean starred;
}