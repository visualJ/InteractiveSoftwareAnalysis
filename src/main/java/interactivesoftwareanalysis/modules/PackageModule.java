package interactivesoftwareanalysis.modules;

import interactivesoftwareanalysis.model.Attribute;
import interactivesoftwareanalysis.model.Model;
import interactivesoftwareanalysis.model.Resource;
import interactivesoftwareanalysis.model.ResourceNotFoundException;
import interactivesoftwareanalysis.modules.actions.ResourceSelectionModuleAction;
import interactivesoftwareanalysis.modules.data.*;
import interactivesoftwareanalysis.modules.filter.DecideFilterBase;
import interactivesoftwareanalysis.modules.filter.DecideFilterFactoryBase;
import interactivesoftwareanalysis.modules.filter.Filter;
import interactivesoftwareanalysis.modules.parameters.FileParameter;
import interactivesoftwareanalysis.modules.parameters.StringParameter;
import interactivesoftwareanalysis.modules.parameters.VisitParameter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A builtin module that deals with packages.
 * <p>
 * <p>
 * Interactive submodules:
 * <ul>
 * <li>{@link PackagesSubmodule}</li>
 * </ul>
 * </p>
 * <p>
 * <p>
 * Importsubmodules:
 * <ul>
 * <li>{@link DPKGPackagesImportModule}</li>
 * <li>{@link DPKGPackageFilesImportModule}</li>
 * </ul>
 * </p>
 * <p>
 * <p>
 * Exportsubmodules:
 * <ul>
 * <li>{@link DPKGPackageSelectionExportModule}</li>
 * </ul>
 * </p>
 * <p>
 * <p>
 * Filters:
 * <ul>
 * <li>'dpkg': {@link PackageFileFilter}</li>
 * </ul>
 * </p>
 */
public class PackageModule extends ModuleBase {

    public static final String NAMESPACE = "http://interactivesoftwareanalysis/";

    public PackageModule(ModuleContext moduleContext) {
        super(moduleContext, "Pakete", "Dieses Modul stellt Informationen zu Softwarepaketen bereit.");
        getInteractiveSubmodules().add(new PackagesSubmodule(moduleContext));
        getImportSubmodules().addAll(Arrays.asList(
                new DPKGPackagesImportModule(moduleContext),
                new DPKGPackageFilesImportModule(moduleContext))
        );
        getExportSubmodules().add(new DPKGPackageSelectionExportModule(moduleContext));
        getFilterFactories().add(new DecideFilterFactoryBase("dpkg", "Filtert nach Dateien, die zu einem Paket gehören, oder Paketen, die eine Datei beinhalten.", moduleContext) {
            @Override
            public Filter newInstance(String pattern) {
                return new PackageFileFilter(pattern);
            }
        });
    }

    /**
     * Retrieve packages as list or table.
     */
    private class PackagesSubmodule extends InteractiveSubmoduleBase {

        public PackagesSubmodule(ModuleContext moduleContext) {
            super("Pakete", "Zeigt alle im Modell enthaltenen Pakete an", Arrays.asList(DataList.class, DataTable.class), moduleContext);
            getModuleActions().add(new TagPackageFilesModuleAction(moduleContext));
        }

        @Override
        public <T> T getData(Class<T> dataType) throws DataTypeNotSupportedException {
            if (dataType == DataList.class) {
                String query = "SELECT DISTINCT ?uri ?name ?version WHERE { ?uri rdf:type isa:package. ?uri isa:humanReadableName ?name. ?uri isa:packageVersion ?version}";
                List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
                List<DataItem> packages = new ArrayList<>();
                results.forEach(solution -> {
                    try {
                        String name = MessageFormat.format("{0} ({1})", solution.get("name"), solution.get("version"));
                        packages.add(new DataItem(name, moduleContext.getModel().getResource(solution.get("uri"))));
                    } catch (ResourceNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                packages.sort((o1, o2) -> o1.getString().compareToIgnoreCase(o2.getString()));
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                        T data = (T) new DataList(packages);
                return data;
            } else if (dataType == DataTable.class) {
                String query = "SELECT DISTINCT ?uri ?name ?version ?section ?essential ?priority ?dependencies " +
                        "WHERE { " +
                        "?uri rdf:type isa:package. " +
                        "?uri isa:humanReadableName ?name. " +
                        "?uri isa:packageVersion ?version." +
                        "?uri isa:packageSection ?section." +
                        "?uri isa:packageEssential ?essential." +
                        "?uri isa:packagePriority ?priority." +
                        "?uri isa:packageDependencies ?dependencies." +
                        "}";
                List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
                List<DataItem> packages = new ArrayList<>();
                results.forEach(solution -> {
                    try {
                        String name = solution.get("name");
                        String version = solution.get("version");
                        String section = solution.get("section");
                        String essential = solution.get("essential");
                        String priority = solution.get("priority");
                        String dependencies = solution.get("dependencies");
                        DataItem dataItem = new DataItem(name, moduleContext.getModel().getResource(solution.get("uri")));
                        dataItem.getStrings().put("Name", name);
                        dataItem.getStrings().put("Version", version);
                        dataItem.getStrings().put("Kategorie", section);
                        dataItem.getStrings().put("Wichtig", essential);
                        dataItem.getStrings().put("Priorität", priority);
                        dataItem.getStrings().put("Abhängigkeiten", dependencies);
                        packages.add(dataItem);
                    } catch (ResourceNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                packages.sort((o1, o2) -> o1.getString().compareToIgnoreCase(o2.getString()));
                @SuppressWarnings("unchecked") // The if already checks for the correct type
                        T data = (T) new DataTable(packages, Arrays.asList("Name", "Version", "Kategorie", "Wichtig", "Priorität", "Abhängigkeiten"));
                return data;
            } else {
                throw new DataTypeNotSupportedException(this.getClass().toString() + " does not support datatype " + dataType.toString());
            }
        }

        private class TagPackageFilesModuleAction extends ResourceSelectionModuleAction {
            public TagPackageFilesModuleAction(ModuleContext moduleContext) {
                super("Zugehörige Dateien mit Tag versehen", "Versieht zu Paketen gehörende Dateien mit einem Tag mit dem Paketnamen", moduleContext);
            }

            @Override
            public void execute(List<DataItem> input) {
                Model model = moduleContext.getModel();
                model.setBatchMode(true);
                for (DataItem dataItem : input) {
                    Resource resource = dataItem.getResource();
                    String packageName = resource.getAttributes(NAMESPACE + "packageName").get(0).getValue();
                    List<String> fileUris = resource.getAttributes(NAMESPACE + "packageFile").stream().map(Attribute::getValue).collect(Collectors.toList());
                    fileUris.forEach(uri -> {
                        try {
                            Resource file = model.getResource(uri);
                            file.addTag(packageName, MessageFormat.format("Gehört zum Paket {0} (Automatisch getaggt duch das Paketmodul)", packageName));
                        } catch (ResourceNotFoundException e) {
                            // a file that does not exist can not be tagged. Just ignored.
                        }
                    });
                }
                model.setBatchMode(false);
            }
        }
    }

    /**
     * Imports packages using the debian package manager
     */
    private class DPKGPackagesImportModule extends ImportSubmoduleBase {

        public DPKGPackagesImportModule(ModuleContext moduleContext) {
            super("Pakete (DPKG)", "Importiert Informationen zu installierten Paketen über DPKG", moduleContext);
        }

        @Override
        public boolean doImport(Progress progress) {
            Model model = moduleContext.getModel();
            DataSource dataSource = moduleContext.getDataSourceManager().getDataSource();
            model.setBatchMode(true);

            progress.setMessage("Lade Paketinformationen von der Datenquelle");

            try {
                String[] lines = dataSource.execute("dpkg-query -W -f='${Package}\\t${Version}\\t${Section}\\t${Essential}\\t${Priority}\\t${Depends}\\n'").split("\n");

                progress.setMessage("Importiere Paketinformationen in das interne Modell");

                int totalLines = lines.length;
                int currentLines = 0;
                for (String line : lines) {
                    String[] elements = line.split("\t");
                    String name = elements.length >= 1 ? elements[0] : "";
                    String version = elements.length >= 2 ? elements[1] : "";
                    String section = elements.length >= 3 ? elements[2] : "";
                    String essential = elements.length >= 4 ? elements[3] : "";
                    String priority = elements.length >= 5 ? elements[4] : "";
                    String dependencies = elements.length >= 6 ? elements[5] : "";
                    Resource resource = model.createResource(NAMESPACE + "package" + name.hashCode(), NAMESPACE + "package", name);
                    resource.addAttribute(NAMESPACE + "packageName", name);
                    resource.addAttribute(NAMESPACE + "packageVersion", version);
                    resource.addAttribute(NAMESPACE + "packageSection", section);
                    resource.addAttribute(NAMESPACE + "packageEssential", essential);
                    resource.addAttribute(NAMESPACE + "packagePriority", priority);
                    resource.addAttribute(NAMESPACE + "packageDependencies", dependencies);
                    progress.setProgress(++currentLines / (double) totalLines);
                }
            } catch (DataSourceExecuteException e) {
                e.printStackTrace();
                return false;
            } finally {
                model.setBatchMode(false);
            }

            return true;
        }
    }

    /**
     * Imports information about files that belong to packages using the debian package manager
     */
    private class DPKGPackageFilesImportModule extends ImportSubmoduleBase {

        public DPKGPackageFilesImportModule(ModuleContext moduleContext) {
            super("Paket-Dateien (DPKG)", "Importiert Informationen zu Dateien installierter Pakete über DPKG", moduleContext);
        }

        @Override
        public boolean doImport(Progress progress) {
            Model model = moduleContext.getModel();
            DataSource dataSource = moduleContext.getDataSourceManager().getDataSource();
            model.setBatchMode(true);

            progress.setMessage("Lade Informationen über zu Paketen gehörende Dateien von der Datenquelle");

            try {
                String[] lines = dataSource.execute("dpkg --get-selections | grep install | awk '{print($1)}' | " +
                        "sed 's/:.*$//' | while read i; do dpkg -L $i  | " +
                        "while read j; do printf '%s\\t%s\\n' \"$i\" \"$j\";  done; done").split("\n");

                progress.setMessage("Importiere Informationen über zu Paketen gehörende Dateien in das interne Modell");

                int totalLines = lines.length;
                int currentLines = 0;
                for (String line : lines) {
                    String[] elements = line.split("\t");
                    if (elements.length >= 2) {
                        String packageName = elements[0];
                        String filePath = elements[1];
                        try {
                            Resource packageResource = model.getResource(NAMESPACE + "package" + packageName.hashCode());
                            Resource fileResource = model.getResource(NAMESPACE + "filesystemitem" + filePath.hashCode());
                            packageResource.addResourceAttribute(NAMESPACE + "packageFile", fileResource.getUri());
                        } catch (ResourceNotFoundException e) {
                            // it is okay, when a resource is not found. just ignore it then.
                        }
                    }
                    progress.setProgress(++currentLines / (double) totalLines);
                }
            } catch (DataSourceExecuteException e) {
                e.printStackTrace();
                return false;
            } finally {
                model.setBatchMode(false);
            }
            return true;
        }
    }

    private class DPKGPackageSelectionExportModule extends ExportSubmoduleBase {

        @VisitParameter private FileParameter exportFile = new FileParameter("Datei", "In diese Datei wird die Paketliste exportiert.");
        @VisitParameter private StringParameter tag = new StringParameter("Nach diesen Tags filtern",
                "Wenn angegeben, werden nur mit mindestens einem dieser Tags markierte Pakete berücksichtigt.", () -> "", this::getTagNames);


        public DPKGPackageSelectionExportModule(ModuleContext moduleContext) {
            super("DPKG Paketselektionsliste", "Exportiert Pakete als Liste, die mit\n'dpkg --set-selections'\nverwendet werden kann.", moduleContext);
            exportFile.setSave(true);
            exportFile.setFileExtensions(Collections.singletonList("*.*"));
            tag.setList(true);
            tag.setSeparator(",");
        }

        @Override
        public void export() {
            File file = exportFile.getFile();
            if (file != null) {

                // an optional tag filter query, if tags were given by the user
                String filterQuery = "";
                if (tag.getValues().length >= 1 && !tag.getValues()[0].isEmpty()) {
                    filterQuery = MessageFormat.format("?uri isa:tag [isa:tagName ?tagName]. FILTER ({0}) ",
                            Arrays.stream(tag.getValues())
                                    .map(s -> MessageFormat.format("?tagName = \"{0}\"", s))
                                    .collect(Collectors.joining(" || ")));
                }

                // get all packages and sort alphabetically
                String query = MessageFormat.format("SELECT DISTINCT ?name WHERE '{' ?uri rdf:type isa:package. ?uri isa:packageName ?name. {0}'}'", filterQuery);
                List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
                results.sort((o1, o2) -> o1.get("name").compareToIgnoreCase(o2.get("name")));

                // create the file, if it does not already exist
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                // write the package list for dpkg
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()))) {
                    writer.write(MessageFormat.format("# Pakete: {0}", results.size()));
                    writer.newLine();
                    for (Map<String, String> result : results) {
                        writer.write(MessageFormat.format("{0} install", result.get("name")));
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        /**
         * Retrieve tags for the parameter selection
         * @return a list of tag names
         */
        private List<String> getTagNames() {
            String query = "SELECT DISTINCT ?tagName WHERE { ?thing isa:tag [isa:tagName ?tagName; isa:tagDetail ?tagDetail]}";
            List<Map<String, String>> results = moduleContext.getModel().executeSelectQuery(query);
            List<String> tags = new ArrayList<>();
            results.forEach(solution -> tags.add(solution.get("tagName")));
            return tags;
        }
    }

    /**
     * Filters files that belong to a package, or packages that belong to a file.
     */
    private class PackageFileFilter extends DecideFilterBase {

        public PackageFileFilter(String pattern) {
            super(pattern);
        }

        @Override
        public boolean filter(DataItem dataItem) {
            Resource resource = dataItem.getResource();
            if (resource == null) {
                return false;
            }
            Model model = moduleContext.getModel();
            String query = MessageFormat.format("ASK '{' '{'?package isa:packageName \"{0}\". ?package isa:packageFile <{1}>.'}'" +
                    " UNION '{'<{1}> isa:packageFile ?file. ?file isa:path \"{0}\".'}' '}'", pattern, resource.getUri());
            return model.executeAskQuery(query);
        }
    }

}
