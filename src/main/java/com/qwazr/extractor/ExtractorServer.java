/**
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
import com.qwazr.server.BaseServer;
import com.qwazr.server.GenericServer;
import com.qwazr.server.WelcomeShutdownService;
import com.qwazr.server.configuration.ServerConfiguration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExtractorServer implements BaseServer {

	private final GenericServer server;
	private final ExtractorManager extractorManager;

	private ExtractorServer(final ServerConfiguration configuration) throws IOException, URISyntaxException {
		final ExecutorService executorService = Executors.newCachedThreadPool();
		final GenericServer.Builder builder =
				GenericServer.of(configuration, executorService).webService(WelcomeShutdownService.class);
		new ClusterManager(executorService, configuration).registerHttpClientMonitoringThread(builder)
				.registerProtocolListener(builder)
				.registerWebService(builder);
		extractorManager = new ExtractorManager().registerWebService(builder);
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