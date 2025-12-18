package com.chatapp.repository;

import com.chatapp.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr " +
            "JOIN cr.members m1 " +
            "JOIN cr.members m2 " +
            "WHERE m1.user.id = :user1Id AND m2.user.id = :user2Id " +
            "AND (SELECT COUNT(m) FROM ChatRoomMember m WHERE m.chatRoom = cr) = 2")
    Optional<ChatRoom> findDirectChatBetweenUsers(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );

    @Query("SELECT DISTINCT cr FROM ChatRoom cr " +
            "JOIN cr.members m " +
            "WHERE m.user.id = :userId " +
            "ORDER BY cr.createdAt DESC")
    List<ChatRoom> findAllByUserId(@Param("userId") Long userId);
}
