/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import com.qwazr.cluster.ClusterServer;
import com.qwazr.cluster.service.ClusterServiceImpl;
import com.qwazr.utils.server.AbstractServer;
import com.qwazr.utils.server.RestApplication;
import com.qwazr.utils.server.ServletApplication;
import io.undertow.security.idm.IdentityManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import javax.servlet.ServletException;
import javax.ws.rs.ApplicationPath;
import java.io.IOException;
import java.util.Set;

public class ExtractorServer extends AbstractServer {

	public final static String SERVICE_NAME_EXTRACTOR = "extractor";

	private final static ServerDefinition serverDefinition = new ServerDefinition();

	static {
		serverDefinition.defaultWebApplicationTcpPort = 9091;
		serverDefinition.mainJarPath = "qwazr-extractor.jar";
		serverDefinition.defaultDataDirName = "qwazr";
	}

	private ExtractorServer() {
		super(serverDefinition);
	}

	@ApplicationPath("/")
	public static class TextExtractorApplication extends RestApplication {

		@Override
		public Set<Class<?>> getClasses() {
			Set<Class<?>> classes = super.getClasses();
			classes.add(ExtractorServiceImpl.class);
			classes.add(ClusterServiceImpl.class);
			return classes;
		}
	}

	public static void loadParserManager() throws IOException {
		ParserManager.load();
	}

	@Override
	public void commandLine(CommandLine cmd) throws IOException {
	}

	@Override
	public void load() throws IOException {
		ClusterServer.load(getWebServicePublicAddress(), getCurrentDataDir());
		load();
	}

	@Override
	public Class<TextExtractorApplication> getRestApplication() {
		return TextExtractorApplication.class;
	}

	@Override
	protected Class<ServletApplication> getServletApplication() {
		return null;
	}

	@Override
	protected IdentityManager getIdentityManager(String realm) {
		return null;
	}

	public static void main(String[] args)
			throws IOException, ParseException, ServletException, InstantiationException, IllegalAccessException {
		new ExtractorServer().start(args);
	}

}
