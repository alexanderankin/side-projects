package side.notes.backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import side.notes.backend.model.entity.NoteEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NoteMapper {
    void updateNoteEntity(@MappingTarget NoteEntity target, NoteEntity source);
}
