package com.mariangolea.fintracker.banks.csvparser.ui;

import com.mariangolea.fintracker.banks.csvparser.ui.renderer.TransactionGroupCellRenderer;
import com.mariangolea.fintracker.banks.csvparser.api.transaction.BankTransaction;
import com.mariangolea.fintracker.banks.csvparser.api.transaction.BankTransactionCompanyGroup;
import com.mariangolea.fintracker.banks.csvparser.api.transaction.response.CsvFileParseResponse;
import com.mariangolea.fintracker.banks.csvparser.parsers.BankCSVTransactionParser;
import com.mariangolea.fintracker.banks.csvparser.preferences.UserPreferences;
import com.mariangolea.fintracker.banks.csvparser.preferences.UserPreferencesHandler;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 *
 * @author Marian Golea <mariangolea@gmail.com>
 */
public class CsvParserUI extends Application {

    protected TextFlow feedbackPane;
    private Label inResponseLabel;
    private Label outResponseLabel;
    protected final ObservableList<BankTransactionCompanyGroup> inModel = FXCollections.observableArrayList();
    protected final ObservableList<BankTransactionCompanyGroup> outModel = FXCollections.observableArrayList();
    private ListView<BankTransactionCompanyGroup> listViewIN;
    private ListView<BankTransactionCompanyGroup> listViewOUT;
    private final List<CsvFileParseResponse> parsedTransactionsCopy = new ArrayList<>();
    private final UserPreferencesHandler preferences = UserPreferencesHandler.getInstance();
    private final UserPreferences userPrefs;
    protected final List<File> parsedCsvFiles = new ArrayList<>();
    private Stage primaryStage;
    private BorderPane root;

    protected static final String START_PARSE_MESSAGE = "Started parsing the selected CSV files ...";
    protected static final String FINISHED_PARSING_CSV_FILES = "Finished parsing the CSV files: ";

    public CsvParserUI() {
        userPrefs = preferences.getPreferences();
    }

    @Override
    public void init() throws Exception {
        inResponseLabel = new Label(TransactionGroupListSelectionListener.LABEL_NOTHING_SELECTED);
        outResponseLabel = new Label(TransactionGroupListSelectionListener.LABEL_NOTHING_SELECTED);
        MenuBar menuBar = createMenu();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));
        ColumnConstraints col = new ColumnConstraints();
        col.setFillWidth(true);
        col.setHgrow(Priority.SOMETIMES);
        grid.getColumnConstraints().add(col);
        col.setHgrow(Priority.ALWAYS);
        col.setPercentWidth(80);
        grid.getColumnConstraints().add(col);
        col.setHgrow(Priority.SOMETIMES);
        grid.getColumnConstraints().add(col);
        RowConstraints row = new RowConstraints();
        row.setFillHeight(true);
        row.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().add(row);
        grid.getRowConstraints().add(row);

        ScrollPane centerScroll = createFeedbackView();

        GridPane scrollPaneIN = createTransactionView(BankTransaction.Type.IN);
        GridPane scrollPaneOUT = createTransactionView(BankTransaction.Type.OUT);

        grid.add(inResponseLabel, 0, 0);
        grid.add(scrollPaneIN, 0, 1);
        grid.add(centerScroll, 1, 0, 1, 2);
        grid.add(outResponseLabel, 2, 0);
        grid.add(scrollPaneOUT, 2, 1);
        grid.setStyle("-fx-background-color: BEIGE;");

        root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(grid);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Bank Transactions Merger");
        primaryStage.setScene(new Scene(root, 1000, 600, Color.BISQUE));
        primaryStage.show();
    }

    protected void loadData(final List<CsvFileParseResponse> parsedTransactions) {
        if (parsedTransactions != null) {
            parsedTransactions.forEach(csvFileResponse -> {
                csvFileResponse.parsedTransactionGroups.forEach(transactionGroup -> {
                    switch (transactionGroup.getType()) {
                        case IN:
                            inModel.add(transactionGroup);
                            break;
                        case OUT:
                            outModel.add(transactionGroup);
                            break;
                    }
                });
            });
        }
    }

    public MenuBar createMenu() {
        MenuBar menu = constructSimpleMenuBar();
        Menu file = new Menu("File");
        file.setAccelerator(KeyCombination.keyCombination("Alt+f"));
        MenuItem load = new MenuItem("Load data from CSV file.");
        load.setAccelerator(KeyCombination.keyCombination("Alt+l"));
        load.setOnAction((ActionEvent e) -> {
            popCSVFileChooser();
        });
        file.getItems().add(load);
        menu.getMenus().add(file);
        menu.setUseSystemMenuBar(true);

        return menu;
    }

    public MenuBar constructSimpleMenuBar() {
        return new MenuBar();
    }

    protected void popCSVFileChooser() {
        String inputFolder = userPrefs.getCSVInputFolder();
        FileChooser chooser = new FileChooser();
        File inputFolderFile = inputFolder == null ? null : new File(inputFolder);
        chooser.setInitialDirectory(inputFolderFile == null ? null : inputFolderFile.exists() ? inputFolderFile : null);
        ExtensionFilter filter = new ExtensionFilter(
                "CSV files only", "*.csv");
        chooser.getExtensionFilters().add(filter);
        chooser.setSelectedExtensionFilter(filter);
        List<File> csvFiles = chooser.showOpenMultipleDialog(primaryStage);
        if (csvFiles != null && csvFiles.size() > 0) {
            parseUserSelectedCSVFiles(csvFiles);
        }
    }

    protected void parseUserSelectedCSVFiles(List<File> csvFiles) {
        userPrefs.setCSVInputFolder(csvFiles.get(0).getParent());
        parsedCsvFiles.addAll(csvFiles);
        startParsingCsvFiles(csvFiles);
    }

    protected void startParsingCsvFiles(final List<File> csvFiles) {
        appendReportMessage(START_PARSE_MESSAGE);
        Task<List<CsvFileParseResponse>> parseResponse = new Task<List<CsvFileParseResponse>>() {
            @Override
            protected List<CsvFileParseResponse> call() throws Exception {
                BankCSVTransactionParser fac = new BankCSVTransactionParser();
                final List<CsvFileParseResponse> res = new ArrayList<>();
                csvFiles.stream().map((csvFile) -> fac.parseTransactions(csvFile)).filter((response) -> (response != null)).forEachOrdered((response) -> {
                    res.add(response);
                });
                return res;
            }
        };

        parseResponse.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                appendReportMessage(FINISHED_PARSING_CSV_FILES);
                List<CsvFileParseResponse> values = (List<CsvFileParseResponse>) e.getSource().getValue();
                loadData(values);
                values.forEach((response) -> {
                    appendReportText(response);
                });
                parsedTransactionsCopy.clear();
                parsedTransactionsCopy.addAll(values);
            });
        });
        Thread runThread = new Thread(parseResponse);
        runThread.start();
    }

    private GridPane createTransactionView(BankTransaction.Type type) {
        FilteredList<BankTransactionCompanyGroup> model = new FilteredList<>(BankTransaction.Type.IN == type ? inModel : outModel, s -> true);
        Label responseLabel = BankTransaction.Type.IN == type ? inResponseLabel : outResponseLabel;
        ListView<BankTransactionCompanyGroup> listView = new ListView<>(model);
        if (BankTransaction.Type.IN == type) {
            listViewIN = listView;
        } else {
            listViewOUT = listView;
        }
        model.addListener(new TransactionGroupListSelectionListener(listView, responseLabel));
        listView.setCellFactory(param -> {
            return new TransactionGroupCellRenderer();
        });
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ScrollPane scrollPane = new ScrollPane(listView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        GridPane pane = new GridPane();
        TextField search = new TextField();
        search.setPromptText("Enter company name to filter on");
        search.textProperty().addListener(text -> {
            String filter = search.getText();
            if (filter == null || filter.length() == 0) {
                model.setPredicate(s -> true);
            } else {
                model.setPredicate(s -> s.getCompanyDesc() != null && s.getCompanyDesc().toLowerCase().contains(filter.toLowerCase()));
            }
        });

        pane.add(search, 0, 0, 1, 1);
        pane.add(scrollPane, 0, 1, 3, 12);

        return pane;
    }

    protected ScrollPane createFeedbackView() {
        feedbackPane = constructFeedbackPane();
        ScrollPane scrollPane = new ScrollPane(feedbackPane);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    protected TextFlow constructFeedbackPane() {
        return new TextFlow();
    }

    protected void appendReportText(CsvFileParseResponse fileResponse) {
        appendHyperlinkToFile("\n", fileResponse.csvFile, ": ");
        if (fileResponse.allCsvContentProcessed && fileResponse.expectedTransactionsNumber == fileResponse.foundTransactionsNumber) {
            appendReportMessage("ALL OK!");
        } else if (!fileResponse.allCsvContentProcessed) {
            String message = "Find below CSV content lines unprocessed.\n";
            message = fileResponse.unprocessedStrings.stream().map((line) -> "\t" + line).reduce(message, String::concat);
            appendReportMessage(message);
        } else {
            if (fileResponse.expectedTransactionsNumber == 0) {
                //coould not validate because CSV content did not specify an expected amount.
                appendReportMessage("CSV content did not specify a expected amount. Parser found " + fileResponse.foundTransactionsNumber + " transactions.");
            } else {
                appendReportMessage("Mismatch between expected and found transaction numbers (expected: " + fileResponse.expectedTransactionsNumber + ", found: " + fileResponse.foundTransactionsNumber + ").");
            }
        }
    }

    protected boolean appendHyperlinkToFile(final String pre, final File sourceFile, final String post) {
        if (sourceFile == null) {
            return false;
        }

        if (pre != null && !pre.isEmpty()) {
            feedbackPane.getChildren().add(new Text(pre));
        }

        Hyperlink hyperlink = new Hyperlink(sourceFile.getName());
        hyperlink.setOnMouseClicked((MouseEvent event) -> {
            getHostServices().showDocument(sourceFile.toURI().toString());
        });
        feedbackPane.getChildren().add(hyperlink);

        if (post != null && !post.isEmpty()) {
            feedbackPane.getChildren().add(new Text(post));
        }

        return true;
    }

    protected boolean appendReportMessage(final String message) {
        if (message == null) {
            return false;
        }
        feedbackPane.getChildren().add(new Text(message));
        return true;
    }
}
