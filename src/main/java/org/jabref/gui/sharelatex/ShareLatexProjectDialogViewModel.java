package org.jabref.gui.sharelatex;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import org.jabref.gui.AbstractViewModel;
import org.jabref.gui.StateManager;
import org.jabref.logic.importer.ImportFormatPreferences;
import org.jabref.logic.importer.ParserResult;
import org.jabref.logic.importer.fileformat.BibtexImporter;
import org.jabref.logic.sharelatex.ShareLatexManager;
import org.jabref.logic.sharelatex.ShareLatexParser;
import org.jabref.logic.sharelatex.events.ShareLatexEntryMessageEvent;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.sharelatex.ShareLatexProject;
import org.jabref.model.util.FileUpdateMonitor;

import com.google.common.eventbus.Subscribe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShareLatexProjectDialogViewModel extends AbstractViewModel {

    private static final Log LOGGER = LogFactory.getLog(ShareLatexProjectDialogViewModel.class);

    private final StateManager stateManager;
    private final ShareLatexManager manager;
    private final SimpleListProperty<ShareLatexProjectViewModel> projects = new SimpleListProperty<>(
            FXCollections.observableArrayList());
    private final ImportFormatPreferences prefs;

    private final FileUpdateMonitor fileMonitor;

    public ShareLatexProjectDialogViewModel(StateManager stateManager, ShareLatexManager manager, ImportFormatPreferences prefs, FileUpdateMonitor fileMonitor) {
        this.stateManager = stateManager;
        this.prefs = prefs;
        this.fileMonitor = fileMonitor;
        manager.registerListener(this);
        this.manager = manager;

    }

    public void addProjects(List<ShareLatexProject> projectsToAdd) {
        this.projects.clear();
        this.projects.addAll(projectsToAdd.stream().map(ShareLatexProjectViewModel::new).collect(Collectors.toList()));
    }

    public SimpleListProperty<ShareLatexProjectViewModel> projectsProperty() {
        return this.projects;
    }

    @Subscribe
    public void listenToSharelatexEntryMessage(ShareLatexEntryMessageEvent event) {

        Path actualDbPath = stateManager.getActiveDatabase().get().getDatabasePath().get();

        try {
            ParserResult result = new BibtexImporter(prefs, fileMonitor).importDatabase(event.getNewDatabaseContent());

            ShareLatexParser parser = new ShareLatexParser();
            Optional<BibEntry> entry = parser.getEntryFromPosition(result, 633);
            System.out.println(entry); //Emtpy => Add 

        } catch (IOException e1) {
            // TODO Auto-generated catch block

        }

        try (BufferedWriter writer = Files.newBufferedWriter(actualDbPath, StandardCharsets.UTF_8)) {
            writer.write(event.getNewDatabaseContent());
            writer.close();

        } catch (IOException e) {
            LOGGER.error("Problem writing new database content", e);
        }

    }

}
