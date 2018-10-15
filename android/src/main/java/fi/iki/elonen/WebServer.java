package fi.iki.elonen;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.Map;

import android.content.Context;

public class WebServer extends SimpleWebServer2
{
    private final Context mContext;
    private String assetsRoot = null;
    private Boolean useAssetsDirectory;

    public WebServer(Context context, String localAddr, int port, String assetsRoot) throws IOException {
        // Default to android assets directory
        super(localAddr, port, new File("/"), false, "*");
        this.assetsRoot = assetsRoot;
        addMimeTypes();
        mContext = context;
    }

    public WebServer(Context context, String localAddr, int port, File wwwroot) throws IOException {
        super(localAddr, port, wwwroot, false, "*");
        addMimeTypes();
        mContext = context;
    }

    protected void addMimeTypes () {
        mimeTypes().put("xhtml", "application/xhtml+xml");
        mimeTypes().put("opf", "application/oebps-package+xml");
        mimeTypes().put("ncx", "application/xml");
        mimeTypes().put("epub", "application/epub+zip");
        mimeTypes().put("otf", "application/x-font-otf");
        mimeTypes().put("ttf", "application/x-font-ttf");
        mimeTypes().put("js", "application/javascript");
        mimeTypes().put("svg", "image/svg+xml");
    }

    @Override
    protected boolean useGzipWhenAccepted(Response r) {
        return super.useGzipWhenAccepted(r) && r.getStatus() != Response.Status.NOT_MODIFIED;
    }

    @Override
    protected Response defaultRespond(Map<String, String> headers, IHTTPSession session, String uri) {
        if (assetsRoot != null) {
            return respondFromAssets(headers, session, uri);
        }

        return super.defaultRespond(headers, session, uri);
    }

    protected Response respondFromAssets(Map<String, String> headers, IHTTPSession session, String uri) {
        if (uri == null) {
            return getNotFoundResponse();
        }

        // Remove URL arguments
        uri = assetsRoot + uri.trim().replace(File.separatorChar, '/');

        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }

        String mimeTypeForFile = getMimeTypeForFile(uri);
        InputStream mbuffer = null;

        try {
           mbuffer = mContext.getAssets().open(uri);
           return new Response(NanoHTTPD.Response.Status.OK, mimeTypeForFile, mbuffer, mbuffer.available());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getNotFoundResponse();
    }

}
