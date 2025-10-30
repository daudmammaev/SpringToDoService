package mappers;

import com.emobile.springtodo.dto.DtoToDo;
import com.emobile.springtodo.models.ToDo;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ToDoMapper implements RowMapper<ToDo> {

    @Override
    public ToDo mapRow(ResultSet rs, int rowNum) throws SQLException {
        ToDo toDo = new ToDo();
        toDo.setId(rs.getInt("id"));
        toDo.setText(rs.getString("text"));
        return toDo;
    }
    public static DtoToDo ToDoByDtodoto(ToDo toDo) {
        DtoToDo dto = new DtoToDo();
        dto.setId(toDo.getId());
        dto.setText(toDo.getText());
        return dto;
    }
    public static ToDo DtodotoByTodo(DtoToDo dto) {
        ToDo toDo = new ToDo();
        toDo.setId(dto.getId());
        toDo.setText(dto.getText());
        return toDo;
    }
}
