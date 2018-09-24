package com.mariangolea.fintracker.banks.csvparser.ui;

import com.mariangolea.fintracker.banks.csvparser.api.transaction.BankTransactionAbstractGroup;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

/**
 * @author mariangolea@gmail.com
 */
public class TransactionGroupCellRenderer extends ListCell<BankTransactionAbstractGroup> {

    private final ListView<BankTransactionAbstractGroup> param;

    public TransactionGroupCellRenderer(ListView<BankTransactionAbstractGroup> param) {
        this.param = param;
    }

    @Override
    protected void updateItem(BankTransactionAbstractGroup value, boolean empty) {
        super.updateItem(value, empty);
        if (empty) {
            return;
        }
        String[] split = value.toString().split("\n");
        String text = "";
        for (String split1 : split) {
            text += split1;
        }
        setText(text);

        setStyle("-fx-background-color: lavender; selected: skyblue");
    }
}
