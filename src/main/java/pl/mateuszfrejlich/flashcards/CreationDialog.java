package pl.mateuszfrejlich.flashcards;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;

public class CreationDialog extends JDialog {
    private final Controller controller;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tfName;
    private JTextField tfPath;
    private JButton btnPath;
    private JLabel lbName;
    private JLabel lbPath;
    private JPanel pnName;
    private JPanel pnPath;
    private JPanel pnForm;
    private JPanel pnButtons;
    private JPanel pnAction;
    private JCheckBox cbEmpty;

    public CreationDialog(Controller controller) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setMinimumSize(new Dimension(400, 200));
        setTitle("Creation dialog");
        this.controller = controller;

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
        btnPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useFileChoiceDialog();
            }
        });
        cbEmpty.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setEnabledPathInput(e.getStateChange() == ItemEvent.DESELECTED);
            }
        });
    }

    private void useFileChoiceDialog() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter fileFilter = new FileNameExtensionFilter(".csv, .txt", "txt", "csv");
        fileChooser.setFileFilter(fileFilter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            tfPath.setText(fileChooser.getSelectedFile().getPath());
    }

    private void setEnabledPathInput(boolean enabled) {
        lbPath.setEnabled(enabled);
        tfPath.setEnabled(enabled);
        btnPath.setEnabled(enabled);
    }

    private boolean addNewCollection() {
        if (tfPath.isEnabled())
            return controller.addNewCollection(tfName.getText().trim(), tfPath.getText().trim());
        else
            return controller.addNewCollection(tfName.getText().trim());
    }

    private void onOK() {
        final boolean created = addNewCollection();

        if (created)
            dispose();
        else
            JOptionPane.showMessageDialog(null, "Invalid data!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void onCancel() {
        dispose();
    }
}
