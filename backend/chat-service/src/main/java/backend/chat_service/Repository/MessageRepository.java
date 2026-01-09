package backend.chat_service.repository;
import backend.chat_service.entity.MessageEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository
        extends JpaRepository<MessageEntity, UUID> {

    List<MessageEntity> findBySenderIdAndReceiverIdOrderByCreatedAtAsc(
        UUID senderId,
        UUID receiverId
    );
        }