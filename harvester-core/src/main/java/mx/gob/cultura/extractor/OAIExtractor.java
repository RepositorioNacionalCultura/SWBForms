/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mx.gob.cultura.extractor;

import java.util.logging.Logger;
import org.json.XML;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import java.io.InputStream;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Date;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import mx.gob.cultura.transformer.DataObjectScriptEngineMapper;

import mx.gob.cultura.util.Util;
import org.json.JSONException;
import org.semanticwb.datamanager.DataList;
import org.semanticwb.datamanager.DataObject;
import org.semanticwb.datamanager.SWBDataSource;
import org.semanticwb.datamanager.SWBScriptEngine;

/**
 *
 * @author juan.fernandez
 */
public class OAIExtractor extends ExtractorBase {

    static Logger log = Logger.getLogger(OAIExtractor.class.getName());

    protected DataObject extractorDef;
    private SWBScriptEngine engine;
    private SWBDataSource dsExtract;
    private boolean extracting;
    private boolean update;

//    public static enum STATUS {
//        LOADED, STARTED, EXTRACTING, STOPPED, ABORTED, FAILLOAD
//    }
    private String status = "LOADED";

    /**
     *
     * @param doID
     * @param eng
     */
    public OAIExtractor(String doID, SWBScriptEngine eng) {
        super(doID, eng);
        extractorDef = super.extractorDef;
        engine = super.engine;
        dsExtract = super.dsExtract;
        //dsEPoint = super.dsEPoint;
    }

    @Override
    public void start() {
        System.out.println("canStart(" + canStart() + ")");
        if (canStart()) {
            log.info("ModsExtractor :: Started extractor " + getName());
            try {
                extract();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        status = "STOPPED";
    }

    @Override
    public String getStatus() {
        return status.toString();
    }

    public void setStatus(String s) {
        status = s;
    }

    @Override
    public String getName() {
        if (null != extractorDef) {
            return extractorDef.getString("name");
        }
        return null;
    }

    @Override
    public boolean canStart() {

        return !status.equals("FAILLOAD") && (status.equals("STOPPED") || status.equals("LOADED"));
    }

    @Override
    public String getType() {

        String ret = extractorDef.getString("");
        return ret;
    }

    @Override
    public void extract() throws Exception {

        //2017-12-01T13:05:00.000
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        //DataObject do_extrac = null;
        String ext_name = null;
        String ext_coll = null;
        String ext_url = null;
        String ext_verbs = null;
        String[] ext_prefix = null;
        boolean ext_resTkn = false;
        String ext_resTknVal = null;
        String ext_class = null;
        String ext_script = null;
        boolean ext_period = false;
        int ext_inter = -1;
        String ext_mapeo = null;
        String ext_created = null;
        String ext_lastExec = null;
        String ext_status = null;
        int ext_r2harvest = -1;
        int ext_harvestered = 0;
        int ext_r2process = 0;
        int ext_process = 0;
        int ext_cursor = 0;

        String pid = extractorDef.getId();

        String jsonstr = null;

        String resTkn_str = null;
        String tmpTkn = null;
        String listSize_str = null;
        String cursor_str = null;

        int listSize = 0;
        int cursor = 0;
        int resTkn = 0;

        if (null != pid) {
            //do_extrac = dsExtract.fetchObjById(pid);

            if (null != extractorDef) {
                ext_name = extractorDef.getString("name");
                ext_coll = extractorDef.getString("collection", "objects");
                ext_url = extractorDef.getString("url");
                ext_script = extractorDef.getString("script");
                ext_verbs = extractorDef.getString("verbs");
                DataList dlpfx = extractorDef.getDataList("prefix");
                System.out.println("num items:" + dlpfx.size());
                ext_prefix = new String[dlpfx.size()];
                for (int i = 0; i < dlpfx.size(); i++) {
                    ext_prefix[i] = dlpfx.getString(i);
                    System.out.println("prefix:" + ext_prefix[i]);
                }
                ext_resTkn = extractorDef.getBoolean("resumptionToken", false);
                ext_resTknVal = extractorDef.getString("tokenValue", null);
                ext_period = extractorDef.getBoolean("periodicity");
                ext_class = extractorDef.getString("class");
                ext_inter = extractorDef.getInt("interval", 0);
                ext_mapeo = extractorDef.getString("mapeo");
                ext_created = extractorDef.getString("created");
                ext_lastExec = extractorDef.getString("lastExecution", null);
                ext_status = extractorDef.getString("status");
                ext_r2harvest = extractorDef.getInt("rows2harvest", -1);
                ext_harvestered = extractorDef.getInt("harvestered", 0);
                ext_r2process = extractorDef.getInt("rows2Processed", -1);
                ext_process = extractorDef.getInt("processed", 0);
                ext_cursor = extractorDef.getInt("cursor", -1);

                extractorDef.put("status", "STARTED");
                dsExtract.updateObj(extractorDef);
                ExtractorManager.hmExtractorDef.put(pid, extractorDef);

                MongoClient client = new MongoClient("localhost", 27017);
                DB db = client.getDB(ext_name.toUpperCase());

                if (null != ext_lastExec) {
                    ext_lastExec = ext_lastExec.replace(" ", "T");
                }

                HashMap<String, String> hm = Util.loadOccurrences(engine);
                boolean isResumeExtract = false;

                if (null != ext_prefix) {
                    try {

                        for (String pfx : ext_prefix) {
                            System.out.println("\n\nEmpezando con:...." + pfx);
                            // creando la colección por prefijo
                            DBCollection objects = db.getCollection(pfx);
                            try {
                                objects.dropIndex("oaiid");
                            } catch (Exception e) {
                                System.out.println("Error al eliminar el indice de la colección...");
                            }
                            objects.createIndex("oaiid");

                            String urlConn = ext_url + "?verb=" + ext_verbs + "&metadataPrefix=" + pfx;
                            urlConn = ext_url + "?verb=" + ext_verbs + "&metadataPrefix=" + pfx;
                            if (update && null != ext_lastExec) {
                                urlConn = ext_url + "?verb=" + ext_verbs + "&metadataPrefix=" + pfx + "&from=" + ext_lastExec;
                            }

                            if ((null != ext_status && ext_status.equals("EXTRACTING") || (null != ext_resTknVal && !ext_resTknVal.equals("0") && ext_resTknVal.length() > 0)) && resTkn_str != null) {
                                urlConn = ext_url + "?verb=" + ext_verbs + "&resumptionToken=" + resTkn_str;
                                resTkn_str = ext_resTknVal;
                                listSize = ext_r2harvest;
                                cursor = ext_cursor;
                                isResumeExtract = true;
                            }

                            URL theUrl = new URL(urlConn);

                            System.out.println("Making request " + theUrl.toString());
                            extractorDef.put("lastExecution", sdf.format(new Date()));

                            boolean tknFound = false;
                            int numextract = 0;
                            int numalready = 0;
                            int retries = 0;
                            System.out.println("Empezando extracción..." + ext_name.toUpperCase());
                            do {
                                tknFound = false;
                                try {

                                    jsonstr = Util.makeRequest(theUrl, true);
                                    jsonstr = Util.replaceOccurrences(hm, jsonstr);

                                    if (jsonstr.contains("resumptionToken")) {
                                        tknFound = true;
                                    }

                                    if (jsonstr != null && jsonstr.startsWith("#Error") && jsonstr.endsWith("#")) {
                                        System.out.println(jsonstr.substring(1, jsonstr.length() - 1));
                                        break;
                                    }

                                    JSONObject json = XML.toJSONObject(jsonstr);
                                    //System.out.println("JSON:"+json.toString());
                                    JSONObject jsonroot = json.getJSONObject("OAI-PMH");
                                    JSONObject jsonLst = jsonroot.getJSONObject("ListRecords");

                                    JSONObject jsonTkn = null;
                                    if (tknFound) {

                                        try {
                                            System.out.println("Buscando token...");
                                            jsonTkn = jsonLst.getJSONObject("resumptionToken");

                                            if (null != jsonTkn) {
                                                System.out.println("Token encontrado...");
                                                resTkn_str = jsonTkn.getString("content");
                                                listSize_str = jsonTkn.getString("completeListSize");
                                                cursor_str = jsonTkn.getString("cursor");
                                                if (listSize == 0) {
                                                    try {
                                                        listSize = Integer.parseInt(listSize_str);
                                                    } catch (Exception e) {
                                                        System.out.println("Error: Invalid records size number");
                                                        e.printStackTrace();
                                                        listSize = -1;
                                                    }
                                                }
                                                try {
                                                    cursor = Integer.parseInt(cursor_str);
                                                } catch (Exception e) {
                                                    System.out.println("Error: Invalid cursor size number");
                                                    e.printStackTrace();
                                                    cursor = -1;
                                                }
                                            }

                                        } catch (Exception e) {
                                            //System.out.println("Error...No Resumption Token found.");
                                            //e.printStackTrace();
                                            resTkn_str = null;
                                            listSize_str = null;
                                            cursor_str = null;
                                        }
                                    }

                                    JSONArray jsonRd = jsonLst.getJSONArray("record");

                                    extractorDef.put("status", "EXTRACTING");
                                    extracting = true;
                                    extractorDef.put("rows2harvest", (listSize - numextract));
                                    extractorDef.put("cursor", cursor);
                                    extractorDef.put("rows2Processed", listSize);
                                    extractorDef.put("processed", 0);
                                    dsExtract.updateObj(extractorDef);
                                    ExtractorManager.hmExtractorDef.put(pid, extractorDef);

                                    if (listSize > (numextract + numalready)) {

                                        String nid = null;
                                        for (int i = 0; i < jsonRd.length(); i++) {
                                            JSONObject jsonhdr = jsonRd.getJSONObject(i).getJSONObject("header");
                                            nid = jsonhdr.getString("identifier");

                                            try {
//                                    BasicDBObject dbQuery = new BasicDBObject("oaiid", nid);
//                                    DBObject dbres = objects.findOne(dbQuery);
//                                    if (null == dbres) {
                                                numextract++;
                                                String nodeAsString = jsonRd.getJSONObject(i).toString();
                                                DataObject rec = new DataObject();
                                                rec.put("oaiid", nid);
                                                rec.put("body", DataObject.parseJSON(nodeAsString));
                                                BasicDBObject bjson = Util.toBasicDBObject(rec);
                                                objects.insert(bjson);
//                                    } else {
//                                        numalready++;
//                                        //System.out.println("Already Extracted...");
//                                    }

                                            } catch (Exception e) {
                                                System.out.println("Error..." + e.toString());
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        setStatus("STOPPED");
                                    }

                                    if (resTkn_str != null) {
                                        tmpTkn = resTkn_str;
                                        extractorDef.put("tokenValue", resTkn_str);
                                        theUrl = new URL(ext_url + "?verb=" + ext_verbs + "&resumptionToken=" + resTkn_str);
                                        resTkn_str = null;
                                    }
                                    extractorDef.put("harvestered", numextract);
                                    dsExtract.updateObj(extractorDef);
                                    ExtractorManager.hmExtractorDef.put(pid, extractorDef);
                                } catch (JSONException jex) {
                                    Thread.sleep(5000);
                                    retries++;
                                    jex.printStackTrace();
                                }

                                if (getStatus().equals("STOPPED") || getStatus().equals("ABORT")) {
                                    break;
                                }
                                if (numextract % 100 == 0 && numextract > 0) {
                                    System.out.println("Extracted ==>" + numextract);
                                }
                                if (numalready % 100 == 0 && numalready > 0) {
                                    System.out.println("Already ==>" + numalready + "(" + listSize + ")");
                                }
                                if (retries > 0) {
                                    System.out.println("Retries ==>" + retries);
                                }

                                System.out.println("Retries(" + retries + ")Token(" + tmpTkn + "), List(" + listSize + "), Extracted(" + numextract + "),Existing(" + numalready + ")");
                                tmpTkn = null;
                            } while (retries < 5 && tknFound && listSize > (numextract + numalready));  //(listSize > numextract && listSize > numalready) && 
                            update = false;
                            extractorDef.put("status", "STOPPED");
                            extracting = false;
                            dsExtract.updateObj(extractorDef);
                            ExtractorManager.hmExtractorDef.put(pid, extractorDef);
                            ExtractorManager.getInstance().loadExtractor(extractorDef);
                            System.out.println("Finalizando extracción..." + ext_name.toUpperCase() + " ==> Extracted(" + numextract + "), EXISTING(" + numalready + ")");
                            numextract = 0;
                            numalready = 0;
                            listSize = 0;
                            resTkn_str = null;

                        }
                    } catch (Exception e) {

                    }
                }

            }
        }

    }

    @Override
    public boolean replace() {
        boolean ret = false;
        try {
            MongoClient client = new MongoClient("localhost", 27017);
            DB db = client.getDB(extractorDef.getString("name").toUpperCase());
            db.dropDatabase();
//            String collName = extractorDef.getString("collection", "objects");
//            if (db.collectionExists(collName)) {
//                db.getCollection(collName).drop();
//            }
            ret = true;
            extract();
        } catch (Exception e) {
            System.out.println("Error al tratar de borrar la Base de Datos");
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    /**
     * Método para traer los registros a partir de la última ejecución, revisar
     * como pedir los registros por fecha
     */
    public boolean update() {
        boolean ret = false;
        update = true;
        try {
            extract();
            ret = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getScript() {

        String ret = extractorDef.getString("script");

        return ret;
    }

    @Override
    public void process() throws Exception {

        String scriptsrc = extractorDef.getString("script");
        ScriptEngineManager factory = new ScriptEngineManager();

        if (null != scriptsrc && scriptsrc.trim().length() > 0) 
        {

            ScriptEngine engine = factory.getEngineByName("JavaScript");
            DataObjectScriptEngineMapper mapper = new DataObjectScriptEngineMapper(engine, scriptsrc);

            DataList dlpfx = extractorDef.getDataList("prefix");
            System.out.println("num items:" + dlpfx.size());
            String[] ext_prefix = new String[dlpfx.size()];
            ConcurrentHashMap<String, ConcurrentHashMap<String, DataObject>> hm = new ConcurrentHashMap();
            for (int i = 0; i < dlpfx.size(); i++) {
                ext_prefix[i] = dlpfx.getString(i);
                System.out.println("prefix:" + ext_prefix[i]);
                //cargar los hashmap por metadataPrefix
                ConcurrentHashMap<String, DataObject> hmpfx = Util.loadMetadataPrefixCollection(extractorDef.getString("name"), ext_prefix[i]);
                hm.put(ext_prefix[i], hmpfx);
                System.out.println("Prefix:" + ext_prefix[i] + " size:" + hmpfx.size());
            }
            //Generar el nuevo DataObject combinado por cada prefix
            try {
                MongoClient client = new MongoClient("localhost", 27017);
                DB db = client.getDB(extractorDef.getString("name").toUpperCase());
                DBCollection objects = db.getCollection("fullobjects");
                HashMap<String, DataObject> hmfull = new HashMap();
                
                for (String pfx : ext_prefix) {
                    ConcurrentHashMap<String, DataObject> hmpfx = hm.get(pfx);
                    Iterator<String> it = hmpfx.keySet().iterator();
                    System.out.println("Processing PREFIX: "+pfx+" num items: "+hmpfx.size());
                    while (it.hasNext()) {
                        String key = it.next();
                        DataObject data = hmpfx.get(key);
                        DataObject dobj = hmfull.get(key);
                        boolean hdrLoaded = false;
                        if (null != data) {
                            if (null == dobj) {
                                dobj = new DataObject();
                                dobj.put("oaiid", key);
                            }
                            DataObject tmpObj = (DataObject) DataObject.parseJSON(data.get("body").toString());
                            if (null != tmpObj) {
                                Object header = tmpObj.get("header");
                                if (null != header) {
                                    dobj.put("header", header);
                                    hdrLoaded = true;
                                }
                                Object metadata = tmpObj.get("metadata");
                                if (null != metadata) {
                                    dobj.put(pfx, metadata);
                                    //System.out.println(metadata.toString());
                                }
                            }
                            for (String pfx2 : ext_prefix) {
                                if (!pfx2.equals(pfx)) {
                                    ConcurrentHashMap<String, DataObject> hmpfx2 = hm.get(pfx2);
                                    DataObject data2 = hmpfx2.get(key);
                                    if (null != data2) {
                                        Object body = data2.get("body");
                                        DataObject tmpObj2 = (DataObject) DataObject.parseJSON(body.toString());
                                        if (null != tmpObj2) {
                                            if (!hdrLoaded) {
                                                Object header2 = tmpObj2.get("header");
                                                if (null != header2) {
                                                    dobj.put("header", header2);
                                                    hdrLoaded = true;
                                                }
                                            }
                                            Object metadata2 = tmpObj2.get("metadata");
                                            if (null != metadata2) {
                                                dobj.put(pfx2, metadata2);
                                                //System.out.println(metadata2.toString());
                                            }
                                        }
                                    }
                                    hmpfx2.remove(key);
                                }
                            }
                            hmfull.put(key, dobj);
                        }
                        
                        
                        
                        boolean isDODeleted = false;
                        
                        //Se tiene que hacer el llamado al proceso de transformación validando si el objeto está eliminado "deleted"
                        DataObject result = mapper.map(dobj);
                        System.out.println(result);
                        //Que se hace con el result??? mandarlo al mapeo e indexación
                        

                        // Esta parte sólo es para verificar como forma los objetos completos.
                        BasicDBObject bjson = Util.toBasicDBObject(dobj);
                        objects.insert(bjson);
                        
                        
                        hmpfx.remove(key);
                    }
                    hm.remove(pfx);
                }

                System.out.println("hm --completo--- size ---" + hmfull.size());
//            Iterator<String> itstr = hmfull.keySet().iterator();
//            while (itstr.hasNext()) {
//                String key = itstr.next();
//                DataObject dobj3 = hmfull.get(key);
//                BasicDBObject bjson = Util.toBasicDBObject(dobj3);
//                objects.insert(bjson);
//            }
            } catch (Exception e) {
                System.out.println("Error al procesar la Base de Datos");
                e.printStackTrace();
            }
        }

    }

}
