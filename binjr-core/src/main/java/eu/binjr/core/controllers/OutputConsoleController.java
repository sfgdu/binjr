/*
 *    Copyright 2017-2019 Frederic Thevenet
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.binjr.core.controllers;

import eu.binjr.common.diagnostic.DiagnosticCommand;
import eu.binjr.common.diagnostic.DiagnosticException;
import eu.binjr.common.function.CheckedLambdas;
import eu.binjr.core.Binjr;
import eu.binjr.core.dialogs.Dialogs;
import eu.binjr.core.preferences.AppEnvironment;
import eu.binjr.core.preferences.GlobalPreferences;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static eu.binjr.core.Binjr.DEBUG_CONSOLE_APPENDER;

/**
 * The controller for the output console window.
 *
 * @author Frederic Thevenet
 */
public class OutputConsoleController implements Initializable {
    private static final Logger logger = LogManager.getLogger(OutputConsoleController.class);
    public TextField consoleMaxLinesText;
    public VBox root;
    @FXML
    private TextFlow textOutput;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ChoiceBox<Level> logLevelChoice;
    @FXML
    private ToggleButton alwaysOnTopToggle;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textOutput.getChildren().addListener(
                (ListChangeListener<Node>) ((change) -> {
                    textOutput.layout();
                    scrollPane.layout();
                    scrollPane.setVvalue(1.0f);
                }));

        final TextFormatter<Number> formatter = new TextFormatter<>(new NumberStringConverter(Locale.getDefault(Locale.Category.FORMAT)));
        consoleMaxLinesText.setTextFormatter(formatter);
        formatter.valueProperty().bindBidirectional(GlobalPreferences.getInstance().consoleMaxLineCapacityProperty());

        if (DEBUG_CONSOLE_APPENDER == null){
            Text log = new Text("<ERROR: The debug console appender is unavailable!>\n");
            log.getStyleClass().add("log-error");
            textOutput.getChildren().add(log);
        }else {
            DEBUG_CONSOLE_APPENDER.setTextFlow(textOutput);
        }
        Platform.runLater(() -> {
            logLevelChoice.getItems().setAll(Level.values());
            logLevelChoice.getSelectionModel().select(AppEnvironment.getInstance().getLogLevel());
            AppEnvironment.getInstance().logLevelProperty().addListener((observable, oldValue, newValue) -> {
                logLevelChoice.getSelectionModel().select(newValue);
            });
            logLevelChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                AppEnvironment.getInstance().setLogLevel(newValue);
            });
        });
    }

    /**
     * Returns the alwaysOnTop toggle button.
     *
     * @return the alwaysOnTop toggle button.
     */
    public ToggleButton getAlwaysOnTopToggle() {
        return alwaysOnTopToggle;
    }



    @FXML
    private void handleClearConsole(ActionEvent actionEvent) {
        if (DEBUG_CONSOLE_APPENDER != null) {
            DEBUG_CONSOLE_APPENDER.clearBuffer();
        }
    }

    @FXML
    private void handleSaveConsoleOutput(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save console ouptut");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file", "*.txt"));
            fileChooser.setInitialDirectory(GlobalPreferences.getInstance().getMostRecentSaveFolder().toFile());
            fileChooser.setInitialFileName("binjr_console_output.txt");
            File selectedFile = fileChooser.showSaveDialog(Dialogs.getStage(scrollPane));
            if (selectedFile != null) {
                try (Writer writer = new BufferedWriter(new FileWriter(selectedFile))) {
                    textOutput.getChildren().stream().map(node -> ((Text) node).getText()).forEach(CheckedLambdas.<String, IOException>wrap(writer::write));
                } catch (IOException e) {
                    Dialogs.notifyException("Error writing log message to file", e, scrollPane);
                }
                GlobalPreferences.getInstance().setMostRecentSaveFolder(selectedFile.toPath());

            }
        } catch (Exception e) {
            Dialogs.notifyException("Failed to save console output to file", e, scrollPane);
        }
    }

    @FXML
    private void handleCopyConsoleOutput(ActionEvent actionEvent) {
        try {
            final ClipboardContent content = new ClipboardContent();
            content.putString(textOutput.getChildren().stream().map(node -> ((Text) node).getText()).collect(Collectors.joining()));
            Clipboard.getSystemClipboard().setContent(content);
        } catch (Exception e) {
            Dialogs.notifyException("Failed to copy console output to clipboard", e, scrollPane);
        }
    }

    public void handleDebugForceGC(ActionEvent actionEvent) {
        Binjr.runtimeDebuggingFeatures.debug(() -> "Force GC");
        System.gc();
        Binjr.runtimeDebuggingFeatures.debug(this::getJvmHeapStats);

    }

    public void handleDebugRunFinalization(ActionEvent actionEvent) {
        Binjr.runtimeDebuggingFeatures.debug(() -> "Force runFinalization");
        System.runFinalization();
    }

    public void handleDebugDumpHeapStats(ActionEvent actionEvent) {
        Binjr.runtimeDebuggingFeatures.debug(this::getJvmHeapStats);
    }

    public void handleDebugDumpThreadsStacks(ActionEvent actionEvent) {
        try {
            Binjr.runtimeDebuggingFeatures.debug(DiagnosticCommand.dumpThreadStacks());
        } catch (DiagnosticException e) {
            Dialogs.notifyException("Error running diagnostic command", e, root);
        }
    }

    public void handleDebugDumpVmSystemProperties(ActionEvent actionEvent) {
        try {
            Binjr.runtimeDebuggingFeatures.debug(DiagnosticCommand.dumpVmSystemProperties());
        } catch (DiagnosticException e) {
            Dialogs.notifyException("Error running diagnostic command", e, root);
        }
    }

    public void handleDebugDumpClassHistogram(ActionEvent actionEvent) {
        try {
            Binjr.runtimeDebuggingFeatures.debug(DiagnosticCommand.dumpClassHistogram());
        } catch (DiagnosticException e) {
            Dialogs.notifyException("Error running diagnostic command", e, root);
        }
    }

    private String getJvmHeapStats() {
        Runtime rt = Runtime.getRuntime();
        double maxMB = rt.maxMemory() / 1024.0 / 1024.0;
        double committedMB = (double) rt.totalMemory() / 1024.0 / 1024.0;
        double usedMB = ((double) rt.totalMemory() - rt.freeMemory()) / 1024.0 / 1024.0;
        double percentCommitted = (((double) rt.totalMemory() - rt.freeMemory()) / rt.totalMemory()) * 100;
        double percentMax = (((double) rt.totalMemory() - rt.freeMemory()) / rt.maxMemory()) * 100;
        return String.format(
                "JVM Heap: Max=%.0fMB, Committed=%.0fMB, Used=%.0fMB (%.2f%% of committed, %.2f%% of max)",
                maxMB,
                committedMB,
                usedMB,
                percentCommitted,
                percentMax
        );
    }

    public void handleDebugDumpVmFlags(ActionEvent actionEvent) {
        try {
            Binjr.runtimeDebuggingFeatures.debug(DiagnosticCommand.dumpVmFlags());
        } catch (DiagnosticException e) {
            Dialogs.notifyException("Error running diagnostic command", e, root);
        }
    }

    public void handleDebugDumpVmCommandLine(ActionEvent actionEvent) {
        try {
            Binjr.runtimeDebuggingFeatures.debug(DiagnosticCommand.dumpVmCommandLine());
        } catch (DiagnosticException e) {
            Dialogs.notifyException("Error running diagnostic command", e, root);
        }
    }

}
