package com.emobile.springtodo.validationsTest;

import com.emobile.springtodo.models.ToDo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ToDoRestTemplateValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Валидация задачи при пустом тексте")
    void shouldReturnBadRequestForInvalidToDo() {

        ToDo invalidToDo = new ToDo();
        invalidToDo.setText("");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/addItem", invalidToDo, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Валидация текста задачи")
    void shouldReturnOkForValidTextToDo() {

        ToDo validToDo = new ToDo();
        validToDo.setText("Valid task");

        ResponseEntity<ToDo> response = restTemplate.postForEntity(
                "/addItem", validToDo, ToDo.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}