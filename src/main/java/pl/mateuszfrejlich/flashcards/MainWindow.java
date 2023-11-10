package pl.mateuszfrejlich.flashcards;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Stream;

@Component
public class MainWindow extends JFrame {
    private static final float TEXT_SIZE_FACTOR = 0.69F;
    private static final float MAX_TEXT_SIZE = 48.0F;
    private static final float FLASHCARD_MARGIN = 30.0F;
    private Controller controller;
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
    private CardState cardState = CardState.TO_DRAW;

    public MainWindow() {
        try {
            controller = new Controller();
        } catch (Exception e) {
            handleError(e.getMessage());
            dispose();
            return;
        }

        setContentPane(mainPanel);
        setTitle("Flashcards");
        setSize(800, 600);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addListeners();
        refreshCollectionList();

        setVisible(true);
    }

    private static void handleError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void addListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (cardState != CardState.TO_DRAW)
                    controller.putBorrowedCard(false);
                controller.saveChanges();
                super.windowClosing(e);
            }
        });
        btnNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreationDialog();
                cbxCollection.removeAllItems();
                refreshCollectionList();
            }
        });
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cardState != CardState.TO_DRAW) {
                    handleError("The card must be put down!");
                    return;
                }

                Object collectionObject = cbxCollection.getSelectedItem();
                if (collectionObject == null)
                    handleError("No collection selected!");
                else
                    openEditionDialog(collectionObject.toString());
            }
        });
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object collectionObject = cbxCollection.getSelectedItem();
                if (collectionObject == null) {
                    handleError("No collection selected!");
                    return;
                }

                final boolean deleted = controller.deleteCollection();
                if (!deleted) {
                    handleError("Internal database error!");
                    return;
                }

                cbxCollection.removeItem(collectionObject);
                setCardState(CardState.TO_DRAW);
            }
        });
        cbxCollection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object collectionObject = cbxCollection.getSelectedItem();
                if (collectionObject != null) {
                    controller.saveChanges();
                    controller.selectCollection(collectionObject.toString());
                    refreshGroupView();
                }
            }
        });
        pnFlashcard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                handleCardClicked();
            }
        });
        btnPassed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.putBorrowedCard(true);
                setCardState(CardState.TO_DRAW);
            }
        });
        btnFailed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.putBorrowedCard(false);
                setCardState(CardState.TO_DRAW);
            }
        });
        btnPrepared.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseCardContainer(CardGroupChoice.PREPARED);
            }
        });
        btnArchived.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseCardContainer(CardGroupChoice.ARCHIVED);
            }
        });
        pnBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                chooseCardContainer(CardGroupChoice.INBOX);
            }
        });
    }

    private void refreshCollectionList() {
        Stream<String> names = controller.getCollectionNames();
        if (names != null)
            names.forEach(cbxCollection::addItem);
    }

    private void refreshGroupView() {
        btnPrepared.setText(String.valueOf(controller.preparedCardsNumber()));
        btnArchived.setText(String.valueOf(controller.archivedCardsNumber()));
        List<JLabel> sectionLabels = List.of(lbSection1, lbSection2, lbSection3, lbSection4, lbSection5);
        List<Integer> sectionsFilling = controller.boxSectionsFilling();
        List<Integer> sectionSizes = CardBox.sectionSizes();

        if (sectionLabels.size() != sectionsFilling.size()) {
            handleError("Incorrect card box data!");
            return;
        }

        for (int i = 0; i < sectionLabels.size(); ++i) {
            String text = String.valueOf(sectionsFilling.get(i)) + '/' + sectionSizes.get(i);
            sectionLabels.get(i).setText(text);
        }
    }

    private void handleCardClicked() {
        switch (cardState) {
            case REVERSED -> setCardState(CardState.FACE_UP);
            case FACE_UP -> {
                // From that state it is changed by different action
            }
            case TO_DRAW -> setCardState(CardState.REVERSED);
            default -> throw new IllegalStateException("Unexpected value: " + cardState);
        }
    }

    private void setCardState(CardState state) {
        switch (state) {
            case REVERSED -> {
                controller.processNextCard();
                Flashcard activeCard = controller.getActiveCard();
                if (activeCard == null)
                    return;

                setCardText(activeCard.reverseText());
                setEnabledStates(false, false);
            }
            case FACE_UP -> {
                setCardText(controller.getActiveCard().frontText());
                setEnabledStates(false, true);
            }
            case TO_DRAW -> {
                setCardText("Get next");
                setEnabledStates(true, false);
                refreshGroupView();
            }
            default -> throw new IllegalStateException("Unexpected value: " + state);
        }
        cardState = state;
    }

    private void setCardText(String text) {
        final float calcSize = TEXT_SIZE_FACTOR * (pnFlashcard.getWidth() - 2.0F * FLASHCARD_MARGIN) / text.length();
        final float textSize = Math.min(calcSize, MAX_TEXT_SIZE);
        lbWord.setText(text);
        lbWord.setFont(lbWord.getFont().deriveFont(textSize));
    }

    private void setEnabledStates(boolean cardsChoice, boolean cardRedirection) {
        btnPrepared.setEnabled(cardsChoice);
        btnArchived.setEnabled(cardsChoice);
        btnPassed.setEnabled(cardRedirection);
        btnFailed.setEnabled(cardRedirection);
    }

    private void chooseCardContainer(CardGroupChoice choice) {
        if (controller.getCardGroupChoice() == choice || cardState != CardState.TO_DRAW)
            return;

        controller.setCardGroupChoice(choice);

        Color selectedColor = new Color(20, 100, 20);
        Color normalColor = new Color(20, 20, 20);
        lbPrepared.setForeground(choice == CardGroupChoice.PREPARED ? selectedColor : normalColor);
        lbArchived.setForeground(choice == CardGroupChoice.ARCHIVED ? selectedColor : normalColor);
        pnBox.setBorder(new LineBorder(choice == CardGroupChoice.INBOX ? selectedColor : normalColor));

        switch (choice) {
            case PREPARED -> {
                btnPassed.setText("REMEMBERED");
                btnFailed.setText("KEEP");
            }
            case INBOX -> {
                btnPassed.setText("PASSED");
                btnFailed.setText("FAILED");
            }
            case ARCHIVED -> {
                btnPassed.setText("PRESERVE");
                btnFailed.setText("REMOVE");
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
