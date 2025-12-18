package com.chatapp.repository;

import com.chatapp.model.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    @Query("SELECT m FROM ChatRoomMember m WHERE m.chatRoom.id = :chatRoomId")
    List<ChatRoomMember> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    @Query("SELECT m FROM ChatRoomMember m WHERE m.user.id = :userId")
    List<ChatRoomMember> findByUserId(@Param("userId") Long userId);

    boolean existsByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}