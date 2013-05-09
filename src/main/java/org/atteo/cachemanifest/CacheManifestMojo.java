package org.atteo.cachemanifest;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * @phase generated-resources
 * @goal generate-manifest
 * @threadSafe true
 */
public class CacheManifestMojo extends AbstractMojo {

	/**
	 * @parameter default-value="/src/main/webapp/application.cachemanifest"
	 */
	private String manifestPath;

	/**
	 * @parameter default-value="1"
	 */
	private String manifestVersion;

	/**
	 * @parameter
	 */
	private List<FileSet> resources = new ArrayList<>();

	/**
	 * @parameter
	 */
	private List<String> networkResources = new ArrayList<>();

	/**
	 * @parameter
	 */
	private String fallback;

	@Override
	public void execute() throws MojoExecutionException {
		final FileSetManager fileSetManager = new FileSetManager(getLog());

		final Set<String> resourceEntries = new HashSet<>();

		for (FileSet resource : resources) {
			resourceEntries.addAll(Arrays.asList(fileSetManager.getIncludedFiles(resource)));
		}

		final Set<String> networkResourceEntries = new HashSet<>(networkResources);

		try {
			FileUtils.touch(new File(manifestPath));
			try (PrintWriter manifest = new PrintWriter(manifestPath, "UTF-8")) {
				manifest.println("CACHE MANIFEST\n");
				manifest.println("# version: " + this.manifestVersion);

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
