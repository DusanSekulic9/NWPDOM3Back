package rs.raf.nwpDom3.controller;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import org.hibernate.sql.Delete;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import rs.raf.nwpDom3.config.SpringConfiguration;
import rs.raf.nwpDom3.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import rs.raf.nwpDom3.forms.UserForm;
import rs.raf.nwpDom3.repositories.UserRepository;


import java.util.List;

import static rs.raf.nwpDom3.security.SecurityConstants.*;

@RestController
@RequestMapping("/api")
public class Controller {
    private BCryptPasswordEncoder encoder;

    UserRepository userRepository = (UserRepository) SpringConfiguration.contextProvider().getApplicationContext().getBean("userRepository");
    Gson gson = new Gson();

    @Autowired
    public Controller(BCryptPasswordEncoder encoder){
        this.encoder = encoder;
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserForm form, @RequestHeader (value = HEADER_STRING) String token){


        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_create_users").asBoolean();

        if(!perm){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        User user = new User();
        user.setName(form.getName());
        user.setEmail(form.getUsername());
        user.setPassword(encoder.encode(form.getPassword()));
        user.setSurname(form.getSurname());
        if(form.isCan_create_users()){
            user.setCan_create_users(true);
        }
        if(form.isCan_delete_users()){
            user.setCan_delete_users(true);
        }
        if(form.isCan_read_users()){
            user.setCan_read_users(true);
        }
        if(form.isCan_update_users()){
            user.setCan_update_users(true);
        }
        userRepository.saveAndFlush(user);
        return new ResponseEntity<>(gson.toJson(user), HttpStatus.ACCEPTED);
    }

    @GetMapping
    public ResponseEntity<String> getAllUsers(@RequestHeader (value = HEADER_STRING) String token){
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_read_users").asBoolean();

        if(!perm){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(gson.toJson(users), HttpStatus.ACCEPTED);
    }

    @PutMapping
    public ResponseEntity<String> updateUser(@RequestBody UserForm form, @RequestHeader (value = HEADER_STRING) String token){
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_update_users").asBoolean();

        if(!perm){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        User user = userRepository.findByEmail(form.getUsername());
        user.setName(form.getName());
        user.setEmail(form.getUsername());
        user.setPassword(encoder.encode(form.getPassword()));
        user.setSurname(form.getSurname());
        if(form.isCan_create_users()){
            user.setCan_create_users(true);
        }
        if(form.isCan_delete_users()){
            user.setCan_delete_users(true);
        }
        if(form.isCan_read_users()){
            user.setCan_read_users(true);
        }
        if(form.isCan_update_users()){
            user.setCan_update_users(true);
        }
        userRepository.saveAndFlush(user);
        return new ResponseEntity<>(gson.toJson(user), HttpStatus.ACCEPTED);
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestBody UserForm form, @RequestHeader (value = HEADER_STRING) String token){
        boolean perm = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
                .verify(token.replace(TOKEN_PREFIX, "")).getClaim("can_delete_users").asBoolean();

        if(!perm){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        userRepository.deleteById(Long.parseLong(form.getUsername()));
        return new ResponseEntity<>(gson.toJson(form), HttpStatus.ACCEPTED);
    }

}
