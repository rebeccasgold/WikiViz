package org.wikibrain.viz;

import com.google.gson.*;
import gnu.trove.map.TIntIntMap;
import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;
import org.wikibrain.core.cmd.Env;
import org.wikibrain.core.cmd.EnvBuilder;
import org.wikibrain.core.dao.*;
import org.wikibrain.core.lang.Language;
import org.wikibrain.core.model.CategoryGraph;
import org.wikibrain.core.model.LocalPage;
import org.wikibrain.core.model.NameSpace;
import org.wikibrain.pageview.PageViewDao;
import org.wikibrain.sr.SRBuilder;
import org.wikibrain.sr.SRMetric;
import org.wikibrain.sr.SRResult;
import org.wikibrain.sr.vector.DenseVectorSRMetric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Shilad Sen
 */
public class Test {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Test.class);

    public static String [] TOP_LEVEL_CATS = {"Geography", "History", "Knowledge", "People", "Religion", "Science"};

    /**
     * This is a method to calibrate the model for word2vec.
     * @throws Exception
     */
      public static void buildWord2VecModel() throws Exception {
            SRBuilder.main(new String[]{"-c", "wikiviz.conf", "-m", "word2vec-wbmap", "-g", "wordsim353.txt"});
      }

    public static void main(String args[]) throws Exception {
        // Only do this once
//        Loader.main(new String[] { "-l", "simple", "-c", "wikiviz.conf" });
//        if (true) return;

//        buildWord2VecModel(); // comment this after first run
        Env env = EnvBuilder.envFromArgs(new String[]{"-c", "wikiviz.conf"});

        LocalPageDao pageDao = env.getConfigurator().get(LocalPageDao.class);
        LocalLinkDao linkDao = env.getConfigurator().get(LocalLinkDao.class);
        PageViewDao viewDao = env.getConfigurator().get(PageViewDao.class);
        LocalCategoryMemberDao catDao = env.getConfigurator().get(LocalCategoryMemberDao.class);

        Set<LocalPage> topCats = new HashSet<LocalPage>();
        for (String c : TOP_LEVEL_CATS) {
            LocalPage p = pageDao.getByTitle(Language.SIMPLE, NameSpace.CATEGORY, "Category:"+c);
            if (p == null) {
                LOG.warn("Couldn't find top level category for " + c);
            } else {
                topCats.add(p);
            }
        }

        LOG.info("Loading views...");
        DateTime now = new DateTime();
        TIntIntMap views = viewDao.getAllViews(Language.SIMPLE, now.minusDays(1000), now);
        int n = 0;
        for (LocalPage p : pageDao.get(new DaoFilter().setNameSpaces(NameSpace.CATEGORY))) {
            if (views.containsKey(p.getLocalId())) {
                n++;
            }
        }
        System.err.println("found page views for " + n + " cats");

        BufferedWriter writer = new BufferedWriter(new FileWriter("articles.json"));
        SRMetric sr = env.getConfigurator().get(SRMetric.class, "word2vec-wbmap", "language", "simple");
        for (LocalPage p : pageDao.get(DaoFilter.normalPageFilter(Language.SIMPLE))) {
            double pr = linkDao.getPageRank(Language.SIMPLE, p.getLocalId());
            int v = views.get(p.getLocalId());
            float[] vec = ((DenseVectorSRMetric)sr).getPageVector(p.getLocalId());
            if (vec != null) {
                writeJson(writer, p, v, pr, vec);
            }
        }

        CategoryGraph graph = catDao.getGraph(Language.SIMPLE);
        int numBigArticles = 0;
        for (LocalPage cat : pageDao.get(new DaoFilter().setNameSpaces(NameSpace.CATEGORY))) {
            // Get the minimum # of pageviews in here
            Map<Integer, LocalPage> members = catDao.getCategoryMembers(cat);
            if (members != null) {
                float vec[] = new float[20];
                int nArticles = 0;
                for (LocalPage p2 : members.values()) {
                    float[] v = ((DenseVectorSRMetric)sr).getPageVector(p2.getLocalId());
                    if (v != null && p2.getNameSpace() == NameSpace.ARTICLE) {
                        for (int j = 0; j < v.length; j++) vec[j] += v[j];
                        nArticles++;
                    }
                }
                if (nArticles > 10) {
                    for (int j = 0; j < vec.length; j++) {
                        vec[j] /= nArticles;
                    }
                    System.out.print("results for " + cat + ":");
                    for (SRResult r : ((DenseVectorSRMetric)sr).mostSimilar(vec, 5, null)) {
                        System.out.print(" " + pageDao.getById(Language.SIMPLE, r.getId()));
                    }
                    System.out.println();
                    numBigArticles++;
                    int v = views.get(cat.getLocalId());
                    double pr = graph.catCosts[graph.catIdToIndex(cat.getLocalId())];
                    writeJson(writer, cat, v, pr, vec);
                }
            }
        }
        writer.close();
    }

    public static void writeJson(BufferedWriter writer, LocalPage page, int views, double pageRank, float [] vec) throws IOException {
        JsonObject obj = new JsonObject();
        JsonArray jsonVec = new JsonArray();
        for (float x : vec) jsonVec.add(new JsonPrimitive(x));
        obj.add("article", new JsonPrimitive(page.getTitle().toString()));
        obj.add("views", new JsonPrimitive(views));
        obj.add("pageRank", new JsonPrimitive(pageRank));
        obj.add("vector", jsonVec);
        writer.write(obj.toString() + "\n");
    }
}