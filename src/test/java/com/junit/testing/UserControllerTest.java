package com.junit.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junit.testing.controller.UserController;
import com.junit.testing.entity.User;
import com.junit.testing.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetUsers() throws Exception {
        User user = new User(1L, "Bharath", "bharath@mail.com");

        when(userService.getAllUsers())
                .thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bharath"));
    }

    @Test
    void testCreateUser() throws Exception {
        User user = new User(1L, "Bharath", "bharath@mail.com");
        User user2 = new User(2L,"Viswa","viswa@mail.com");
        when(userService.saveUser(Mockito.any(User.class)))
                .thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bharath"));
    }

    @Test
    void testGetUsers_EmptyList() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
    @Test
    void testGetUsers_Exception() throws Exception {
        when(userService.getAllUsers())
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void testCreateUser_InvalidInput() throws Exception {
        User invalidUser = new User(null, "", "");

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testUpdateUser() throws Exception {

        User updatedUser = new User(1L,"Bharath Updated","bharath@gmail.com");

        when(userService.updateUser(
                        Mockito.eq(1L),
                        Mockito.any(User.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Bharath Updated"));
    }
    @Test
    void testUpdateUser_NotFound() throws Exception {

        User user = new User(1L,"Bharath","bharath@gmail.com");

        when(userService.updateUser(
                        Mockito.eq(10L),
                        Mockito.any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/10")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteUser_success() throws Exception {

        Long userId = 1L;

        // mock service behavior (void method)
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void deleteUser_notFound() throws Exception {

        Long userId = 1L;

        doThrow(new RuntimeException("User not found"))
                .when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isInternalServerError()); // default if no handler

        verify(userService, times(1)).deleteUser(userId);
    }


}

