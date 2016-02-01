package org.wikibrain.viz;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.wikibrain.Loader;
import org.wikibrain.conf.ConfigurationException;
import org.wikibrain.conf.Configurator;
import org.wikibrain.core.WikiBrainException;
import org.wikibrain.core.cmd.Env;
import org.wikibrain.core.cmd.EnvBuilder;
import org.wikibrain.core.dao.DaoException;
import org.wikibrain.core.dao.LocalLinkDao;
import org.wikibrain.core.dao.LocalPageDao;
import org.wikibrain.core.dao.RawPageDao;
import org.wikibrain.core.lang.Language;
import org.wikibrain.core.lang.LocalId;
import org.wikibrain.core.model.LocalPage;
import org.wikibrain.core.model.RawPage;
import org.wikibrain.matrix.DenseMatrix;
import org.wikibrain.pageview.PageViewDao;
import org.wikibrain.sr.SRBuilder;
import org.wikibrain.sr.SRMetric;
import org.wikibrain.sr.SRResultList;
import org.wikibrain.sr.vector.DenseVectorGenerator;
import org.wikibrain.sr.vector.DenseVectorSRMetric;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shilad Sen
 */
public class Test {

    static Logger log = Logger.getLogger(Test.class);

    /**
     * This is a method to calibrate the model for word2vec.
     * @throws Exception
     */
      public static void buildWord2VecModel() throws Exception {
            SRBuilder.main(new String[]{"-c", "wikiviz.conf", "-m", "word2vec-wbmap", "-g", "wordsim353.txt"});
      }

    public static void main(String args[]) throws Exception {
        BasicConfigurator.configure();
        // Only do this once
        buildWord2VecModel(); // comment this after first run

//        Env env = EnvBuilder.envFromArgs(new String[]{"-c", "wb-map.conf"});
    }
//
//    /**
//     * This functiong gets and prints the most similar page in simple english to words from our input list.
//     *
//     * After playing around with this, I found it unsatisfactory for building groupings, as the pages were frequently
//     * weird and/or inaccurate.
//     *
//     * TODO: Look into doing this on full english, which may have more accurate results.
//     * @param env
//     * @throws ConfigurationException
//     * @throws DaoException
//     */
//    public static void getPages(Env env) throws ConfigurationException, DaoException {
//        Configurator conf = env.getConfigurator();
//        RawPageDao rpDao = conf.get(RawPageDao.class);
//
//        Language simple = Language.getByLangCode("simple");
//
//        SRMetric sr = conf.get(
//                SRMetric.class, "milnewitten",
//                "language", simple.getLangCode());
//
//        for (String s : wordList) {
//            SRResultList similar = sr.mostSimilar(s, 4);
//            try {
//                System.out.print(s + " --> ");
//                RawPage[] rps = new RawPage[4];
//                for(int i = 0; i<4; i++){
//                    RawPage rp = rpDao.getById(simple, similar.getId(i));
//                    System.out.print(rp.getTitle() + ", ");
//                }
//                System.out.println();
//            } catch (NullPointerException e) {
//                System.out.println(s + " is null");
//            }
//
////          rp.getPlainText(false)
//        }
//
//    }
//
//    /**
//     * This functiong gets the 20-dimensional vectors for each word from the given list in wikibrain.
//     *
//     * Possible TODO: Look into more/fewer dimensions, which could then be compressed similarly to how I'm doing now.
//     * @param env
//     * @throws ConfigurationException
//     * @throws FileNotFoundException
//     * @throws UnsupportedEncodingException
//     */
//    public static void getVectors(Env env) throws ConfigurationException, FileNotFoundException, UnsupportedEncodingException {
//        DenseVectorSRMetric w2v = (DenseVectorSRMetric) env.getConfigurator().get(
//                SRMetric.class, "word2vec-wbmap", "language", Language.SIMPLE.getLangCode());
//        DenseVectorGenerator generator = w2v.getGenerator();
//        Map<String, float[]> vectors = new HashMap<String, float[]>();
//        for(String s : wordList){
//            float[] vector = generator.getVector(s);
//            if (vector != null){
//                vectors.put(s, vector);
//            }
//        }
//
//        PrintWriter writer = new PrintWriter("points.txt",  "UTF-8");
//        for(String w : vectors.keySet()) {
//            writer.println("\"" + w + "\": [" + seeVector(vectors.get(w)) + "],");
//        }
//        writer.close();
//    }
//
//    /**
//     * This is an unfinished function that is meant to get the pageviews for a given word's most similar page. This will
//     * also probably suffer the same issues as getPage() as the pages are frequently inaccurate and it uses the same
//     * mechanism for getting pages as that function.
//     * @param env
//     * @throws ConfigurationException
//     * @throws DaoException
//     */
//    public static void getPageViews(Env env) throws ConfigurationException, DaoException {
//        PageViewDao viewDao = env.getConfigurator().get(PageViewDao.class);
//        DateTime start = new DateTime(2015, 11, 15, 0, 0, 0);
//        DateTime end = new DateTime(2015, 12, 1, 0, 0, 0);
//        viewDao.ensureLoaded(start, end, env.getLanguages());
//
//        Configurator conf = env.getConfigurator();
//        RawPageDao rpDao = conf.get(RawPageDao.class);
//
//        Language simple = Language.getByLangCode("simple");
//
//        SRMetric sr = conf.get(
//                SRMetric.class, "milnewitten",
//                "language", simple.getLangCode());
//
//        for (String s : wordList) {
//            SRResultList similar = sr.mostSimilar(s, 1);
//            try {
//                System.out.print(s + " --> ");
//                int views = viewDao.getNumViews(new LocalId(simple, similar.getId(0)), start, end);
//                System.out.print(views);
//            } catch (NullPointerException e) {
//                System.out.println(s + " is null");
//            }
//            System.out.println();
//        }
//    }
//
//    public static String seeVector(float[] floats){
//        String s = "";
//        for(float f : floats){
//            s += new Float(f).toString() + ", ";
//        }
//        return s.substring(0,s.length()-1);
//    }
}