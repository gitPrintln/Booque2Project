package site.book.project.service;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.book.project.domain.Chat;
import site.book.project.dto.ChatReadDto;
import site.book.project.repository.ChatRepository;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class ChatService {
    
    private final ChatRepository chatRepository;
    
    @Value("${file.upload-dir}.txt")
    String fileUploadPath; 
    
    // DB의 Chat 테이블에 새 데이터 행 생성
    // fileName 컬럼은 일단 null인 듯?
    public void createChat(Integer usedBookId, Integer sellerId, Integer buyerId) throws IOException {
        
        Chat chat = Chat.builder().usedBookId(usedBookId).sellerId(sellerId).buyerId(buyerId).build();
        
        chatRepository.save(chat);
        createFile(chat.getChatRoomId(), usedBookId);
    }
    
    // 로컬 저장소(D:\\study\\chat-txt/)에 txt 파일 생성
    // 파일 경로:  D:\\study\\chat-txt/중고판매글id_채팅방id.txt    (예: D:\\study\\chat-txt/2_1.txt)
    public void createFile(Integer chatRoomId, Integer usedBookId) throws IOException {
        
        String fileName = usedBookId+ "_" + chatRoomId + ".txt";
        String pathName = fileUploadPath + fileName;
        
        // Java의 File 클래스와 createNewFile() 메서드 이용해 파일 생성
        File chatFile = new File(pathName);
        chatFile.createNewFile();
        updateChat(chatRoomId, fileName);
    }
    
    @Transactional
    public void updateChat(Integer chatRoomId, String fileName) {
        // 파일이 생성되면 chat 테이블의 fileName 컬럼에 위의 파일명을 업데이트
        
        Chat entity = chatRepository.findById(chatRoomId).get();
        entity.update(fileName);
        
    }
    
    
    
    // (이미 채팅 기록이 있을 경우) 기록 불러 오기
    public List<ChatReadDto> readChatHistory(Chat chat) throws IOException {
        
        List<ChatReadDto> chatHistory = new ArrayList<ChatReadDto>();
        
        // fileName 컬럼을 통해 파일의 경로 찾기, 파일 읽기
        String pathName = fileUploadPath + chat.getFileName();
        FileReader fr = new FileReader(pathName);
        BufferedReader br = new BufferedReader(fr);
        
        ChatReadDto chatLines = new ChatReadDto();
        String chatLine;
        int index = 1;   // 채팅 메시지블럭 하나의 줄(row) 인덱스
        
        while ((chatLine = br.readLine()) != null) {
            
            //1개 메시지는 3줄(보낸사람,메시지내용,보낸시간)로 구성돼있음
            int answer = index % 3;
            if (answer == 1) {
                //보낸사람 항목에 출력 후 다음 줄로
                chatLines.setSender(chatLine);
                index++;
            } else if (answer == 2) {
                //메시지내용 항목에 출력 후 다음 줄로
                chatLines.setMessage(chatLine);
                index++;
            } else {
                //보낸시간 항목에 출력
                chatLines.setSendTime(chatLine);
                // 채팅 기록 List에 저장
                chatHistory.add(chatLines);
                //객체 초기화, 줄(row)인덱스 초기화
                chatLines = new ChatReadDto();
                index = 1;
            }            
        }
        
        return chatHistory;
    }
    
    
    




}
