package pl.mateuszfrejlich.flashcards;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

@Component
public class MainWindow extends JFrame {
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
    private final Controller controller = new Controller();
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
                    JOptionPane.showMessageDialog(null, "The card must be put down!");
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

                final boolean deleted = controller.deleteCollection(collectionObject.toString());
                if (deleted)
                    cbxCollection.removeItem(collectionObject);
                else
                    JOptionPane.showMessageDialog(null, "Internal database error!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cbxCollection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object collectionObject = cbxCollection.getSelectedItem();
                if (collectionObject != null)
                    controller.selectCollection(collectionObject.toString());
            }
        });
        pnFlashcard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                switch (cardState) {
                    case SHOWN -> {
                        cardState = CardState.FLIPPED;
                        lbWord.setText(controller.getActiveCard().getReverseText());
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
                        lbWord.setText(activeCard.getFrontText());
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
                lbWord.setText("Get next");
                cardState = CardState.GUESSED;
                setEnabledStates(true, false);
            }
        });
        btnFailed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.putBorrowedCard(false);
                lbWord.setText("Get next");
                cardState = CardState.GUESSED;
                setEnabledStates(true, false);
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setMinimumSize(new Dimension(800, 570));
        mainPanel.setPreferredSize(new Dimension(800, 570));
        pnOptions = new JPanel();
        pnOptions.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(15, 15, 15, 15), -1, -1));
        mainPanel.add(pnOptions, BorderLayout.NORTH);
        btnNew = new JButton();
        btnNew.setText("New ...");
        pnOptions.add(btnNew, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEdit = new JButton();
        btnEdit.setText("Edit");
        pnOptions.add(btnEdit, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnDelete = new JButton();
        btnDelete.setText("Delete");
        pnOptions.add(btnDelete, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cbxCollection = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        cbxCollection.setModel(defaultComboBoxModel1);
        cbxCollection.setToolTipText("xd");
        pnOptions.add(cbxCollection, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnView = new JPanel();
        pnView.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, false, true));
        mainPanel.add(pnView, BorderLayout.CENTER);
        pnLeft = new JPanel();
        pnLeft.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(20, 20, 20, 20), -1, -1, true, false));
        pnView.add(pnLeft, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(200, -1), new Dimension(200, -1), new Dimension(500, -1), 0, false));
        pnBox = new JPanel();
        pnBox.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 1, new Insets(5, 5, 5, 5), -1, -1, true, false));
        pnLeft.add(pnBox, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(160, -1), new Dimension(160, -1), new Dimension(460, -1), 0, false));
        pnBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        pnSection1 = new JPanel();
        pnSection1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(5, 25, 5, 25), -1, -1));
        pnBox.add(pnSection1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pnSection1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lbSection1 = new JLabel();
        Font lbSection1Font = this.$$$getFont$$$(null, -1, 26, lbSection1.getFont());
        if (lbSection1Font != null) lbSection1.setFont(lbSection1Font);
        lbSection1.setText("0/50");
        pnSection1.add(lbSection1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnSection2 = new JPanel();
        pnSection2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(5, 25, 5, 25), -1, -1));
        pnBox.add(pnSection2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pnSection2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lbSection2 = new JLabel();
        Font lbSection2Font = this.$$$getFont$$$(null, -1, 26, lbSection2.getFont());
        if (lbSection2Font != null) lbSection2.setFont(lbSection2Font);
        lbSection2.setText("0/70");
        pnSection2.add(lbSection2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnSection3 = new JPanel();
        pnSection3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(5, 25, 5, 25), -1, -1));
        pnBox.add(pnSection3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pnSection3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lbSection3 = new JLabel();
        Font lbSection3Font = this.$$$getFont$$$(null, -1, 26, lbSection3.getFont());
        if (lbSection3Font != null) lbSection3.setFont(lbSection3Font);
        lbSection3.setText("0/95");
        pnSection3.add(lbSection3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnSection4 = new JPanel();
        pnSection4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(5, 25, 5, 25), -1, -1));
        pnBox.add(pnSection4, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pnSection4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lbSection4 = new JLabel();
        Font lbSection4Font = this.$$$getFont$$$(null, -1, 26, lbSection4.getFont());
        if (lbSection4Font != null) lbSection4.setFont(lbSection4Font);
        lbSection4.setText("0/130");
        pnSection4.add(lbSection4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnSection5 = new JPanel();
        pnSection5.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(5, 25, 5, 25), -1, -1));
        pnBox.add(pnSection5, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pnSection5.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lbSection5 = new JLabel();
        Font lbSection5Font = this.$$$getFont$$$(null, -1, 26, lbSection5.getFont());
        if (lbSection5Font != null) lbSection5.setFont(lbSection5Font);
        lbSection5.setText("0/155");
        pnSection5.add(lbSection5, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnRight = new JPanel();
        pnRight.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(30, 50, 30, 50), -1, 40));
        pnView.add(pnRight, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pnFlashcard = new JPanel();
        pnFlashcard.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 1, new Insets(50, 50, 50, 50), -1, -1));
        pnRight.add(pnFlashcard, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        pnFlashcard.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(-16777216)), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        lbWord = new JLabel();
        Font lbWordFont = this.$$$getFont$$$(null, -1, 48, lbWord.getFont());
        if (lbWordFont != null) lbWord.setFont(lbWordFont);
        lbWord.setText("Get next");
        pnFlashcard.add(lbWord, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnButtons = new JPanel();
        pnButtons.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 25, 0, 25), 50, -1, true, true));
        pnRight.add(pnButtons, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        btnPassed = new JButton();
        btnPassed.setEnabled(false);
        btnPassed.setText("PASSED");
        pnButtons.add(btnPassed, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnFailed = new JButton();
        btnFailed.setEnabled(false);
        btnFailed.setText("FAILED");
        pnButtons.add(btnFailed, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnUnboxedCards = new JPanel();
        pnUnboxedCards.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new Insets(5, 5, 5, 5), 40, -1, false, true));
        pnRight.add(pnUnboxedCards, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 50), new Dimension(-1, 50), new Dimension(-1, 50), 0, false));
        pnUnboxedCards.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        pnPrepared = new JPanel();
        pnPrepared.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, false, true));
        pnUnboxedCards.add(pnPrepared, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lbPrepared = new JLabel();
        lbPrepared.setText("Prepared:");
        pnPrepared.add(lbPrepared, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnPrepared = new JButton();
        btnPrepared.setEnabled(true);
        btnPrepared.setText("0");
        pnPrepared.add(btnPrepared, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pnArchived = new JPanel();
        pnArchived.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, false, true));
        pnUnboxedCards.add(pnArchived, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lbArchived = new JLabel();
        lbArchived.setText("Archived:");
        pnArchived.add(lbArchived, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnArchived = new JButton();
        btnArchived.setEnabled(true);
        btnArchived.setText("0");
        pnArchived.add(btnArchived, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
