package side.notes.backend.model.entity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {
    void updateTagEntity(@MappingTarget TagEntity target, TagEntity source);
}
