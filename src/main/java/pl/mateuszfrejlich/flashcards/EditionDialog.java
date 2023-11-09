package pl.mateuszfrejlich.flashcards;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.stream.Stream;

public class EditionDialog extends JDialog {
    private final Controller controller;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox cbxItem;
    private JTextField tfFront;
    private JTextField tfReverse;
    private JButton btnAdd;
    private JButton btnSwapSides;
    private JPanel pnAction;
    private JPanel pnForm;
    private JLabel lbItem;
    private JLabel lbFront;
    private JLabel lbReverse;
    private JPanel pnItem;
    private JPanel pnFront;
    private JPanel pnReverse;
    private JPanel pnControl;
    private JPanel pnButtons;
    private JButton btnUpdate;
    private JButton btnDelete;
    private CollectionEditor editor;
    private boolean isSwapped = false;

    public EditionDialog(String name, Controller controller) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setMinimumSize(new Dimension(400, 300));
        setTitle("Edition dialog");
        this.controller = controller;
        this.editor = controller.createEditor();
        pnForm.setBorder(new TitledBorder(pnForm.getBorder(), name));
        refreshWordList();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        btnSwapSides.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isSwapped = !isSwapped;
                refreshWordList();
            }
        });
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Flashcard card = new Flashcard(tfFront.getText().trim(), tfReverse.getText().trim());
                final boolean updated = editor.addCard(card);

                if (updated)
                    cbxItem.addItem(formatCardText(card));
                else
                    JOptionPane.showMessageDialog(null, "Invalid data", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int index = cbxItem.getSelectedIndex();

                if (index == -1)
                    JOptionPane.showMessageDialog(null, "No selected item!", "Error", JOptionPane.ERROR_MESSAGE);
                else
                    updateItem(index);
            }
        });
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int index = cbxItem.getSelectedIndex();

                if (index == -1)
                    JOptionPane.showMessageDialog(null, "No selected item!", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    editor.deleteCard(index);
                    cbxItem.removeItemAt(index);
                    fillTextFields();
                }
            }
        });
        cbxItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillTextFields();
            }
        });
    }

    private void fillTextFields() {
        final int index = cbxItem.getSelectedIndex();
        if (index == -1)
            return;

        Flashcard card = editor.getCard(index);
        tfFront.setText(card.frontText());
        tfReverse.setText(card.reverseText());
    }

    private void clearTextFields() {
        tfFront.setText("");
        tfReverse.setText("");
    }

    private void refreshWordList() {
        Stream<Flashcard> items = editor.getCards();
        cbxItem.removeAllItems();
        items.forEach(item -> cbxItem.addItem(formatCardText(item)));
    }

    private void updateItem(int index) {
        Flashcard card = new Flashcard(tfFront.getText().trim(), tfReverse.getText().trim());
        final boolean updated = editor.updateCard(index, card);

        if (updated) {
            cbxItem.removeItemAt(index);
            cbxItem.insertItemAt(formatCardText(card), index);
            clearTextFields();
        } else
            JOptionPane.showMessageDialog(null, "Invalid data!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private String formatCardText(Flashcard card) {
        if (isSwapped)
            return card.reverseText() + '/' + card.frontText();
        else
            return card.frontText() + '/' + card.reverseText();
    }

    private void onOK() {
        controller.executeEdition(editor);
        dispose();
    }

    private void onCancel() {
        editor = null;
        dispose();
    }
}
