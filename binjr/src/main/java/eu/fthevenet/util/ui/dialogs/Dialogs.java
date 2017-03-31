package eu.fthevenet.util.ui.dialogs;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ExceptionDialog;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Defines helper methods to facilitate the display of common dialog boxes
 *
 * @author Frederic Thevenet
 */
public class Dialogs {
    private static final Logger logger = LogManager.getLogger(Dialogs.class);

    /**
     * Displays a modal dialog box to shows details about an {@link Exception}.
     *
     * @param e the exception to display
     */
    public static void displayException(Exception e) {
        displayException(e.getLocalizedMessage(), e, null);
    }

    /**
     * Displays a modal dialog box to shows details about an {@link Exception}.
     *
     * @param header the header text for the dialog
     * @param e      the exception to display
     */
    public static void displayException(String header, Exception e) {
        displayException(header, e, null);
    }

    /**
     * Displays a modal dialog box to shows details about an {@link Exception}.
     *
     * @param header the header text for the dialog
     * @param e      the exception to display
     * @param owner  the {@link Node} used to recover the stage the dialog should be linked to
     */
    public static void displayException(String header, Exception e, Node owner) {
        logger.error(header, e);
        ExceptionDialog dlg = new ExceptionDialog(e);
        dlg.initStyle(StageStyle.UTILITY);
        dlg.initOwner(getStage(owner));
        dlg.getDialogPane().setHeaderText(header);
        dlg.showAndWait();
    }

    /**
     * Display an error notification
     *
     * @param title   the title for the notification
     * @param message the title for the notification
     * @param owner   the node to whichposition the notification is attached
     */
    public static void notifyError(String title, String message, Pos position, Node owner) {
        Notifications.create()
                .title(title)
                .text(message)
                .hideAfter(Duration.seconds(3))
                .position(position)
                .owner(owner).showError();
    }

    /**
     * Display an warning notification
     *
     * @param title   the title for the notification
     * @param message the title for the notification
     * @param owner   the node to whichposition the notification is attached
     */
    public static void notifyWarning(String title, String message, Pos position, Node owner) {
        Notifications.create()
                .title(title)
                .text(message)
                .hideAfter(Duration.seconds(3))
                .position(position)
                .owner(owner).showWarning();
    }

    /**
     * Display an info notification
     *
     * @param title   the title for the notification
     * @param message the title for the notification
     * @param owner   the node to whichposition the notification is attached
     */
    public static void notifyInfo(String title, String message, Pos position, Node owner) {
        Notifications.create()
                .title(title)
                .text(message)
                .hideAfter(Duration.seconds(3))
                .position(position)
                .owner(owner).showInformation();
    }

    /**
     * Returns the {@link Stage} instance to which the provided {@link Node} is attached
     *
     * @param node the node to get the stage for.
     * @return the {@link Stage} instance to which the provided {@link Node} is attached
     */
    public static Stage getStage(Node node) {
        if (node != null && node.getScene() != null) {
            return (Stage) node.getScene().getWindow();
        }
        return null;
    }

    /**
     * Launch the system default browser to browse the provided URL
     *
     * @param url a string that represent the url to point the browser at
     * @throws IOException        if the default browser is not found or fails to be launched
     * @throws URISyntaxException if the string could not be transform into a proper URI
     */
    public static void launchUrlInExternalBrowser(String url) throws IOException, URISyntaxException {
        if (!isUnix()) {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                }
                else {
                    logger.warn("Action Desktop.Action.BROWSE is not supported on this platform");
                }
            }
            else {
                logger.warn("java.awt.Desktop is not supported on this platform");
            }
        }
        else {
            // Using Desktop.browse on linux appears to hang the calling thread even though it reports as
            // being supported, so we're forced to revert to some nasty platform specific trickery, so
            // that at least it doesn't hang everything up.
            if (Runtime.getRuntime().exec(new String[]{"which", "xdg-open"}).getInputStream().read() != -1) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
        }
    }

    /**
     * Displays a dialog box that provides the end user with an opportunity to save the current state
     *
     * @param node     the node to get the stage for
     * @param fileName the name of the file to save
     * @return the {@link ButtonType} for the button that was chosen by the user
     */
    public static ButtonType confirmSaveDialog(Node node, String fileName) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.initOwner(Dialogs.getStage(node));
        dlg.setTitle("Save");
        dlg.getDialogPane().setHeaderText("Do you want to save changes to " + fileName + "?");
        ImageView img = new ImageView(new Image(Dialogs.class.getResourceAsStream("/images/save_96.png")));
        img.setFitHeight(32);
        img.setFitWidth(32);
        dlg.getDialogPane().setGraphic(img);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        return dlg.showAndWait().orElse(ButtonType.CANCEL);
    }

    private static boolean isUnix() {
        String osName = System.getProperty("os.name").toLowerCase();
        return (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"));
    }
}
