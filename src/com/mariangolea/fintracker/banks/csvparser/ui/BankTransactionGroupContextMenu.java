/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mariangolea.fintracker.banks.csvparser.ui;

import com.mariangolea.fintracker.banks.csvparser.ui.edit.BankTransactionGroupEditHandler;
import com.mariangolea.fintracker.banks.csvparser.api.transaction.BankTransactionCompanyGroup;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Marian
 */
public class BankTransactionGroupContextMenu extends ContextMenu {

    BankTransactionCompanyGroup group;
    private final BankTransactionGroupEditHandler editHandler;

    public BankTransactionGroupContextMenu(final BankTransactionGroupEditHandler editHandler) {
        this.editHandler = editHandler;
        getItems().add(constructEditMenu());
    }

    public void setBankTransactionGroup(final BankTransactionCompanyGroup group) {
        this.group = group;
    }

    protected final MenuItem constructEditMenu() {
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(e -> {
            edit();
        });
        return edit;
    }

    protected final void edit() {
        if (group == null) {
            return;
        }

        editHandler.editGroup(group);
    }
}
