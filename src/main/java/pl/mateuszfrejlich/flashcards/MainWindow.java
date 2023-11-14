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
    private CardCollection activeCollection = null;
    private CollectionsManager collectionsManager;
    private CardState cardState = CardState.ABSENT;
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

    public MainWindow() {
        try {
            collectionsManager = new CollectionsManager();
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

        setCardState(CardState.ABSENT);
        refreshCollectionList();

        setVisible(true);
    }

    private static void handleError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void handleNewClicked() {
        openCreationDialog();
        cbxCollection.removeAllItems();
        refreshCollectionList();
    }

    private void handleEditClicked() {
        if (activeCollection == null)
            handleError("No collection selected!");
        else
            openEditionDialog();
    }

    private void handleDeleteClicked() {
        if (activeCollection == null) {
            handleError("No collection selected!");
            return;
        }

        String collectionName = activeCollection.getName();
        String msg = "Are you sure you want to permanently delete collection '" + collectionName + "'?";
        int response = JOptionPane.showConfirmDialog(null, msg, "Confirm deletion", JOptionPane.YES_NO_OPTION);
        if (response != JOptionPane.YES_OPTION)
            return;

        deleteCollection(collectionName);
    }

    private void handleCollectionChanged() {
        Object collectionObject = cbxCollection.getSelectedItem();
        if (collectionObject != null) {
            if (activeCollection != null)
                activeCollection.saveChanges();

            chooseCardContainer(CardGroupChoice.UNSELECTED);
            activeCollection = collectionsManager.getCollection(collectionObject.toString());
            setCardState(CardState.TO_DRAW);
        } else
            setPanelEnabled(pnView, false);
    }

    private void handlePreparedClicked() {
        if (cardState == CardState.TO_DRAW)
            chooseCardContainer(CardGroupChoice.PREPARED);
    }

    private void handleBoxClicked() {
        if (cardState == CardState.TO_DRAW)
            chooseCardContainer(CardGroupChoice.INBOX);
    }

    private void handleArchivedClicked() {
        if (cardState == CardState.TO_DRAW)
            chooseCardContainer(CardGroupChoice.ARCHIVED);
    }

    private void handlePassedClicked() {
        activeCollection.putBorrowedCard(true);
        setCardState(CardState.TO_DRAW);
    }

    private void handleFailedClicked() {
        activeCollection.putBorrowedCard(false);
        setCardState(CardState.TO_DRAW);
    }

    private void handleCardClicked() {
        switch (cardState) {
            case TO_DRAW -> setCardState(CardState.REVERSED);
            case REVERSED -> setCardState(CardState.FACE_UP);
            case FACE_UP, ABSENT -> {
                // From that state it is changed by different action
            }
            default -> throw new IllegalStateException("Unexpected value: " + cardState);
        }
    }

    private void handleWindowClosing() {
        if (activeCollection == null)
            return;

        if (activeCollection.getActiveCard() != null)
            activeCollection.putBorrowedCard(false);

        activeCollection.saveChanges();
    }

    private void deleteCollection(String collectionName) {
        final boolean deleted = collectionsManager.deleteCollection(collectionName);
        if (!deleted) {
            handleError("Request to database has caused an error! Collection wasn't delete properly!");
            return;
        }

        activeCollection = null;
        chooseCardContainer(CardGroupChoice.UNSELECTED);
        setCardState(CardState.ABSENT);
        cbxCollection.removeItem(cbxCollection.getSelectedItem());
    }

    private void setCardState(CardState state) {
        switch (state) {
            case REVERSED -> {
                activeCollection.processNextCard();
                Flashcard activeCard = activeCollection.getActiveCard();
                if (activeCard == null)
                    return;

                setCardText(activeCard.reverseText());
                setEnabledStates(false, false);
            }
            case FACE_UP -> {
                setCardText(activeCollection.getActiveCard().frontText());
                setEnabledStates(false, true);
            }
            case TO_DRAW -> {
                setCardText("Get next");
                setEnabledStates(true, false);
                refreshGroupView();
            }
            case ABSENT -> {
                setCardText("Select collection");
                setPanelEnabled(pnView, false);
                refreshGroupView();
            }
            default -> throw new IllegalStateException("Unexpected value: " + state);
        }
        cardState = state;
    }

    private void chooseCardContainer(CardGroupChoice choice) {
        if (activeCollection != null) {
            if (activeCollection.getCardGroupChoice() == choice)
                return;
            activeCollection.setCardGroupChoice(choice);
        }

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
            case INBOX, UNSELECTED -> {
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

    private void setEnabledStates(boolean cardGroupChoice, boolean cardRedirection) {
        setPanelEnabled(pnOptions, cardGroupChoice);
        setPanelEnabled(pnBox, cardGroupChoice);
        setPanelEnabled(pnUnboxedCards, cardGroupChoice);
        setPanelEnabled(pnButtons, cardRedirection);
        setPanelEnabled(pnFlashcard, cardGroupChoice || !cardRedirection);
    }

    private void setCardText(String text) {
        final float calcSize = TEXT_SIZE_FACTOR * (pnFlashcard.getWidth() - 2.0F * FLASHCARD_MARGIN) / text.length();
        final float textSize = Math.min(calcSize, MAX_TEXT_SIZE);
        lbWord.setText(text);
        lbWord.setFont(lbWord.getFont().deriveFont(textSize));
    }

    private void refreshCollectionList() {
        Stream<String> names = collectionsManager.getCollectionNames();
        if (names != null) {
            cbxCollection.removeAllItems();
            names.forEach(cbxCollection::addItem);
        }
    }

    private void refreshGroupView() {
        List<JLabel> sectionLabels = List.of(lbSection1, lbSection2, lbSection3, lbSection4, lbSection5);
        List<Integer> sectionSizes = CardBox.sectionSizes();
        final int numberOfPreparedCards;
        final int numberOfArchivedCards;
        List<Integer> sectionsFilling;

        if (activeCollection == null) {
            numberOfPreparedCards = 0;
            numberOfArchivedCards = 0;
            sectionsFilling = List.of(0, 0, 0, 0, 0);
        } else {
            numberOfPreparedCards = activeCollection.numberOfPreparedCards();
            numberOfArchivedCards = activeCollection.numberOfArchivedCards();
            sectionsFilling = activeCollection.boxSectionsFilling();
        }

        if (sectionLabels.size() != sectionsFilling.size()) {
            handleError("Incorrect card box data!");
            return;
        }

        btnPrepared.setText(String.valueOf(numberOfPreparedCards));
        btnArchived.setText(String.valueOf(numberOfArchivedCards));

        for (int i = 0; i < sectionLabels.size(); ++i) {
            String text = String.valueOf(sectionsFilling.get(i)) + '/' + sectionSizes.get(i);
            sectionLabels.get(i).setText(text);
        }
    }

    private void setPanelEnabled(JPanel panel, Boolean isEnabled) {
        panel.setEnabled(isEnabled);
        java.awt.Component[] components = panel.getComponents();

        for (java.awt.Component component : components) {
            if (component instanceof JPanel) {
                setPanelEnabled((JPanel) component, isEnabled);
            }
            component.setEnabled(isEnabled);
        }
    }

    private void openCreationDialog() {
        new CreationDialog(collectionsManager).setVisible(true);
    }

    private void openEditionDialog() {
        new EditionDialog(activeCollection).setVisible(true);
    }

    private void addListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
                super.windowClosing(e);
            }
        });
        btnNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewClicked();
            }
        });
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEditClicked();
            }
        });
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeleteClicked();
            }
        });
        cbxCollection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCollectionChanged();
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
                handlePassedClicked();
            }
        });
        btnFailed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleFailedClicked();
            }
        });
        btnPrepared.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePreparedClicked();
            }
        });
        btnArchived.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleArchivedClicked();
            }
        });
        pnBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                handleBoxClicked();
            }
        });
    }
}
