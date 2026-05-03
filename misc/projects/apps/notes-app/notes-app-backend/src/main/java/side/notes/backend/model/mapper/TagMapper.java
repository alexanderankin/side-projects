package side.notes.backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import side.notes.backend.model.entity.TagEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {
    void updateTagEntity(@MappingTarget TagEntity target, TagEntity source);
}
