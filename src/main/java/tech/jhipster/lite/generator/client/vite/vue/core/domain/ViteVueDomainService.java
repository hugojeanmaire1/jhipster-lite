package tech.jhipster.lite.generator.client.vite.vue.core.domain;

import static tech.jhipster.lite.common.domain.FileUtils.getPath;
import static tech.jhipster.lite.generator.project.domain.Constants.PACKAGE_JSON;
import static tech.jhipster.lite.generator.project.domain.DefaultConfig.BASE_NAME;

import java.util.List;
import java.util.Map;
import tech.jhipster.lite.error.domain.GeneratorException;
import tech.jhipster.lite.generator.packagemanager.npm.domain.NpmService;
import tech.jhipster.lite.generator.project.domain.Project;
import tech.jhipster.lite.generator.project.domain.ProjectRepository;

public class ViteVueDomainService implements ViteVueService {

  public static final String SOURCE = "client/vite/vue";
  public static final String SOURCE_PRIMARY = getPath(SOURCE, "webapp/app/common/primary/app");
  public static final String DESTINATION_PRIMARY = "src/main/webapp/app/common/primary/app";

  private final ProjectRepository projectRepository;
  private final NpmService npmService;

  public ViteVueDomainService(ProjectRepository projectRepository, NpmService npmService) {
    this.projectRepository = projectRepository;
    this.npmService = npmService;
  }

  @Override
  public void addViteVue(Project project) {
    addCommonViteVue(project);
    addAppFilesWithoutCss(project);
  }

  @Override
  public void addStyledViteVue(Project project) {
    addCommonViteVue(project);
    addAppFilesWithCss(project);
  }

  private void addCommonViteVue(Project project) {
    addDependencies(project);
    addDevDependencies(project);
    addScripts(project);
    addJestSonar(project);
    addViteConfigFiles(project);
    addRootFiles(project);
    addAppFiles(project);
  }

  public void addDependencies(Project project) {
    ViteVue.dependencies().forEach(dependency -> addDependency(project, dependency));
  }

  public void addDevDependencies(Project project) {
    ViteVue.devDependencies().forEach(devDependency -> addDevDependency(project, devDependency));
  }

  private void addDependency(Project project, String dependency) {
    npmService
      .getVersion("vite/vue", dependency)
      .ifPresentOrElse(
        version -> npmService.addDependency(project, dependency, version),
        () -> {
          throw new GeneratorException("Dependency not found: " + dependency);
        }
      );
  }

  private void addDevDependency(Project project, String devDependency) {
    npmService
      .getVersion("vite/vue", devDependency)
      .ifPresentOrElse(
        version -> npmService.addDevDependency(project, devDependency, version),
        () -> {
          throw new GeneratorException("DevDependency not found: " + devDependency);
        }
      );
  }

  public void addScripts(Project project) {
    // prettier-ignore
    Map
      .of(
        "build", "vue-tsc --noEmit && vite build --emptyOutDir",
        "dev", "vite",
        "preview", "vite preview",
        "start", "vite",
        "test", "jest src/test/javascript/spec"
      )
      .forEach((name, cmd) -> npmService.addScript(project, name, cmd));
  }

  public void addViteConfigFiles(Project project) {
    List
      .of(".eslintrc.js", "jest.config.js", "tsconfig.json", "vite.config.ts")
      .forEach(file -> projectRepository.add(project, SOURCE, file));
  }

  public void addRootFiles(Project project) {
    projectRepository.template(project, getPath(SOURCE, "webapp"), "index.html", "src/main/webapp");
    projectRepository.template(project, getPath(SOURCE, "webapp/app"), "env.d.ts", "src/main/webapp/app");
    projectRepository.template(project, getPath(SOURCE, "webapp/app"), "main.ts", "src/main/webapp/app");
  }

  public void addAppFiles(Project project) {
    project.addDefaultConfig(BASE_NAME);

    projectRepository.template(project, SOURCE_PRIMARY, "App.component.ts", DESTINATION_PRIMARY);
    projectRepository.template(project, SOURCE_PRIMARY, "index.ts", DESTINATION_PRIMARY);

    projectRepository.template(
      project,
      getPath(SOURCE, "test/spec/common/primary/app"),
      "App.spec.ts",
      "src/test/javascript/spec/common/primary/app"
    );
  }

  public void addAppFilesWithoutCss(Project project) {
    project.addDefaultConfig(BASE_NAME);

    projectRepository.template(project, SOURCE_PRIMARY, "App.html", DESTINATION_PRIMARY);
    projectRepository.template(project, SOURCE_PRIMARY, "App.vue", DESTINATION_PRIMARY);
  }

  public void addAppFilesWithCss(Project project) {
    project.addDefaultConfig(BASE_NAME);

    projectRepository.template(project, SOURCE_PRIMARY, "StyledApp.html", DESTINATION_PRIMARY, "App.html");
    projectRepository.template(project, SOURCE_PRIMARY, "StyledApp.vue", DESTINATION_PRIMARY, "App.vue");

    projectRepository.add(
      project,
      getPath(SOURCE, "webapp/content/images"),
      "JHipster-Lite-neon-green.png",
      "src/main/webapp/content/images"
    );
    projectRepository.add(project, getPath(SOURCE, "webapp/content/images"), "VueLogo.png", "src/main/webapp/content/images");
  }

  public void addJestSonar(Project project) {
    String oldText = "\"cacheDirectories\": \\[";
    String newText =
      """
      "jestSonar": \\{
          "reportPath": "target/test-results/jest",
          "reportFile": "TESTS-results-sonar.xml"
        \\},
        "cacheDirectories": \\[""";
    projectRepository.replaceText(project, "", PACKAGE_JSON, oldText, newText);
  }
}
