package org.atteo.cachemanifest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Mojo(name = "generate-manifest", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class CacheManifestMojo extends AbstractMojo {

	@Parameter(defaultValue = "/target/classes/META-INF/resources/application.cachemanifest")
	private String manifestPath;

	@Parameter
	private String manifestVersion;

	@Parameter
	private List<FileSet> fileResources = new ArrayList<>();

	@Parameter
	private List<String> resources = new ArrayList<>();

	@Parameter
	private List<String> resourcesReferencedFrom = new ArrayList<>();

	@Parameter
	private List<String> networkResources = new ArrayList<>();

	@Parameter
	private String fallback;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	MavenProject project;

	@Override
	public void execute() throws MojoExecutionException {
		final FileSetManager fileSetManager = new FileSetManager(getLog());

		final Set<String> resourceEntries = new TreeSet<>();

		for (FileSet resource : fileResources) {
			resourceEntries.addAll(Arrays.asList(fileSetManager.getIncludedFiles(resource)));
		}

		resourceEntries.addAll(resources);

		for (String referencedFrom : resourcesReferencedFrom) {
			File input = new File(project.getBasedir(), referencedFrom);
			try {
				Document doc = Jsoup.parse(input, "UTF-8");
				Elements scripts = doc.getElementsByTag("script");
				Elements links = doc.getElementsByTag("link");

				for (Element script : scripts) {
					String src = script.attr("src");
					if (src != null) {
						resourceEntries.add(src);
					}
				}

				for (Element link : links) {
					String rel = link.attr("rel");
					String href = link.attr("href");
					if (href != null && rel.equals("stylesheet")) {
						resourceEntries.add(href);
					}
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		final Set<String> networkResourceEntries = new TreeSet<>(networkResources);

		try {
			File file = new File(manifestPath);

			if (!file.isAbsolute()) {
				file = new File(project.getBasedir(), manifestPath);
			}

			if (file.exists()) {
				file.delete();
			}

			file.getParentFile().mkdirs();
			file.createNewFile();

			try (PrintWriter manifest = new PrintWriter(file, "UTF-8")) {
				if (manifestVersion == null || manifestVersion.isEmpty()) {
					manifestVersion = project.getVersion();

					if (manifestVersion.endsWith("SNAPSHOT")) {
						manifestVersion += "-" + String.valueOf(new Date().getTime());
					}
				}

				getLog().info("Generating version: " + manifestVersion);
				getLog().info(resourceEntries.size() + " resources");

				manifest.println("CACHE MANIFEST\n");
				manifest.println("# version: " + manifestVersion);

				if (!resourceEntries.isEmpty()) {
					manifest.println();
					for (String resource : resourceEntries) {
						manifest.println(resource);
					}
				}

				if (!networkResourceEntries.isEmpty()) {
					manifest.println();
					manifest.println("NETWORK:");
					for (String networkResource : networkResourceEntries) {
						manifest.println(networkResource);
					}
				}

				if (fallback != null && !fallback.isEmpty()) {
					manifest.println();
					manifest.println("FALLBACK:");
					manifest.println(fallback);
				}

				manifest.flush();
			}
		} catch (IOException e) {
			getLog().error("Cache manifest generation failed", e);
		}
	}
}
