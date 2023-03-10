package site.book.project.service;

import java.util.ArrayList;
import java.util.List;

// import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.book.project.domain.Book;
import site.book.project.domain.Post;
import site.book.project.domain.User;
import site.book.project.dto.PostCreateDto;
import site.book.project.dto.PostListDto;
import site.book.project.dto.PostReadDto;
import site.book.project.dto.PostUpdateDto;
import site.book.project.dto.ReplyReadDto;
import site.book.project.repository.BookRepository;
import site.book.project.repository.PostRepository;
import site.book.project.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReplyService replyService;
    private final UserService userService;
    
    

    @Transactional(readOnly = true)
    public List<Post> read(){
        log.info("read()");
        
        return postRepository.findByOrderByPostIdDesc();
    }
   
    @Transactional(readOnly = true)
    public List<PostListDto> postDtoList(Integer userId) {
        List<Post> list = postRepository.findByUserIdOrderByCreatedTimeDesc(userId);
         
        List<PostListDto> dtoList = new ArrayList<>();
        
        PostListDto dto = null;
        
        for (Post post : list) {
            Post p = post;
            
            List<ReplyReadDto> rpiList = replyService.readReplies(p.getPostId());
             
           dto = PostListDto.builder()
            .userId(p.getUser().getId())
            .postId(p.getPostId())
            .title(p.getTitle())
            .postWriter(p.getPostWriter())
            .postContent(p.getPostContent())
            .bookId(p.getBook().getBookId())
            .bookImage(p.getBook().getBookImage()).modifiedTime(p.getModifiedTime())
            .createdTime(p.getCreatedTime())
            .replyCount(rpiList.size())
            .hit(p.getHit())
            .build();
            
        
             dtoList.add(dto);           
        }
        
        
         return dtoList;
    }
    
    
    
  
    @Transactional
    public Post create(PostCreateDto dto) {
        Book book = bookRepository.findById(dto.getBookId()).get();
        User user = userRepository.findById(dto.getUserId()).get();

        if( book.getBookScore() == null) {
        	book.update(25);
        } else {
        	Integer score = book.getBookScore() + dto.getMyScore()*10;
        	book.update(score/2);
        	
        }
        
        
        Post entity = postRepository.save(dto.toEntity(book,user));
        return entity;
    }

    @Transactional(readOnly = true)
    public Post read(Integer postId) {
        log.info("read(postId = {})", postId);
        
        return postRepository.findById(postId).get();
    }

    public void delete(Integer postId) {
        log.info("delete(postId={})",postId);
       
        postRepository.deleteById(postId);
       
    }
   
    @Transactional // readOnly = false(?????????)
    public void update(PostUpdateDto dto) {
        log.info("update(dto={})", dto);
       
        // ???????????? @Transactional ?????????????????? ????????????,
        // (1) ???????????? ?????? ????????? ????????? ????????? ??????
        // (2) ????????? ????????? ????????? ????????????
        // ???????????? ????????? ??? ?????????????????? ???????????? ???????????? update SQL??? ?????????.
        // PostRepository??? save() ???????????? ??????????????? ???????????? ????????? ???.
        Post entity = postRepository.findById(dto.getPostId()).get(); // (1)
        entity.update(dto.getTitle(), dto.getPostContent()); // (2)
       
        
    }

    @Transactional(readOnly = true)
    public List<Post> search(String type, String keyword) {
        log.info("search(type= {} keyword={})", type, keyword);
        
        List<Post> list = new ArrayList<>();
        
        switch(type) {
        case "pt":
            list = postRepository.findByTitleIgnoreCaseContainingOrderByPostIdDesc(keyword);
            break;
        case "pc":
            list = postRepository.findByPostContentIgnoreCaseContainingOrderByPostIdDesc(keyword);
            break;
        
        }
        
        return list;
    }


    
    // choi 1207 ??? ?????? post ?????????, ?????? ?????????, ?????? ????????? => Ajax??? ??? ??????
    
	public List<Post> findBybookId(Integer bookId) {
	    
	    // ????????? ???
	    return postRepository.findByBookBookId(bookId);
	}

	// ?????????
	public List<PostReadDto> findDesc(Integer bookId){
	    List<Post> list = postRepository.findByBookBookIdOrderByCreatedTimeDesc(bookId); 
	    return list.stream().map(PostReadDto:: fromEntity).toList();
	}
	
	// ?????? ?????????
	public List<PostReadDto> findScoreDesc(Integer bookId){
	    List<Post> list = postRepository.findByBookBookIdOrderByMyScoreDesc(bookId);
	    
	    return list.stream().map(PostReadDto:: fromEntity).toList();
	}
	
	// ?????? ?????????
	public List<PostReadDto> findScore(Integer bookId){
	    List<Post> list = postRepository.findByBookBookIdOrderByMyScore(bookId);
	    return list.stream().map(PostReadDto:: fromEntity).toList();
	}

	// (??????) ?????? ???????????? BookId??? Post ?????? ??? ??? ??????????????? select??????
	@Transactional(readOnly = true)
	public Integer countPostByBookId(Integer bookId) {
	    Integer count = 0;
	    List<Post> list = postRepository.findByBookBookId(bookId);
	    count = list.size();
	    return count;
	}

	// (??????) ??????????????? ????????? ??? - ??? ID??? ???????????? ????????? ?????? 1 ??????????????????
	@Transactional
	public void countUpPostByBookId(Integer bookId) {
	    Book entity = bookRepository.findById(bookId).get();
	    entity.updatePostCount(entity.getPostCount()+1);
	}
	
	
	
	// (??????)  writer ??? ????????? ?????? ?????? ????????????
	public List<PostReadDto> postRecomm(String writer, Integer bookId ){
	    List<PostReadDto> list = findScoreDesc(bookId);
	    List<PostReadDto> postC =  new ArrayList<>();
	    
	    
	     
	    for(PostReadDto post : list) {
	        
	       if(!post.getWriter().equals(writer)) {
	           
	           User user = userService.read(post.getWriter());
	           
	           String userImage = user.getUserImage();
	           post.setUserImage(userImage);
	           
	           postC.add(post);
	           
	       }
	        
	        
	    }
	    
	    
	    
	    return postC;
	}
	
	
	
}
