package org.jabref.gui.maintable;

import org.jabref.gui.DialogService;
import org.jabref.gui.Globals;
import org.jabref.gui.StateManager;
import org.jabref.gui.actions.ActionHelper;
import org.jabref.gui.actions.SimpleCommand;
import org.jabref.gui.externalfiletype.ExternalFileTypes;
import org.jabref.gui.fieldeditors.LinkedFileViewModel;
import org.jabref.logic.l10n.Localization;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.LinkedFile;
import org.jabref.preferences.PreferencesService;

import java.util.List;

public class OpenMultipleExternalFilesAction extends SimpleCommand {

    private final static int MAX_FILES = 10;

    private final DialogService dialogService;
    private final StateManager stateManager;
    private final PreferencesService preferencesService;

    public OpenMultipleExternalFilesAction(DialogService dialogService, StateManager stateManager, PreferencesService preferencesService) {
        this.dialogService = dialogService;
        this.stateManager = stateManager;
        this.preferencesService = preferencesService;

        this.executable.bind(ActionHelper.isFilePresentForSelectedEntry(stateManager, preferencesService)
                .and(ActionHelper.lessOrEqualFilesOfEntriesSelected(MAX_FILES, stateManager)));
    }

    @Override
    public void execute() {
        stateManager.getActiveDatabase().ifPresent(databaseContext -> {
            final List<BibEntry> selectedEntries = stateManager.getSelectedEntries();

            // check again before executing
            if (selectedEntries.stream()
                    .map(BibEntry::getFiles)
                    .mapToInt(List::size)
                    .sum() > MAX_FILES) {
                dialogService.notify(Localization.lang("Trying to open too many files. Make sure less than " + MAX_FILES + " are selected"));
                return;
            }

            for (BibEntry selectedEntry : selectedEntries) {

                for (LinkedFile file : selectedEntry.getFiles()) {

                    LinkedFileViewModel linkedFileViewModel = new LinkedFileViewModel(
                            file,
                            selectedEntry,
                            databaseContext,
                            Globals.TASK_EXECUTOR,
                            dialogService,
                            preferencesService.getXmpPreferences(),
                            preferencesService.getFilePreferences(),
                            ExternalFileTypes.getInstance());
                    linkedFileViewModel.open();
                }
            }
        });
    }
}
