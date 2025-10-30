package com.emobile.springtodo.HealthIndicator;

import com.emobile.springtodo.services.ToDoServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Autowired
    public ToDoServicesImpl toDoServices;

    @Override
    public Health health() {return Health.up().withDetail("Всего задач : ", toDoServices.allItem().size()).build();}

}