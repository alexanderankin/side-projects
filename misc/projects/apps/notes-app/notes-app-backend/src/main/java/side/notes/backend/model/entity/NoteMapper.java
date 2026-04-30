package side.notes.backend.model.entity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NoteMapper {
    @Mapping(target = "size", ignore = true)
    void updateNoteEntity(@MappingTarget NoteEntity target, NoteEntity source);
}
