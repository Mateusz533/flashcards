package pl.mateuszfrejlich.flashcards.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.mateuszfrejlich.flashcards.entity.CardCollectionEntity;

@Repository
public interface FlashcardsRepository extends JpaRepository<CardCollectionEntity, String> {
}
