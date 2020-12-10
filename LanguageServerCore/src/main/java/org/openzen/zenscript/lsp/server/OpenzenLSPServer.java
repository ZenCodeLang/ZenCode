package org.openzen.zenscript.lsp.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class OpenzenLSPServer implements LanguageServer, LanguageClientAware {
	private final TextDocumentService textDocumentService;
	private final WorkspaceService workspaceService;

	@Nullable
	private LanguageClient client;

	public OpenzenLSPServer() {
		this.textDocumentService = new OpenzenTextDocumentService();
		this.workspaceService = new OpenzenWorkspaceService();
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		return CompletableFuture.supplyAsync(() -> {
			final ServerCapabilities serverCapabilities = new ServerCapabilities();
			serverCapabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
			serverCapabilities.setCompletionProvider(new CompletionOptions());
			final ServerInfo serverInfo = new ServerInfo("ZenCode LSP", "0.0.0");

			return new InitializeResult(serverCapabilities, serverInfo);
		});
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		this.client = null;
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void exit() {
		System.exit(client == null ? 0 : 1);
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return textDocumentService;
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	@Override
	public void connect(LanguageClient client) {
		this.client = client;
		SemanticTokens
	}

	@Nullable
	public LanguageClient getClient() {
		return client;
	}
}