package io.trivium.anystore;

import io.trivium.Central;
import io.trivium.glue.TriviumObject;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Differential;
import io.trivium.profile.Profiler;
import io.trivium.profile.Ticker;
import net.openhft.chronicle.ExcerptTailer;
import net.openhft.chronicle.IndexedChronicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class AnyServer implements Runnable {

	public static AnyServer INSTANCE = new AnyServer();
    Logger log = LogManager.getLogger(getClass());
	private MapStore store = null;
	
	public void init(){
        store = new MapStore();

        //init profiler
        Profiler.INSTANCE.initTicker(new Ticker(DataPoints.ANYSTORE_QUEUE_OUT));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_QUEUE_SIZE));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_SIZE));
	}

	@Override
	public void run() {
	    log.info("starting anystore server");
	    if(store==null) init();
		String pipeIn = Central.getProperty("basePath")+"queues"+File.separator+"anystore"+File.separator+"queueIn";
		StoreUtils.createIfNotExists(pipeIn);
		IndexedChronicle chronicle=null;
		try {
			chronicle = new IndexedChronicle(pipeIn);
		}catch(Exception ex){
			log.error("cannot init anystore chronicle",ex);
		}
		try {
			ExcerptTailer et = chronicle.createTailer();
			while (true) {
				if (et.nextIndex()) {
                    TriviumObject po = new TriviumObject();
                    byte[] id = new byte[16];
					et.read(id);
					byte[] typeId = new byte[16];
					et.read(typeId);
					int metaSize = et.readInt();
                    byte[] meta = new byte[metaSize];
                    et.read(meta);
                    int dataSize = et.readInt();
                    byte[] data = new byte[dataSize];
                    et.read(data);
                    po.setId(ObjectRef.getInstance(id));
                    //FIXME find correct version
					po.setTypeId(ObjectRef.getInstance(typeId));
                    po.setMetadataBinary(meta);
                    po.setDataBinary(data);

                    store.storeObject(po);
					et.finish();
                    Profiler.INSTANCE.tick(DataPoints.ANYSTORE_QUEUE_OUT);
                    Profiler.INSTANCE.decrement(DataPoints.ANYSTORE_QUEUE_SIZE);
                    Profiler.INSTANCE.increment(DataPoints.ANYSTORE_SIZE);
                } else {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
		} catch (Exception e1) {
			log.error("error while writing to backend",e1);
		} finally{
			try {
				chronicle.close();
			}catch(Exception ex){
				//ignore
			}
		}
	}
	
    public MapStore getStore() {
        if (store == null)
            init();
        return this.store;
    }
}
