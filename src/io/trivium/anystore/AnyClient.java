package io.trivium.anystore;

import io.trivium.Central;
import io.trivium.anystore.query.Query;
import io.trivium.glue.InfiniObject;
import io.trivium.profile.DataPoints;
import io.trivium.profile.Differential;
import io.trivium.profile.Profiler;
import io.trivium.profile.Ticker;
import javolution.util.FastList;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;

import java.io.File;
import java.io.IOException;

public class AnyClient {

	public static AnyClient INSTANCE = new AnyClient();
	private ExcerptAppender pipeIn;

	public AnyClient() {
		String locPipeIn = Central.getProperty("basePath") + "queues"
				+ File.separator + "anystore" + File.separator + "queueIn";

        IndexedChronicle chronicle = null;
        try {
            chronicle = new IndexedChronicle(locPipeIn);
			pipeIn = chronicle.createAppender();
		} catch (IOException e) {
			Central.logger.error(e);
		}
        //init profiler
        Profiler.INSTANCE.initTicker(new Ticker(DataPoints.ANYSTORE_QUEUE_IN));
        Profiler.INSTANCE.initDifferential(new Differential(DataPoints.ANYSTORE_QUEUE_SIZE));
    }

    public synchronized void storeObject(InfiniObject po) {
        Profiler.INSTANCE.tick(DataPoints.ANYSTORE_QUEUE_IN);
        Profiler.INSTANCE.increment(DataPoints.ANYSTORE_QUEUE_SIZE);
        //pre serialize
        byte[] id = po.getId().toBytes();
        //FIXME implement version
        byte[] typeId = po.getTypeId().getObjectRef().toBytes();
        byte[] metadata = po.getMetadataBinary();
        byte[] data = po.getDataBinary();

        int len = id.length + typeId.length + 4 + metadata.length + 4 + data.length;

        pipeIn.startExcerpt(len);
        pipeIn.write(id);
        pipeIn.write(typeId);
        pipeIn.writeInt(metadata.length);
        pipeIn.write(metadata);
        pipeIn.writeInt(data.length);
        pipeIn.write(data);
        pipeIn.finish();
    }

	public void delete(Query query) {
        AnyServer.INSTANCE.getStore().delete(query);
	}

	public FastList<InfiniObject> loadObjects(Query query) {
		return AnyServer.INSTANCE.getStore().loadObjects(query);
	}
}
