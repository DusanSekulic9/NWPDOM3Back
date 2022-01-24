package rs.raf.nwpDom3.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rs.raf.nwpDom3.config.SpringConfiguration;
import rs.raf.nwpDom3.entities.Error;
import rs.raf.nwpDom3.entities.Machine;
import rs.raf.nwpDom3.entities.MachineStatus;
import rs.raf.nwpDom3.entities.User;
import rs.raf.nwpDom3.forms.MachineForm;
import rs.raf.nwpDom3.forms.SearchFrom;
import rs.raf.nwpDom3.repositories.ErrorRepository;
import rs.raf.nwpDom3.repositories.MachineRepository;
import rs.raf.nwpDom3.repositories.UserRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static rs.raf.nwpDom3.security.SecurityConstants.*;

@RestController
@EnableScheduling
@RequestMapping("/api/machines")
public class MachinesController {

    UserRepository userRepository = (UserRepository) SpringConfiguration.contextProvider().getApplicationContext().getBean("userRepository");
    MachineRepository machineRepository;
    ErrorRepository errorRepository = (ErrorRepository) SpringConfiguration.contextProvider().getApplicationContext().getBean("errorRepository");
    Gson gson = new Gson();
    TaskScheduler taskScheduler;

    @Autowired
    public MachinesController(MachineRepository machineRepository, TaskScheduler taskScheduler){
        this.machineRepository = machineRepository;
        this.taskScheduler = taskScheduler;
    }


    @PostMapping("/create")
    public ResponseEntity<Machine> createMachine(@RequestBody MachineForm form, @RequestHeader(value = HEADER_STRING) String token) {

        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_create_machines").asBoolean();

        if (!perm) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String email = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getSubject();

        User user = userRepository.findByEmail(email);

        Machine machine = new Machine();
        machine.setActive(true);
        machine.setName(form.getName());
        machine.setCreation(new Date());
        machine.setStatus(MachineStatus.STOPPED);
        machine.setCreatedBy(user);
        machineRepository.saveAndFlush(machine);
        return new ResponseEntity<>(machine, HttpStatus.ACCEPTED);
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteMachine(@RequestBody MachineForm form, @RequestHeader(value = HEADER_STRING) String token) {

        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_destroy_machines").asBoolean();

        if (!perm) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Machine machine = machineRepository.findByIdAndVersion(form.getId(), form.getVersion());

        if (machine == null) {
            return new ResponseEntity<>("Outdated version",HttpStatus.FORBIDDEN);
        }

        if(machine.getStatus().equals(MachineStatus.RUNNING)){
            return new ResponseEntity<>("Machine has to be stopped before destroying",HttpStatus.FORBIDDEN);
        }

        machine.setActive(false);
        machineRepository.saveAndFlush(machine);
        return new ResponseEntity<>(gson.toJson(machine), HttpStatus.ACCEPTED);
    }

    @PostMapping("/search")
    public ResponseEntity<String> searchMachines(@RequestHeader(value = HEADER_STRING) String token, @RequestBody SearchFrom form) {
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_search_machines").asBoolean();

        if (!perm) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String email = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getSubject();

        User user = userRepository.findByEmail(email);

        String name = null;
        if (form.getName() != null && !form.getName().equals("")) {
            name = form.getName();
        }

        String status = null;
        if (form.getStatus() != null && !form.getStatus().equals("")) {
            status = form.getStatus();
        }


        if (user == null) {
            return new ResponseEntity<>("No user found", HttpStatus.FORBIDDEN);

        }


        List<Machine> machines = new ArrayList<>();

        if (name != null && status == null && (form.getDateFrom() == null || form.getDateTo() == null)) {
            machines = machineRepository.getAllByCreatedByAndNameContainingAndActive(user, name, true);
        } else if (name != null && status != null && (form.getDateFrom() == null || form.getDateTo() == null)) {
            machines = machineRepository.getAllByCreatedByAndNameContainingAndStatusAndActive(user, name, MachineStatus.valueOf(status), true);
        } else if (name != null && status != null && form.getDateFrom() != null && form.getDateTo() != null) {
            machines = machineRepository.getAllByCreatedByAndNameContainingAndStatusAndCreationBetweenAndActive(user, name, MachineStatus.valueOf(status), form.getDateFrom(), form.getDateTo(), true);
        } else if (name != null && status == null && form.getDateFrom() != null && form.getDateTo() != null) {
            machines = machineRepository.getAllByCreatedByAndNameContainingAndCreationBetweenAndActive(user, name, form.getDateFrom(), form.getDateTo(), true);
        } else if (name == null && status == null && form.getDateFrom() != null && form.getDateTo() != null) {
            machines = machineRepository.getAllByCreatedByAndCreationBetweenAndActive(user, form.getDateFrom(), form.getDateTo(), true);
        } else if (name == null && status != null && form.getDateFrom() != null && form.getDateTo() != null) {
            machines = machineRepository.getAllByCreatedByAndStatusAndCreationBetweenAndActive(user, MachineStatus.valueOf(status), form.getDateFrom(), form.getDateTo(), true);
        } else if (name == null && status != null && (form.getDateFrom() == null || form.getDateTo() == null)) {
            machines = machineRepository.getAllByCreatedByAndStatusAndActive(user, MachineStatus.valueOf(status), true);
        } else if (name == null && status == null && (form.getDateFrom() == null || form.getDateTo() == null)) {
            machines = machineRepository.getAllByCreatedByAndActive(user, true);
        }

        return new ResponseEntity<>(gson.toJson(machines), HttpStatus.ACCEPTED);

    }

    @PostMapping("/start")
    public ResponseEntity<String> startMachines(@RequestHeader(value = HEADER_STRING) String token, @RequestBody MachineForm form) {
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_start_machines").asBoolean();

        if (!perm) {
            return new ResponseEntity<>("No permission",HttpStatus.FORBIDDEN);
        }

        if (form.getId() == null) {
            return new ResponseEntity<>("No machine found", HttpStatus.FORBIDDEN);
        }

        Machine machine = machineRepository.findByIdAndVersion(form.getId(), form.getVersion());


        if (machine == null) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to start!");
            error.setOperation("START");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Outdated version", HttpStatus.FORBIDDEN);
        }

        if (machine.getStatus().equals(MachineStatus.RUNNING)) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to start!");
            error.setOperation("START");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Can't start a machine that is running", HttpStatus.FORBIDDEN);
        }

        System.out.println("PRE THREAD SLEEP START");

        try{
            machine.setStatus(MachineStatus.RUNNING);
            machineRepository.saveAndFlush(machine);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ObjectOptimisticLockingFailureException exception) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to start!");
            error.setOperation("START");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Can't do action on this machine at the moment. Please try again", HttpStatus.FORBIDDEN);
        }

        System.out.println("POSLE THREAD SLEEP START");


        return new ResponseEntity<>(gson.toJson(machine), HttpStatus.ACCEPTED);
    }


    @PostMapping("/restart")
    public ResponseEntity<String> restartMachines(@RequestHeader(value = HEADER_STRING) String token, @RequestBody MachineForm form) {
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_restart_machines").asBoolean();

        if (!perm) {
            return new ResponseEntity<>("No permission",HttpStatus.FORBIDDEN);
        }

        if (form.getId() == null) {
            return new ResponseEntity<>("No machine found", HttpStatus.FORBIDDEN);
        }

        Machine machine = machineRepository.findByIdAndVersion(form.getId(), form.getVersion());


        if (machine == null) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to restart!");
            error.setOperation("RESTART");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Outdated version", HttpStatus.FORBIDDEN);
        }

        if (machine.getStatus().equals(MachineStatus.STOPPED)) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to restart!");
            error.setOperation("RESTART");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Can't restart a machine that is not running", HttpStatus.FORBIDDEN);
        }

        System.out.println("PRE THREAD SLEEP RESTART");


        try{
            machine.setStatus(MachineStatus.STOPPED);
            machineRepository.saveAndFlush(machine);
            Thread.sleep(5000);
            machine.setStatus(MachineStatus.RUNNING);
            machineRepository.saveAndFlush(machine);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ObjectOptimisticLockingFailureException exception) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to restart!");
            error.setOperation("RESTART");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Can't do action on this machine at the moment. Please try again", HttpStatus.FORBIDDEN);
        }

        System.out.println("POSLE THREAD SLEEP RESTART");


        return new ResponseEntity<>(gson.toJson(machine), HttpStatus.ACCEPTED);
    }


    @PostMapping("/stop")
    public ResponseEntity<String> stopMachines(@RequestHeader(value = HEADER_STRING) String token, @RequestBody MachineForm form) {
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_stop_machines").asBoolean();

        if (!perm) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (form.getId() == null) {
            return new ResponseEntity<>("No machine found", HttpStatus.FORBIDDEN);
        }

        Machine machine = machineRepository.findByIdAndVersion(form.getId(), form.getVersion());

        if (machine == null) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to stop!");
            error.setOperation("STOP");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Outdated version", HttpStatus.FORBIDDEN);
        }

        if (machine.getStatus().equals(MachineStatus.STOPPED)) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to stop!");
            error.setOperation("STOP");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Can't stop a stopped machine", HttpStatus.FORBIDDEN);
        }

        System.out.println("PRE THREAD SLEEP STOP");


        try{
            machine.setStatus(MachineStatus.STOPPED);
            machineRepository.saveAndFlush(machine);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ObjectOptimisticLockingFailureException exception) {
            Error error = new Error();
            error.setDate(new Date());
            error.setMachine(machine);
            error.setMessage("Failed to stop!");
            error.setOperation("STOP");
            errorRepository.saveAndFlush(error);
            return new ResponseEntity<>("Can't do action on this machine at the moment. Please try again", HttpStatus.FORBIDDEN);
        }
        System.out.println("POSLE THREAD SLEEP STOP");


        return new ResponseEntity<>(gson.toJson(machine), HttpStatus.ACCEPTED);
    }

    @GetMapping("/errors")
    public ResponseEntity<String> errors(@RequestHeader(value = HEADER_STRING) String token){
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_search_machines").asBoolean();

        if (!perm) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        String email = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getSubject();

        User user = userRepository.findByEmail(email);

        List<Machine> machines = machineRepository.getAllByCreatedBy(user);

        List<Error> errors = new ArrayList<>();

        for(Machine m : machines){
            errors.addAll(errorRepository.getAllByMachine(m));
        }

        errors.sort(null);



        return new ResponseEntity<>(gson.toJson(errors), HttpStatus.ACCEPTED);

    }


    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestBody MachineForm form, @RequestHeader(value = HEADER_STRING) String token){
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_search_machines").asBoolean();

        if (!perm) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }


        if(form.getId() != null && form.getName() != null && !form.getName().equals("") && form.getDate() != null){
            form.getDate().setHours(form.getDate().getHours() - 1);
            if(form.getName().equalsIgnoreCase("START")){
                this.taskScheduler.schedule(() ->{
                    boolean perms = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                            .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_start_machines").asBoolean();

                    if (!perms) {
                        return;
                    }


                    Optional<Machine> optmachine = machineRepository.findById(form.getId());

                    Machine machine = null;
                    if(optmachine.isPresent()){
                        machine = optmachine.get();
                    }

                    if (machine == null) {
                        return;
                    }

                    if (machine.getStatus().equals(MachineStatus.RUNNING)) {
                        Error error = new Error();
                        error.setDate(new Date());
                        error.setMachine(machine);
                        error.setMessage("Machine is already running");
                        error.setOperation("START");
                        errorRepository.saveAndFlush(error);
                        return;
                    }

                    try{
                        Thread.sleep(10000);
                        machine.setStatus(MachineStatus.STOPPED);
                        machineRepository.saveAndFlush(machine);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ObjectOptimisticLockingFailureException exception) {
                        Error error = new Error();
                        error.setDate(new Date());
                        error.setMachine(machine);
                        error.setMessage("Failed to start!");
                        error.setOperation("START");
                        errorRepository.saveAndFlush(error);
                    }

                } ,form.getDate());
            }else if(form.getName().equalsIgnoreCase("RESTART")){
                this.taskScheduler.schedule(() ->{
                    boolean perms = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                            .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_restart_machines").asBoolean();

                    if (!perms) {
                        return;
                    }
                    Optional<Machine> optmachine = machineRepository.findById(form.getId());

                    Machine machine = null;
                    if(optmachine.isPresent()){
                        machine = optmachine.get();
                    }

                    if (machine == null) {
                        return;
                    }

                    if (machine.getStatus().equals(MachineStatus.STOPPED)) {
                        Error error = new Error();
                        error.setDate(new Date());
                        error.setMachine(machine);
                        error.setMessage("Machine was stopped, can't restart stopped machine");
                        error.setOperation("RESTART");
                        errorRepository.saveAndFlush(error);
                        return;
                    }

                    try{
                        Thread.sleep(5000);
                        machine.setStatus(MachineStatus.STOPPED);
                        machineRepository.saveAndFlush(machine);
                        Thread.sleep(5000);
                        machine.setStatus(MachineStatus.RUNNING);
                        machineRepository.saveAndFlush(machine);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ObjectOptimisticLockingFailureException exception) {
                        Error error = new Error();
                        error.setDate(new Date());
                        error.setMachine(machine);
                        error.setMessage("Failed to restart!");
                        error.setOperation("RESTART");
                        errorRepository.saveAndFlush(error);
                    }
                } ,form.getDate());

            }else if(form.getName().equalsIgnoreCase("STOP")){
                this.taskScheduler.schedule(() ->{
                    boolean perms = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                            .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_stop_machines").asBoolean();

                    if (!perms) {
                        return;
                    }
                    Optional<Machine> optmachine = machineRepository.findById(form.getId());

                    Machine machine = null;
                    if(optmachine.isPresent()){
                        machine = optmachine.get();
                    }

                    if (machine == null) {
                        return;
                    }

                    if (machine.getStatus().equals(MachineStatus.STOPPED)) {
                        Error error = new Error();
                        error.setDate(new Date());
                        error.setMachine(machine);
                        error.setMessage("Machine was already stopped");
                        error.setOperation("STOP");
                        errorRepository.saveAndFlush(error);
                        return;
                    }

                    try{
                        Thread.sleep(10000);
                        machine.setStatus(MachineStatus.STOPPED);
                        machineRepository.saveAndFlush(machine);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ObjectOptimisticLockingFailureException exception) {
                        Error error = new Error();
                        error.setDate(new Date());
                        error.setMachine(machine);
                        error.setMessage("Failed to stop!");
                        error.setOperation("STOP");
                        errorRepository.saveAndFlush(error);
                    }
                } ,form.getDate());

            }
            return new ResponseEntity<>(gson.toJson("Operation scheduled successfully"),HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Scheduling failed" ,HttpStatus.FORBIDDEN);
    }


}