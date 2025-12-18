package com.chatapp.repository;

import com.chatapp.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId, Pageable pageable);

    @Modifying
    @Query("UPDATE Message m SET m.status = :status " +
           "WHERE m.chatRoom.id = :chatRoomId " +
           "AND m.sender.id != :userId " +
           "AND m.status != 'READ'")
    void updateMessageStatusForChatRoom(
        @Param("chatRoomId") Long chatRoomId,
        @Param("userId") Long userId,
        @Param("status") com.chatapp.model.MessageStatus status
    );
}