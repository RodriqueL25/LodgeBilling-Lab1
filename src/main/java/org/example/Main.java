package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

public class Main extends Application {

    private static final String BLUE = "#2c5d94";

    // ---------- App state ----------
    private Stage stage;
    private Scene appScene;
    private LoginView loginView;
    private TabPane tabs;

    private final ObservableList<Bill> bills = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private int billCounter = 0;
    private int reservationCounter = 0;
    private Reservation activeReservation = null; // reservation being checked in

    // ---------- Header ----------
    private final Label userLabel = new Label();

    // ---------- Billing tab ----------
    private final TableView<Bill> billTable = new TableView<>();
    private final TextField guestField = new TextField();
    private final ComboBox<String> roomCombo = new ComboBox<>();
    private final TextField nightsField = new TextField("1");
    private final TextField extrasField = new TextField("0");
    private final TextField paidField = new TextField("0");
    private final TextArea notesArea = new TextArea();
    private final Label billStatus = new Label();

    // ---------- Reservations tab ----------
    private final TableView<Reservation> reservationTable = new TableView<>();
    private final TextField resGuestField = new TextField();
    private final ComboBox<String> resRoomCombo = new ComboBox<>();
    private final DatePicker resDatePicker = new DatePicker(LocalDate.now());
    private final TextField resNightsField = new TextField("1");
    private final Label resStatus = new Label();

    // =====================================================================
    //  Start / login / logout
    // =====================================================================

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        loginView = new LoginView(this::showApp);
        appScene = new Scene(buildAppRoot(), 1000, 750);

        stage.setTitle("Lodge Billing Lab");
        stage.setScene(loginView.getScene());
        stage.show();
    }

    private void showApp() {
        userLabel.setText("Logged in as: " + LoginView.getCurrentUser());
        stage.setScene(appScene);
    }

    private void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Log out now? Saved bills stay until you close the app.",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.setTitle("Logout");
        confirm.showAndWait().ifPresent(choice -> {
            if (choice == ButtonType.YES) {
                LoginView.logout();
                loginView.clear();
                stage.setScene(loginView.getScene());
            }
        });
    }

    // =====================================================================
    //  Layout
    // =====================================================================

    private BorderPane buildAppRoot() {
        BorderPane root = new BorderPane();
        root.setTop(buildHeader());

        tabs = new TabPane(buildBillingTab(), buildReservationTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.setCenter(tabs);
        return root;
    }

    private HBox buildHeader() {
        Label title = new Label("Lodge Billing Lab");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        userLabel.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> logout());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());
        MenuButton fileMenu = new MenuButton("File", null, logoutItem, exitItem);

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> info("About", "Lodge Billing Lab\nICT261 practice project"));
        MenuButton helpMenu = new MenuButton("Help", null, aboutItem);

        HBox header = new HBox(15, title, spacer, userLabel, fileMenu, helpMenu);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 15, 12, 15));
        header.setStyle("-fx-background-color: " + BLUE + ";");
        return header;
    }

    // =====================================================================
    //  Billing tab
    // =====================================================================

    private Tab buildBillingTab() {
        roomCombo.getItems().addAll("Standard", "Deluxe", "Family");
        roomCombo.setValue("Deluxe");
        roomCombo.setMaxWidth(Double.MAX_VALUE);
        guestField.setPromptText("Guest name");
        notesArea.setPromptText("Notes...");
        notesArea.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        addRow(grid, 0, "Guest name:", guestField);
        addRow(grid, 1, "Room type:", roomCombo);
        addRow(grid, 2, "Nights:", nightsField);
        addRow(grid, 3, "Extras in K:", extrasField);
        addRow(grid, 4, "Amount paid in K:", paidField);
        addRow(grid, 5, "Notes:", notesArea);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(130);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        Button calcBtn = new Button("Calculate");
        calcBtn.setOnAction(e -> calculate());

        Button saveBtn = new Button("Save bill");
        saveBtn.setStyle("-fx-background-color: #367ce8; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> saveBill());

        billStatus.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        billStatus.setWrapText(true);

        VBox form = new VBox(15, sectionTitle("New bill"), grid, new HBox(15, calcBtn, saveBtn), billStatus);
        HBox.setHgrow(form, Priority.ALWAYS);

        HBox top = new HBox(30, form, buildPriceBox());

        // Toolbar
        Button newGuestBtn = new Button("New guest");
        newGuestBtn.setOnAction(e -> {
            clearBillForm();
            billStatus.setText("");
        });

        Button receiptBtn = new Button("View receipt");
        receiptBtn.setOnAction(e -> {
            Bill selected = billTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                setStatus(billStatus, "Select a bill in the table first.", false);
            } else {
                showReceipt(selected);
            }
        });

        Button deleteBtn = new Button("Delete selected bill");
        deleteBtn.setOnAction(e -> deleteSelectedBill());

        HBox toolbar = new HBox(10, newGuestBtn, receiptBtn, deleteBtn);

        setupBillTable();
        billTable.setPrefHeight(220);

        VBox content = new VBox(15, toolbar, top, sectionTitle("Saved bills"), billTable);
        content.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return new Tab("Billing", scroll);
    }

    private VBox buildPriceBox() {
        VBox box = new VBox(10,
                sectionTitle("Practice room prices"),
                new Label(String.format("Standard:  K %.2f per night", Bill.rateFor("Standard"))),
                new Label(String.format("Deluxe:  K %.2f per night", Bill.rateFor("Deluxe"))),
                new Label(String.format("Family:  K %.2f per night", Bill.rateFor("Family"))),
                new Separator(),
                wrapped("5 nights or more: 5% off the room charge."),
                wrapped("Extras get no discount. No tax in this lab."),
                wrapped("Saved bills last only until you close the app."));
        box.setPadding(new Insets(20));
        box.setMinWidth(300);
        box.setMaxWidth(300);
        box.setMaxHeight(VBox.USE_PREF_SIZE);
        box.setStyle("-fx-background-color: #f4f8fe; -fx-border-color: #dce7f5;");
        return box;
    }

    private void setupBillTable() {
        TableColumn<Bill, String> idCol = column("Bill", "billId");
        TableColumn<Bill, String> guestCol = column("Guest", "guestName");
        TableColumn<Bill, String> roomCol = column("Room", "roomType");
        TableColumn<Bill, Integer> nightsCol = column("Nights", "nights");
        TableColumn<Bill, Double> totalCol = moneyColumn("Total", "totalCost");
        TableColumn<Bill, Double> paidCol = moneyColumn("Paid", "amountPaid");
        TableColumn<Bill, Double> balCol = moneyColumn("Balance", "balance");

        billTable.getColumns().addAll(idCol, guestCol, roomCol, nightsCol, totalCol, paidCol, balCol);
        billTable.setItems(bills);
        billTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        billTable.setPlaceholder(new Label("No bills saved yet."));
    }

    // ----- Billing actions -----

    // Reads the form. Returns null (and shows a message) if something is wrong.
    private Bill buildBillFromForm(String billId) {
        String guest = guestField.getText().trim();
        if (guest.isEmpty()) {
            setStatus(billStatus, "Guest name is required.", false);
            return null;
        }
        try {
            int nights = Integer.parseInt(nightsField.getText().trim());
            double extras = Double.parseDouble(extrasField.getText().trim());
            double paid = Double.parseDouble(paidField.getText().trim());

            if (nights < 1) {
                setStatus(billStatus, "Nights must be at least 1.", false);
                return null;
            }
            if (extras < 0 || paid < 0) {
                setStatus(billStatus, "Extras and Paid cannot be negative.", false);
                return null;
            }
            return new Bill(billId, guest, roomCombo.getValue(), nights, extras, paid,
                    notesArea.getText().trim());
        } catch (NumberFormatException ex) {
            setStatus(billStatus, "Nights must be a whole number. Extras and Paid must be numbers.", false);
            return null;
        }
    }

    private void calculate() {
        Bill preview = buildBillFromForm("-");
        if (preview == null) {
            return;
        }
        setStatus(billStatus, String.format(
                "Room K %.2f - discount K %.2f + extras K %.2f = Total K %.2f  |  Balance K %.2f",
                preview.getRoomCharge(), preview.getDiscount(), preview.getExtras(),
                preview.getTotalCost(), preview.getBalance()), true);
    }

    private void saveBill() {
        Bill bill = buildBillFromForm(String.format("B%03d", billCounter + 1));
        if (bill == null) {
            return;
        }
        billCounter++;
        bills.add(bill);
        billTable.scrollTo(bill);
        billTable.getSelectionModel().select(bill);

        if (activeReservation != null) {
            activeReservation.setStatus(Reservation.CHECKED_IN);
            reservationTable.refresh();
        }

        setStatus(billStatus, String.format("Saved %s. Balance: K %.2f", bill.getBillId(), bill.getBalance()), true);
        showReceipt(bill);
        clearBillForm();
    }

    private void deleteSelectedBill() {
        Bill selected = billTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus(billStatus, "Select a bill in the table first.", false);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete bill " + selected.getBillId() + " for " + selected.getGuestName() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(choice -> {
            if (choice == ButtonType.YES) {
                bills.remove(selected);
                setStatus(billStatus, "Bill deleted.", true);
            }
        });
    }

    private void clearBillForm() {
        activeReservation = null;
        guestField.clear();
        roomCombo.setValue("Deluxe");
        nightsField.setText("1");
        extrasField.setText("0");
        paidField.setText("0");
        notesArea.clear();
    }

    // =====================================================================
    //  Receipt window
    // =====================================================================

    private void showReceipt(Bill bill) {
        String text = bill.toReceipt(LoginView.getCurrentUser());

        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setFont(Font.font("Monospaced", 14));
        VBox.setVgrow(area, Priority.ALWAYS);

        Stage receiptStage = new Stage();
        receiptStage.initOwner(stage);
        receiptStage.initModality(Modality.WINDOW_MODAL);
        receiptStage.setTitle("Receipt " + bill.getBillId());

        Button printBtn = new Button("Print");
        printBtn.setOnAction(e -> printText(text, receiptStage));

        Button saveBtn = new Button("Save as text");
        saveBtn.setOnAction(e -> saveText(text, bill.getBillId(), receiptStage));

        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> receiptStage.close());

        HBox buttons = new HBox(10, printBtn, saveBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, area, buttons);
        root.setPadding(new Insets(15));

        receiptStage.setScene(new Scene(root, 520, 560));
        receiptStage.showAndWait();
    }

    private void printText(String text, Stage owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            info("Print", "No printer is available on this computer.");
            return;
        }
        if (job.showPrintDialog(owner)) {
            Text node = new Text(text);
            node.setFont(Font.font("Monospaced", 11));
            if (job.printPage(node)) {
                job.endJob();
            } else {
                job.cancelJob();
            }
        }
    }

    private void saveText(String text, String billId, Stage owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save receipt");
        chooser.setInitialFileName("receipt-" + billId + ".txt");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File file = chooser.showSaveDialog(owner);
        if (file != null) {
            try {
                Files.writeString(file.toPath(), text);
            } catch (IOException ex) {
                info("Save failed", "Could not save the file: " + ex.getMessage());
            }
        }
    }

    // =====================================================================
    //  Reservations tab
    // =====================================================================

    private Tab buildReservationTab() {
        resRoomCombo.getItems().addAll("Standard", "Deluxe", "Family");
        resRoomCombo.setValue("Standard");
        resRoomCombo.setMaxWidth(Double.MAX_VALUE);
        resDatePicker.setMaxWidth(Double.MAX_VALUE);
        resGuestField.setPromptText("Guest name");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        addRow(grid, 0, "Guest name:", resGuestField);
        addRow(grid, 1, "Room type:", resRoomCombo);
        addRow(grid, 2, "Check-in date:", resDatePicker);
        addRow(grid, 3, "Nights:", resNightsField);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(130);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        Button reserveBtn = new Button("Make reservation");
        reserveBtn.setStyle("-fx-background-color: #367ce8; -fx-text-fill: white; -fx-font-weight: bold;");
        reserveBtn.setOnAction(e -> makeReservation());

        resStatus.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        resStatus.setWrapText(true);

        VBox form = new VBox(15, sectionTitle("New reservation"), grid, reserveBtn, resStatus);
        form.setMaxWidth(600);

        Button checkInBtn = new Button("Check in (create bill)");
        checkInBtn.setOnAction(e -> checkInSelected());

        Button cancelBtn = new Button("Cancel reservation");
        cancelBtn.setOnAction(e -> cancelSelected());

        setupReservationTable();
        reservationTable.setPrefHeight(260);

        VBox content = new VBox(15, form, sectionTitle("Reservations"),
                new HBox(10, checkInBtn, cancelBtn), reservationTable);
        content.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        return new Tab("Reservations", scroll);
    }

    private void setupReservationTable() {
        TableColumn<Reservation, String> idCol = column("Reservation", "reservationId");
        TableColumn<Reservation, String> guestCol = column("Guest", "guestName");
        TableColumn<Reservation, String> roomCol = column("Room", "roomType");
        TableColumn<Reservation, LocalDate> inCol = column("Check-in", "checkIn");
        TableColumn<Reservation, LocalDate> outCol = column("Check-out", "checkOut");
        TableColumn<Reservation, Integer> nightsCol = column("Nights", "nights");
        TableColumn<Reservation, String> statusCol = column("Status", "status");

        reservationTable.getColumns().addAll(idCol, guestCol, roomCol, inCol, outCol, nightsCol, statusCol);
        reservationTable.setItems(reservations);
        reservationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        reservationTable.setPlaceholder(new Label("No reservations yet."));
    }

    private void makeReservation() {
        String guest = resGuestField.getText().trim();
        LocalDate date = resDatePicker.getValue();

        if (guest.isEmpty()) {
            setStatus(resStatus, "Guest name is required.", false);
            return;
        }
        if (date == null) {
            setStatus(resStatus, "Pick a check-in date.", false);
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            setStatus(resStatus, "Check-in date cannot be in the past.", false);
            return;
        }

        int nights;
        try {
            nights = Integer.parseInt(resNightsField.getText().trim());
        } catch (NumberFormatException ex) {
            setStatus(resStatus, "Nights must be a whole number.", false);
            return;
        }
        if (nights < 1) {
            setStatus(resStatus, "Nights must be at least 1.", false);
            return;
        }

        reservationCounter++;
        Reservation reservation = new Reservation(String.format("R%03d", reservationCounter),
                guest, resRoomCombo.getValue(), date, nights);
        reservations.add(reservation);
        reservationTable.scrollTo(reservation);

        setStatus(resStatus, String.format("Reservation %s accepted for %s (%s to %s).",
                reservation.getReservationId(), guest, date, reservation.getCheckOut()), true);

        resGuestField.clear();
        resRoomCombo.setValue("Standard");
        resDatePicker.setValue(LocalDate.now());
        resNightsField.setText("1");
    }

    // Loads the reservation into the billing form so staff can finish the bill
    private void checkInSelected() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus(resStatus, "Select a reservation in the table first.", false);
            return;
        }
        if (!Reservation.RESERVED.equals(selected.getStatus())) {
            setStatus(resStatus, "Only reservations with status 'Reserved' can be checked in.", false);
            return;
        }

        clearBillForm();
        activeReservation = selected;
        guestField.setText(selected.getGuestName());
        roomCombo.setValue(selected.getRoomType());
        nightsField.setText(String.valueOf(selected.getNights()));
        notesArea.setText("Reservation " + selected.getReservationId());

        tabs.getSelectionModel().select(0);
        setStatus(billStatus, "Reservation " + selected.getReservationId()
                + " loaded. Enter extras and amount paid, then Save bill.", true);
    }

    private void cancelSelected() {
        Reservation selected = reservationTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus(resStatus, "Select a reservation in the table first.", false);
            return;
        }
        if (!Reservation.RESERVED.equals(selected.getStatus())) {
            setStatus(resStatus, "Only reservations with status 'Reserved' can be cancelled.", false);
            return;
        }
        selected.setStatus(Reservation.CANCELLED);
        reservationTable.refresh();
        setStatus(resStatus, "Reservation " + selected.getReservationId() + " cancelled.", true);
    }

    // =====================================================================
    //  Small helpers
    // =====================================================================

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        grid.add(new Label(labelText), 0, row);
        grid.add(field, 1, row);
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        label.setTextFill(Color.web(BLUE));
        return label;
    }

    private Label wrapped(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        return label;
    }

    private void setStatus(Label label, String message, boolean ok) {
        label.setText(message);
        label.setTextFill(ok ? Color.web("#1a7f37") : Color.RED);
    }

    private void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private <S, T> TableColumn<S, T> column(String title, String property) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private TableColumn<Bill, Double> moneyColumn(String title, String property) {
        TableColumn<Bill, Double> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setCellFactory(c -> new TableCell<Bill, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("K %.2f", value));
            }
        });
        return col;
    }

    public static void main(String[] args) {
        launch(args);
    }
}