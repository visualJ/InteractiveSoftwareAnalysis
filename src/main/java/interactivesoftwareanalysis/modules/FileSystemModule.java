package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.model.Model;
import interactivesoftwareanalysis.model.Resource;
import interactivesoftwareanalysis.model.ResourceNotFoundException;
import interactivesoftwareanalysis.modules.data.*;
import javafx.scene.control.TreeItem;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * A builtin module that deals with the file system (files, directories).
 *
 * <p>
 *     Interactive submodules:
 *     <ul>
 *         <li>{@link FilesAndDirectoriesSubmodule}</li>
 *         <li>{@link SymlinksSubmodule}</li>
 *     </ul>
 * </p>
 *
 * <p>
 *     Importsubmodules:
 *     <ul>
 *         <li>{@link FileSystemImportModule}</li>
 *     </ul>
 * </p>
 */
public class FileSystemModule extends ModuleBase {

    private final static String NAME_SPACE = "http://interactivesoftwareanalysis/";

    public FileSystemModule(ModuleContext moduleContext) {
        super(moduleContext, "Dateisystem", "Dieses Modul stellt Informationen zu Dateien und Verzeichnissen bereit");
        getInteractiveSubmodules().addAll(Arrays.asList(
                new FilesAndDirectoriesSubmodule(moduleContext),
                new SymlinksSubmodule(moduleContext)));
        getImportSubmodules().addAll(Collections.singletonList(new FileSystemImportModule(moduleContext)));
    }

    /**
     * Appends a path to a tree by creating new nodes or appeding to existing ones.
     * The paths have to be sorted lexicographically, otherwise folder items will get the wrong resource.
     * @param inputItem the tree root
     * @param path the path to append (split into path segments at the path separator)
     * @param level the recursion level. Use 0.
     */
    private void appendPath(TreeItem<DataItem> inputItem, String[] path, Resource resource, int level){
        if (level >= path.length || inputItem == null){
            return;
        }

        // choose the right path segment for the current recursion level
        String levelPath = path[level].isEmpty() ? "/" : path[level];

        // get an existing tree node or create a new one
        Optional<TreeItem<DataItem>> result = inputItem.getChildren().stream()
                .filter(node -> node.getValue().getString().equals(levelPath))
                .findAny();
        TreeItem<DataItem> newItem;
        if (result.isPresent()) {
            newItem = result.get();
        }
        else {
            newItem = new TreeItem<>(new DataItem(levelPath, level == path.length - 1 ? resource : null));
            inputItem.getChildren().add(newItem);
        }

        // repeat with the next path segment
        appendPath(newItem, path, resource, level + 1);
    }

    /**
     * Retrieves files and directories as list or tree
     */
    private class FilesAndDirectoriesSubmodule extends InteractiveSubmoduleBase {

        public FilesAndDirectoriesSubmodule(ModuleContext moduleContext) {
            super("Dateien und Verzeichnisse", "Zeigt Dateien und Verzeichnisse an", Arrays.asList(DataList.class, DataTree.class), moduleContext);
        }

        @Override public <T> T getData(Class<T> dataType) throws DataTypeNotSupportedException {
            String filesQuery = "SELECT DISTINCT ?file ?path  WHERE {?file isa:path ?path. {{?file rdf:type isa:file.} UNION {?file rdf:type isa:directory.}}}";
            List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(filesQuery);
            List<DataItem> files = new ArrayList<>();
            results.forEach(result -> {
                String name = result.get("path");
                String uri = result.get("file");
                Resource resource = null;
                try {
                    resource = moduleContext.getModel().getResource(uri);
                    files.add(new DataItem(name, resource));
                } catch (ResourceNotFoundException e) {
                    e.printStackTrace();
                }
            });
            files.sort((o1, o2) -> o1.getString().compareToIgnoreCase(o2.getString()));
            if (dataType == DataList.class) {
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                        T data = (T) new DataList(files);
                return data;
            } else if (dataType == DataTree.class) {
                DataItem rootItem = new DataItem("", null);
                TreeItem<DataItem> root = new TreeItem<>(rootItem);
                files.forEach(path -> appendPath(root, path.getString().split("/"), path.getResource(), 0));
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                T data = (T) new DataTree(root.getChildren().size()==1?root.getChildren().get(0):root);
                return data;
            } else {
                throw new DataTypeNotSupportedException(this.getClass().toString() + " does not support datatype " + dataType.toString());
            }
        }

    }

    /**
     * Retrieves symlinks as list or tree
     */
    private class SymlinksSubmodule extends InteractiveSubmoduleBase {

        public SymlinksSubmodule(ModuleContext moduleContext) {
            super("Symlinks", "Zeigt Symlinks an", Arrays.asList(DataList.class, DataTree.class), moduleContext);
        }

        @Override public <T> T getData(Class<T> dataType) throws DataTypeNotSupportedException {
            String filesQuery = "SELECT ?link ?path WHERE {?link rdf:type isa:symlink. ?link isa:path ?path.}";
            List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(filesQuery);
            List<DataItem> symLinks = new ArrayList<>();
            results.forEach(solution -> {
                String name = solution.get("path");
                String uri = solution.get("link");
                Resource resource = null;
                try {
                    resource = moduleContext.getModel().getResource(uri);
                    symLinks.add(new DataItem(name, resource));
                } catch (ResourceNotFoundException e) {
                    e.printStackTrace();
                }
            });
            symLinks.sort((o1, o2) -> o1.getString().compareToIgnoreCase(o2.getString()));
            if (dataType == DataList.class) {
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                        T data = (T) new DataList(symLinks);
                return data;
            } else if (dataType == DataTree.class) {
                DataItem rootItem = new DataItem("", null);
                TreeItem<DataItem> root = new TreeItem<>(rootItem);
                symLinks.forEach(path -> appendPath(root, path.getString().split("/"), path.getResource(), 0));
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                        T data = (T) new DataTree(root.getChildren().size()==1?root.getChildren().get(0):root);
                return data;
            } else {
                throw new DataTypeNotSupportedException(this.getClass().toString() + " does not support datatype " + dataType.toString());
            }
        }
    }

    /**
     * Import files, directories and symlinks
     */
    private class FileSystemImportModule extends ImportSubmoduleBase {

        public FileSystemImportModule(ModuleContext moduleContext) {
            super("Dateien und Verzeichnisse", "Importiert Informationen zu Dateien und Verzeichnissen auf dem System", moduleContext);
        }

        @Override public boolean doImport(Progress progress) {
            Model model = moduleContext.getModel();
            DataSource dataSource = moduleContext.getDataSourceManager().getDataSource();

            CountDownLatch latch = new CountDownLatch(3);
            AtomicReference<String[]> filesRef = new AtomicReference<>();
            AtomicReference<String[]> pathsRef = new AtomicReference<>();
            AtomicReference<String[]> symlinksRef = new AtomicReference<>();

            progress.setMessage("Lade Datei- und Verzeichnisinformationen von der Datenquelle");

            // execute the commands simultaneously and split the output
            // when each command is done, the latch is counted down
            executeCommandInNewThread("cd /\nfind -xtype f 2>/dev/null", filesRef, dataSource, latch);
            executeCommandInNewThread("cd /\nfind -xtype d 2>/dev/null", pathsRef, dataSource, latch);
            executeCommandInNewThread("cd /\nfind -type l 2>/dev/null", symlinksRef, dataSource, latch);

            // wait, until all threads are finished, so the importing can be done sequentially
            // and the user can be presented with a sensible progress indication. The model
            // would block simultaneous write accesses anyway.
            while (latch.getCount() > 0) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            progress.setMessage("Importiere Datei- und Verzeichnisinformationen in das interne Modell");

            // some preparations for tracking the progress
            AtomicInteger current = new AtomicInteger();
            double total = filesRef.get().length + pathsRef.get().length + symlinksRef.get().length;

            // add files, directories and paths to the model. addPathResource reduces duplicate code.
            model.setBatchMode(true);
            addPathResource(s -> NAME_SPACE + "filesystemitem" + s.hashCode(), NAME_SPACE + "file", filesRef.get(), progress, model, total, current);
            addPathResource(s -> NAME_SPACE + "filesystemitem" + s.hashCode(), NAME_SPACE + "directory", pathsRef.get(), progress, model, total, current);
            addPathResource(s -> NAME_SPACE + "filesystemitem" + s.hashCode(), NAME_SPACE + "symlink", symlinksRef.get(), progress, model, total, current);
            model.setBatchMode(false);
            return true;
        }

        /**
         * Execute a command on the data source in a new thread and split the result in lines.
         * @param command the command to execute
         * @param result areference to an array to store the result in
         * @param dataSource the data source to execute the command on
         * @param latch a latch to count down, when command is finished. Used when running multiple
         *              commands in parallel  to wait for all to finish
         */
        private void executeCommandInNewThread(String command, AtomicReference<String[]> result, DataSource dataSource, CountDownLatch latch) {
            new Thread(() -> {
                try {
                    result.set(dataSource.execute(command).split("\n"));
                } catch (DataSourceExecuteException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        /**
         * Add a path resource to the model.
         * @param uri function for creating uris for the resources
         * @param typeUri the new resoruces types uri
         * @param lines the pathes to add
         * @param progress a progress object to set the importing progress on
         * @param model the model to add the resources to
         * @param totalLines the total number of lines, for the progress calculation
         * @param current the current line, for the progress calculation
         */
        private void addPathResource(Function<String, String> uri, String typeUri, String[] lines, Progress progress, Model model, double totalLines, AtomicInteger current) {
            Arrays.stream(lines).filter(s -> !s.isEmpty()).forEach(s -> {
                String p = s.substring(1);
                p = p.isEmpty() ? "/" : p;
                Resource directory = model.createResource(uri.apply(p), typeUri, p);
                directory.addAttribute(NAME_SPACE + "path", p);
                progress.setProgress(current.incrementAndGet() / totalLines);
            });
        }
    }
}
