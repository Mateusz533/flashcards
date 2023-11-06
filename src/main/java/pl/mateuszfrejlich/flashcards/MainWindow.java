package pl.mateuszfrejlich.flashcards;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class MainWindow extends JFrame {
    private static final float TEXT_SIZE_FACTOR = 0.69F;
    private static final float MAX_TEXT_SIZE = 48.0F;
    private static final float FLASHCARD_MARGIN = 30.0F;
    private final Controller controller = new Controller();
    private JPanel mainPanel;
    private JButton btnNew;
    private JButton btnEdit;
    private JButton btnDelete;
    private JPanel pnOptions;
    private JPanel pnView;
    private JPanel pnLeft;
    private JPanel pnRight;
    private JPanel pnFlashcard;
    private JLabel lbWord;
    private JLabel lbSection1;
    private JLabel lbSection2;
    private JLabel lbSection3;
    private JLabel lbSection4;
    private JLabel lbSection5;
    private JComboBox cbxCollection;
    private JPanel pnBox;
    private JPanel pnSection1;
    private JPanel pnSection2;
    private JPanel pnSection3;
    private JPanel pnSection4;
    private JPanel pnSection5;
    private JButton btnPassed;
    private JButton btnFailed;
    private JPanel pnButtons;
    private JButton btnPrepared;
    private JButton btnArchived;
    private JLabel lbPrepared;
    private JLabel lbArchived;
    private JPanel pnPrepared;
    private JPanel pnArchived;
    private JPanel pnUnboxedCards;
    private CardState cardState = CardState.GUESSED;

    public MainWindow() {
        setContentPane(mainPanel);
        setTitle("Flashcards");
        setSize(800, 600);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btnNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreationDialog();
                cbxCollection.removeAllItems();
                controller.getCollectionNames().forEach(name -> cbxCollection.addItem(name));
            }
        });
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cardState != CardState.GUESSED) {
                    JOptionPane.showMessageDialog(null, "The card must be put down!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Object collectionObject = cbxCollection.getSelectedItem();
                if (collectionObject == null)
                    JOptionPane.showMessageDialog(null, "No collection selected!", "Error", JOptionPane.ERROR_MESSAGE);
                else
                    openEditionDialog(collectionObject.toString());
            }
        });
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object collectionObject = cbxCollection.getSelectedItem();
                if (collectionObject == null) {
                    JOptionPane.showMessageDialog(null, "No collection selected!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                final boolean deleted = controller.deleteCollection();
                if (!deleted) {
                    JOptionPane.showMessageDialog(null, "Internal database error!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                cbxCollection.removeItem(collectionObject);
                setGuessedState();
            }
        });
        cbxCollection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object collectionObject = cbxCollection.getSelectedItem();
                if (collectionObject != null){
                    controller.selectCollection(collectionObject.toString());
                    btnPrepared.setText(String.valueOf(controller.preparedCardsNumber()));
                    btnArchived.setText(String.valueOf(controller.archivedCardsNumber()));
                }
            }
        });
        pnFlashcard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                switch (cardState) {
                    case SHOWN -> {
                        cardState = CardState.FLIPPED;
                        setCardText(controller.getActiveCard().getFrontText());
                        setEnabledStates(false, true);
                    }
                    case FLIPPED -> {
                        // State changing by button
                    }
                    case GUESSED -> {
                        controller.processNextCard();
                        Flashcard activeCard = controller.getActiveCard();
                        if (activeCard == null)
                            return;

                        cardState = CardState.SHOWN;
                        setCardText(activeCard.getReverseText());
                        setEnabledStates(false, false);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + cardState);
                }
            }
        });
        btnPassed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.putBorrowedCard(true);
                setGuessedState();
            }
        });
        btnFailed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.putBorrowedCard(false);
                setGuessedState();
            }
        });
        btnPrepared.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseCardContainer(CardsChoice.PREPARED);
            }
        });
        btnArchived.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseCardContainer(CardsChoice.ARCHIVED);
            }
        });
        pnBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                chooseCardContainer(CardsChoice.INBOX);
            }
        });
        setVisible(true);
    }

    private void setGuessedState() {
        setCardText("Get next");
        cardState = CardState.GUESSED;
        setEnabledStates(true, false);
    }

    private void setCardText(String text) {
        final float calcSize = TEXT_SIZE_FACTOR * (pnFlashcard.getWidth() - 2.0F * FLASHCARD_MARGIN) / text.length();
        final float textSize = (calcSize < MAX_TEXT_SIZE) ? calcSize : MAX_TEXT_SIZE;
        lbWord.setText(text);
        lbWord.setFont(lbWord.getFont().deriveFont(textSize));
    }

    private void setEnabledStates(boolean cardsChoice, boolean cardRedirection) {
        btnPrepared.setEnabled(cardsChoice);
        btnArchived.setEnabled(cardsChoice);
        btnPassed.setEnabled(cardRedirection);
        btnFailed.setEnabled(cardRedirection);
    }

    private void chooseCardContainer(CardsChoice choice) {
        if (controller.getCardChoice() == choice || cardState != CardState.GUESSED)
            return;

        controller.setCardsChoice(choice);

        Color selectedColor = new Color(20, 100, 20);
        Color normalColor = new Color(20, 20, 20);
        lbPrepared.setForeground(choice == CardsChoice.PREPARED ? selectedColor : normalColor);
        lbArchived.setForeground(choice == CardsChoice.ARCHIVED ? selectedColor : normalColor);
        pnBox.setBorder(new LineBorder(choice == CardsChoice.INBOX ? selectedColor : normalColor));

        switch (choice) {
            case PREPARED -> {
                btnPassed.setText("REMEMBERED");
                btnFailed.setText("KEEP");
            }
            case ARCHIVED -> {
                btnPassed.setText("PRESERVE");
                btnFailed.setText("REMOVE");
            }
            case INBOX -> {
                btnPassed.setText("PASSED");
                btnFailed.setText("FAILED");
            }
            default -> throw new IllegalStateException("Unexpected value: " + choice);
        }
    }

    private void openCreationDialog() {
        new CreationDialog(controller).setVisible(true);
    }

    private void openEditionDialog(String name) {
        new EditionDialog(name, controller).setVisible(true);
    }
}
