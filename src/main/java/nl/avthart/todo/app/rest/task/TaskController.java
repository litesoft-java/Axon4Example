package nl.avthart.todo.app.rest.task;

import java.security.Principal;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import nl.avthart.todo.app.domain.task.commands.TaskCommandComplete;
import nl.avthart.todo.app.domain.task.commands.TaskCommandCreate;
import nl.avthart.todo.app.domain.task.commands.TaskCommandModifyTitle;
import nl.avthart.todo.app.domain.task.commands.TaskCommandStar;
import nl.avthart.todo.app.domain.task.commands.TaskCommandUnstar;
import nl.avthart.todo.app.query.task.TaskEntry;
import nl.avthart.todo.app.query.task.TaskEntryRepository;
import nl.avthart.todo.app.rest.task.requests.TaskRequestCreate;
import nl.avthart.todo.app.rest.task.requests.TaskRequestModifyTitle;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.IdentifierFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author albert
 */
@RestController
@RequiredArgsConstructor
public class TaskController {

    private final IdentifierFactory identifierFactory = IdentifierFactory.getInstance();

    private final TaskEntryRepository taskEntryRepository;

    private final SimpMessageSendingOperations messagingTemplate;

    private final CommandGateway commandGateway;

    @RequestMapping(value = "/api/tasks", method = RequestMethod.GET)
    public @ResponseBody
    Page<TaskEntry> findAll( Principal principal, @RequestParam(required = false, defaultValue = "false") boolean completed, Pageable pageable ) {
        return taskEntryRepository.findByUsernameAndCompleted( principal.getName(), completed, pageable );
    }

    @RequestMapping(value = "/api/tasks", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.CREATED)
    public void create( Principal principal, @RequestBody @Valid TaskRequestCreate request ) {
        commandGateway.sendAndWait( new TaskCommandCreate( identifierFactory.generateIdentifier(), principal.getName(), request.getTitle() ) );
    }

    @RequestMapping(value = "/api/tasks/{identifier}/title", method = RequestMethod.POST) // IMO: should be PATCH, but not supported by current Angular version
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void modifyTitle( @PathVariable String identifier, @RequestBody @Valid TaskRequestModifyTitle request ) {
        commandGateway.sendAndWait( new TaskCommandModifyTitle( identifier, request.getTitle() ) );
    }

    @RequestMapping(value = "/api/tasks/{identifier}/complete", method = RequestMethod.POST) // IMO: should be PATCH, but not supported by current Angular version
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void complete( @PathVariable String identifier ) {
        commandGateway.sendAndWait( new TaskCommandComplete( identifier ) );
    }

    @RequestMapping(value = "/api/tasks/{identifier}/star", method = RequestMethod.POST) // IMO: should be PATCH, but not supported by current Angular version
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void star( @PathVariable String identifier ) {
        commandGateway.sendAndWait( new TaskCommandStar( identifier ) );
    }

    @RequestMapping(value = "/api/tasks/{identifier}/unstar", method = RequestMethod.POST) // IMO: should be PATCH, but not supported by current Angular version
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void unstar( @PathVariable String identifier ) {
//		throw new RuntimeException("Could not unstar task...");
        commandGateway.sendAndWait( new TaskCommandUnstar( identifier ) );
    }

    @ExceptionHandler
    public void handleException( Principal principal, Throwable exception ) {
        messagingTemplate.convertAndSendToUser( principal.getName(), "/queue/errors", exception.getMessage() );
    }
}
