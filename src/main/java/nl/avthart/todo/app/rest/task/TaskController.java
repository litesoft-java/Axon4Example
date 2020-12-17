package nl.avthart.todo.app.rest.task;

import java.security.Principal;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import nl.avthart.todo.app.common.util.ExceptionMessage;
import nl.avthart.todo.app.configuration.Endpoint;
import nl.avthart.todo.app.query.task.TaskActive;
import nl.avthart.todo.app.rest.task.requests.TaskRequestCreate;
import nl.avthart.todo.app.rest.task.requests.TaskRequestModifyTitle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tasks")
public class TaskController implements Endpoint.Controller {
    private final TaskRequestHandler handler;
    private final SimpMessageSendingOperations messagingTemplate;

    @GetMapping
    public @ResponseBody
    Page<TaskActive> findAll( Principal principal,
                              @RequestParam(required = false, defaultValue = "false") boolean completed,
                              Pageable pageable ) {
        return handler.findAll( principal.getName(), completed, pageable );
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public void create( Principal principal, @RequestBody @Valid TaskRequestCreate request ) {
        String id = handler.create( principal.getName(), request );
        System.out.println( "TaskController.create (id, should add location header): " + id );
    }

    @PostMapping("/{identifier}/title") // IMO: should be PATCH, but not supported by current Angular version
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifyTitle( @PathVariable String identifier, @RequestBody @Valid TaskRequestModifyTitle request ) {
        handler.modifyTitle( identifier, request );
    }

    @PostMapping("/{identifier}/complete") // IMO: should be PATCH, but not supported by current Angular version
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void complete( @PathVariable String identifier ) {
        handler.complete( identifier );
    }

    @PostMapping("/{identifier}/star") // IMO: should be PATCH, but not supported by current Angular version
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void star( @PathVariable String identifier ) {
        handler.star( identifier );
    }

    @PostMapping("/{identifier}/unstar") // IMO: should be PATCH, but not supported by current Angular version
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unstar( @PathVariable String identifier ) {
        handler.unstar( identifier );
    }

    @DeleteMapping("/{identifier}/delete")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void delete( @PathVariable String identifier ) {
        handler.delete( identifier );
    }

    @PutMapping("/{identifier}/restore")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Endpoint.Admin
    public void restore( @PathVariable String identifier ) {
        handler.restore( identifier );
    }

    @ExceptionHandler
    public void handleException( Principal principal, Throwable exception ) {
        messagingTemplate.convertAndSendToUser( principal.getName(), "/queue/errors", ExceptionMessage.from(exception) );
    }
}
