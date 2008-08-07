package org.carrot2.webapp.model;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.carrot2.webapp.jawr.JawrUrlGenerator;
import org.carrot2.webapp.util.UserAgentUtils;
import org.simpleframework.xml.ElementList;

/**
 * Contains URLs for CSS and JS generated by Jawr.
 */
public class AssetUrlsModel
{
    @ElementList(name = "css-urls", entry = "css-url")
    public final List<String> cssUrls;

    @ElementList(name = "js-urls", entry = "js-url")
    public final List<String> jsUrls;

    public AssetUrlsModel(SkinModel currentSkin, HttpServletRequest request,
        JawrUrlGenerator generator)
    {
        final String sprite = UserAgentUtils.isModernBrowser(request)
            && currentSkin.sprited ? "-sprite" : "";

        this.cssUrls = generator.getCssUrls(request, "/" + currentSkin.id + sprite
            + ".jcss");
        this.jsUrls = generator.getJsUrls(request, "/" + currentSkin.id + ".jjs");
    }
}
