package site.book.project.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import site.book.project.domain.PostReply;

@AllArgsConstructor
@Builder
@Getter
@ToString
public class ReplyReadDto {

    private Integer replyId;
    private Integer postId;
    private Integer bookId;
    private Integer userId; // postWriter id;
    private String nickName;
    private String replyContent;
    private String replyWriter;
    private String userImage;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    
    public static ReplyReadDto fromEntity(PostReply entity) {
        return ReplyReadDto.builder().replyId(entity.getReplyId())
                .postId(entity.getPost().getPostId())
                .bookId(entity.getPost().getBook().getBookId())
                .userId(entity.getPost().getUser().getId())
                .replyWriter(entity.getReplyWriter())
                .userImage(entity.getUser().getUserImage())
                .nickName(entity.getUser().getNickName())
                .replyContent(entity.getReplyContent())
                .createdTime(entity.getCreatedTime())
                .modifiedTime(entity.getModifiedTime())
                .build();
    }
}
