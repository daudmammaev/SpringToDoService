package mappers;

import com.emobile.springtodo.models.ToDo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ToDoMapRow implements RowMapper<ToDo> {
    @Override
    public ToDo mapRow(ResultSet rs, int rowNum) throws SQLException {
        ToDo toDo = new ToDo();
        toDo.setId(rs.getInt("id"));
        toDo.setText(rs.getString("text"));
        return toDo;
    }
}
