package pl.mateuszfrejlich.flashcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flashcards")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CardCollectionEntity {
    @Id
    @Column(length = 100)
    private String name;
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FlashcardEntity> pre = new ArrayList<>();
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FlashcardEntity> arch = new ArrayList<>();
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FlashcardEntity> sec1 = new ArrayList<>();
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FlashcardEntity> sec2 = new ArrayList<>();
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FlashcardEntity> sec3 = new ArrayList<>();
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FlashcardEntity> sec4 = new ArrayList<>();
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<FlashcardEntity> sec5 = new ArrayList<>();

    public CardCollectionEntity(String name) {
        this.name = name;
    }
}
