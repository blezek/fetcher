package org.fetcher.dicom;

import org.apache.commons.cli.CommandLine;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.DimseRSPHandler;
import org.dcm4che3.net.Status;
import org.fetcher.Fetcher;
import org.fetcher.model.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CFind {
  static Logger logger = LoggerFactory.getLogger(CFind.class);

  private Query query;

  private Fetcher fetcher;

  public CFind(Fetcher fetcher, Query query) {
    this.fetcher = fetcher;
    this.query = query;
  }

  public void execute(ResultHandler handler) throws IOException, Exception {
    /*
     * Connection conn = findSCU.getRemoteConnection(); AAssociateRQ rq =
     * findSCU.getAAssociateRQ(); rq.setCalledAET(job.getCalled());
     * rq.setCallingAET(job.getCalling()); conn.setHostname(job.getHostname());
     * conn.setPort(job.getCalledPort());
     * 
     * // from CLIUtils.configure
     * conn.setReceivePDULength(Connection.DEF_MAX_PDU_LENGTH);
     * conn.setSendPDULength(Connection.DEF_MAX_PDU_LENGTH);
     * conn.setMaxOpsInvoked(0); conn.setMaxOpsPerformed(0);
     * 
     * conn.setPackPDV(true); conn.setConnectTimeout(0);
     * conn.setRequestTimeout(0); conn.setAcceptTimeout(0);
     * conn.setReleaseTimeout(0); conn.setResponseTimeout(0);
     * conn.setRetrieveTimeout(0); conn.setIdleTimeout(0);
     * conn.setSocketCloseDelay(Connection.DEF_SOCKETDELAY);
     * conn.setSendBufferSize(0); conn.setReceiveBufferSize(0);
     * conn.setTcpNoDelay(false);
     * 
     * String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian,
     * UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired };
     * 
     * // Configure Service Class
     * findSCU.setInformationModel(InformationModel.valueOf(job.getFetchBy()),
     * IVR_LE_FIRST, EnumSet.allOf(QueryOption.class));
     * 
     * findSCU.addLevel(job.getFetchBy());
     */

    ArrayList<String> args = new ArrayList<>();
    // -c,--connect <aet@host:port> specify AE Title, remote address
    args.add("--connect");
    args.add(fetcher.getCalledAET() + "@" + fetcher.getHostname() + ":" + fetcher.getCalledPort());

    // -b,--bind <aet[@ip][:port]> specify AE Title, local address
    args.add("--bind");
    args.add(fetcher.getCalledAET());

    // -L <PATIENT|STUDY|SERIES|IMAGE> specifies retrieve level. Use
    args.add("-L");
    args.add(fetcher.getFetchBy());

    // Attributes
    for (Entry<String, String> i : query.getQueryAttributes().entrySet()) {
      args.add("-m");
      args.add(i.getKey() + "=" + i.getValue());
    }

    // What we need back
    args.add("-r");
    args.add("StudyInstanceUID");
    args.add("-r");
    args.add("SeriesInstanceUID");
    args.add("-r");
    args.add("NumberOfSeriesRelatedInstances");

    logger.info(args.toString());

    CommandLine cl = FindSCU.parseComandLine(args.toArray(new String[args.size()]));
    FindSCU main = new FindSCU();
    CLIUtils.configureConnect(main.getRemoteConnection(), main.getAAssociateRQ(), cl);
    CLIUtils.configureBind(main.getConnection(), main.getApplicationEntity(), cl);
    CLIUtils.configure(main.getConnection(), cl);
    main.getRemoteConnection().setTlsProtocols(main.getConnection().getTlsProtocols());
    main.getRemoteConnection().setTlsCipherSuites(main.getConnection().getTlsCipherSuites());
    FindSCU.configureServiceClass(main, cl);
    FindSCU.configureKeys(main, cl);
    FindSCU.configureOutput(main, cl);
    FindSCU.configureCancel(main, cl);
    main.setPriority(CLIUtils.priorityOf(cl));
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    main.getDevice().setExecutor(executorService);
    main.getDevice().setScheduledExecutor(scheduledExecutorService);
    try {
      main.open();

      // Call the passed in handler with each dataset
      DimseRSPHandler rspHandler = new DimseRSPHandler(main.getAssociation().nextMessageID()) {
        @Override
        public void onDimseRSP(Association as, Attributes cmd, Attributes data) {
          super.onDimseRSP(as, cmd, data);
          int status = cmd.getInt(Tag.Status, -1);
          if (Status.isPending(status)) {
            handler.onResult(data);
          }
        }
      };
      main.query(main.getKeys(), rspHandler);
    } finally {
      main.close();
      executorService.shutdownNow();
      scheduledExecutorService.shutdownNow();
    }

  }

}
