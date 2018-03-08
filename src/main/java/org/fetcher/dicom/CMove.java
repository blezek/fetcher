package org.fetcher.dicom;

import org.apache.commons.cli.CommandLine;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.tool.common.CLIUtils;
import org.fetcher.Fetcher;
import org.fetcher.model.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CMove {
    static Logger logger = LoggerFactory.getLogger(CMove.class);

    private Fetcher fetcher;

    private Move move;

    public CMove(Fetcher fetcher, Move move) {
	this.fetcher = fetcher;
	this.move = move;
    }

    public void execute(ResultHandler handler) throws IOException, Exception {

	ArrayList<String> args = new ArrayList<>();
	// -c,--connect <aet@host:port> specify AE Title, remote address
	args.add("--connect");
	args.add(fetcher.getCalledAET() + "@" + fetcher.getHostname() + ":" + fetcher.getCalledPort());

	// -b,--bind <aet[@ip][:port]> specify AE Title, local address
	args.add("--bind");
	args.add(fetcher.getCalledAET());

	// --dest <aet> specifies AE title of the Move
	args.add("--dest");
	args.add(fetcher.getDestinationAET());

	// -L <PATIENT|STUDY|SERIES|IMAGE> specifies retrieve level. Use
	args.add("-L");
	args.add(move.getQueryRetrieveLevel());

	// -m <attr=value>
	// Attributes
	for (Entry<String, String> i : move.getMoveAttributes().entrySet()) {
	    args.add("-m");
	    args.add(i.getKey() + "=" + i.getValue());
	}

	args.add("--soclose-delay");
	args.add("1000");
	logger.info(args.toString());
	CommandLine cl = MoveSCU.parseComandLine(args.toArray(new String[args.size()]));
	MoveSCU main = new MoveSCU();
	CLIUtils.configureConnect(main.remote, main.rq, cl);
	CLIUtils.configureBind(main.conn, main.ae, cl);
	CLIUtils.configure(main.conn, cl);
	main.remote.setTlsProtocols(main.conn.getTlsProtocols());
	main.remote.setTlsCipherSuites(main.conn.getTlsCipherSuites());
	MoveSCU.configureServiceClass(main, cl);
	MoveSCU.configureKeys(main, cl);
	main.setPriority(CLIUtils.priorityOf(cl));
	main.setDestination(MoveSCU.destinationOf(cl));
	ExecutorService executorService = Executors.newSingleThreadExecutor();
	ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
	main.setExecutor(executorService);
	main.setScheduledExecutor(scheduledExecutorService);

	try {
	    main.open();
	    DimseRSPHandler rspHandler = new DimseRSPHandler(main.as.nextMessageID()) {

		@Override
		public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
		    super.onDimseRSP(as, cmd, data);
		    boolean shouldContinue = handler.onResult(cmd);
		    if (!shouldContinue) {
			as.abort();
		    }
		}
	    };
	    main.retrieve(rspHandler);
	} finally {
	    try {
		main.close();
	    } catch (Exception e) {
		// Can safely swallow any exceptions here, because the images are transferred
		logger.warn("Error closing association: " + e.getLocalizedMessage());
	    }
	    executorService.shutdown();
	    scheduledExecutorService.shutdown();
	}

    }

}
