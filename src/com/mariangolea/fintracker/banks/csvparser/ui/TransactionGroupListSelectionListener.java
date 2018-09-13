package com.mariangolea.fintracker.banks.csvparser.ui;

import com.mariangolea.fintracker.banks.csvparser.api.transaction.BankTransactionAbstractGroup;
import java.math.BigDecimal;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Updates amount based on existing selections. If none are made, all
 * transactions are summed up.
 *
 * @author mariangolea@gmail.com
 */
public class TransactionGroupListSelectionListener implements ListDataListener, ListSelectionListener {

    private static final String LABEL_NOTHING_SELECTED = "Total Amount: ";
    private static final String LABEL_SOMETHING_SELECTED = "Selected Amount: ";

    private final JList<BankTransactionAbstractGroup> jList;
    private final JTextPane amountArea;

    public TransactionGroupListSelectionListener(JList<BankTransactionAbstractGroup> jList, JTextPane amountArea) {
        this.jList = jList;
        this.amountArea = amountArea;
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        updateAmount();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        updateAmount();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        updateAmount();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        updateAmount();
    }

    private void updateAmount() {
        int[] selectedIndices = jList.getSelectedIndices();
        BigDecimal amount = BigDecimal.ZERO;
        if (selectedIndices == null || selectedIndices.length < 1) {
            int size = jList.getModel().getSize();
            for (int i = 0; i < size; i++) {
                amount= amount.add(jList.getModel().getElementAt(i).getTotalAmount());
            }
            amountArea.setText(LABEL_NOTHING_SELECTED + amount);
        } else {
            for (int index : selectedIndices) {
                amount= amount.add(jList.getModel().getElementAt(index).getTotalAmount());
            }
            amountArea.setText(LABEL_SOMETHING_SELECTED + amount);
        }

    }

}