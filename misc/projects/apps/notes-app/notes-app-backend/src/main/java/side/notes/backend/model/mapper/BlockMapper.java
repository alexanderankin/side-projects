package side.notes.backend.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import side.notes.backend.model.entity.BlockEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BlockMapper {
    @Mapping(target = "note", ignore = true)
    void updateBlockEntity(@MappingTarget BlockEntity target, BlockEntity source);
}
