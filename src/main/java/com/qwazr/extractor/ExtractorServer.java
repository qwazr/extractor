/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.extractor;

import com.qwazr.cluster.ClusterManager;
import com.qwazr.cluster.ClusterServiceInterface;
import com.qwazr.server.ApplicationBuilder;
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.RestApplication;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExtractorServer implements BaseServer {

	private final GenericServer server;
	private final ExtractorManager extractorManager;

	private ExtractorServer(final ServerConfiguration configuration)
			throws IOException, URISyntaxException, ClassNotFoundException {
		final ExecutorService executorService = Executors.newCachedThreadPool();
		final GenericServer.Builder builder = GenericServer.of(configuration, executorService);

		final Set<String> services = new HashSet<>();
		services.add(ClusterServiceInterface.SERVICE_NAME);
		services.add(ExtractorServiceInterface.SERVICE_NAME);

		final ApplicationBuilder webServices = ApplicationBuilder.of("/*").classes(RestApplication.JSON_CLASSES).
				singletons(new WelcomeShutdownService());

		new ClusterManager(executorService, configuration).registerProtocolListener(builder, services)
				.registerContextAttribute(builder)
				.registerWebService(webServices);

		extractorManager = new ExtractorManager().registerContextAttribute(builder).registerWebService(webServices);
		extractorManager.registerServices();

		builder.getWebServiceContext().jaxrs(webServices);
		server = builder.build();
	}

	@Override
	public GenericServer getServer() {
		return server;
	}

	public ExtractorServiceInterface getService() {
		return extractorManager.getService();
	}

	public static void main(final String... args) throws Exception {
		new ExtractorServer(new ServerConfiguration(args)).start();
	}

}