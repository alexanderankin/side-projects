package side.notes.backend.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Data
@Accessors(chain = true)
public class PagedModelDto<T> {
    List<T> content;
    PagedModel.PageMetadata page;

    public static <T> PagedModelDto<T> from(PagedModel<T> pagedModel) {
        return new PagedModelDto<T>()
                .setContent(pagedModel.getContent())
                .setPage(pagedModel.getMetadata());
    }

    public PagedModel<T> toPagedModel() {
        return new PagedModel<>(new PageImpl<>(content, PageRequest.of(Math.toIntExact(page.number()), Math.toIntExact(page.size())), page.totalElements()));
    }
}
