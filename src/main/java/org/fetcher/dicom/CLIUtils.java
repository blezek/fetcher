/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.fetcher.dicom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Priority;
import org.dcm4che3.net.SSLManagerFactory;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.UserIdentityRQ;
import org.dcm4che3.tool.common.DetectEndOfOptionsPosixParser;
import org.dcm4che3.tool.common.FilesetInfo;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class CLIUtils {

  public static ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.common.messages");

  private static String[] IVR_LE_FIRST = { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian,
      UID.ExplicitVRBigEndianRetired };

  private static String[] EVR_LE_FIRST = { UID.ExplicitVRLittleEndian, UID.ExplicitVRBigEndianRetired,
      UID.ImplicitVRLittleEndian };

  private static String[] EVR_BE_FIRST = { UID.ExplicitVRBigEndianRetired, UID.ExplicitVRLittleEndian,
      UID.ImplicitVRLittleEndian };

  private static String[] IVR_LE_ONLY = { UID.ImplicitVRLittleEndian };

  @SuppressWarnings("static-access")
  public static void addAcceptTimeoutOption(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("ms");
    OptionBuilder.withDescription(rb.getString("accept-timeout"));
    OptionBuilder
        .withLongOpt("accept-timeout");
    opts.addOption(OptionBuilder.create(null));
  }

  @SuppressWarnings("static-access")
  public static void addAEOptions(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("length");
    OptionBuilder.withDescription(rb.getString("max-pdulen-rcv"));
    OptionBuilder
        .withLongOpt("max-pdulen-rcv");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("length");
    OptionBuilder.withDescription(rb.getString("max-pdulen-snd"));
    OptionBuilder
        .withLongOpt("max-pdulen-snd");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("no");
    OptionBuilder.withDescription(rb.getString("max-ops-invoked"));
    OptionBuilder
        .withLongOpt("max-ops-invoked");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("no");
    OptionBuilder.withDescription(rb.getString("max-ops-performed"));
    OptionBuilder
        .withLongOpt("max-ops-performed");
    opts.addOption(OptionBuilder.create(null));
    opts.addOption(null, "not-async", false, rb.getString("not-async"));
    opts.addOption(null, "not-pack-pdv", false, rb.getString("not-pack-pdv"));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("ms");
    OptionBuilder.withDescription(rb.getString("idle-timeout"));
    OptionBuilder
        .withLongOpt("idle-timeout");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("ms");
    OptionBuilder.withDescription(rb.getString("release-timeout"));
    OptionBuilder
        .withLongOpt("release-timeout");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("ms");
    OptionBuilder.withDescription(rb.getString("soclose-delay"));
    OptionBuilder
        .withLongOpt("soclose-delay");
    opts.addOption(OptionBuilder.create(null));
    addSocketOptions(opts);
    addTLSOptions(opts);
  }

  public static void addAttributes(Attributes attrs, int[] tags, String... ss) {
    Attributes item = attrs;
    for (int i = 0; i < tags.length - 1; i++) {
      int tag = tags[i];
      Sequence sq = item.getSequence(tag);
      if (sq == null)
        sq = item.newSequence(tag, 1);
      if (sq.isEmpty())
        sq.add(new Attributes());
      item = sq.get(0);
    }
    int tag = tags[tags.length - 1];
    VR vr = ElementDictionary.vrOf(tag, item.getPrivateCreator(tag));
    if (ss.length == 0)
      if (vr == VR.SQ)
        item.newSequence(tag, 1).add(new Attributes(0));
      else
        item.setNull(tag, vr);
    else
      item.setString(tag, vr, ss);
  }

  public static void addAttributes(Attributes attrs, String[] optVals) {
    if (optVals != null)
      for (int i = 1; i < optVals.length; i++, i++)
        addAttributes(attrs, toTags(StringUtils.split(optVals[i - 1], '/')), StringUtils.split(optVals[i], '/'));
  }

  @SuppressWarnings("static-access")
  public static void addBindOption(Options opts, String defAET) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("aet[@ip][:port]");
    OptionBuilder
        .withDescription(MessageFormat.format(rb.getString("bind"), defAET));
    OptionBuilder.withLongOpt("bind");
    opts.addOption(OptionBuilder.create("b"));
  }

  @SuppressWarnings("static-access")
  public static void addBindServerOption(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("[aet[@ip]:]port");
    OptionBuilder.withDescription(rb.getString("bind-server"));
    OptionBuilder
        .withLongOpt("bind");
    opts.addOption(OptionBuilder.create("b"));
    addRequestTimeoutOption(opts);
  }

  public static void addCommonOptions(Options opts) {
    opts.addOption("h", "help", false, rb.getString("help"));
    opts.addOption("V", "version", false, rb.getString("version"));
  }

  @SuppressWarnings("static-access")
  public static void addConnectOption(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("aet@host:port");
    OptionBuilder.withDescription(rb.getString("connect"));
    OptionBuilder
        .withLongOpt("connect");
    opts.addOption(OptionBuilder.create("c"));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("[user:password@]host:port");
    OptionBuilder
        .withDescription(rb.getString("proxy"));
    OptionBuilder.withLongOpt("proxy");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("name");
    OptionBuilder.withDescription(rb.getString("user"));
    OptionBuilder.withLongOpt("user");
    opts.addOption(OptionBuilder
        .create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("password");
    OptionBuilder.withDescription(rb.getString("user-pass"));
    OptionBuilder
        .withLongOpt("user-pass");
    opts.addOption(OptionBuilder.create(null));
    opts.addOption(null, "user-rsp", false, rb.getString("user-rsp"));
    addConnectTimeoutOption(opts);
    addAcceptTimeoutOption(opts);
  }

  @SuppressWarnings("static-access")
  public static void addConnectTimeoutOption(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("ms");
    OptionBuilder.withDescription(rb.getString("connect-timeout"));
    OptionBuilder
        .withLongOpt("connect-timeout");
    opts.addOption(OptionBuilder.create(null));
  }

  public static void addEmptyAttributes(Attributes attrs, String[] optVals) {
    if (optVals != null)
      for (int i = 0; i < optVals.length; i++)
        addAttributes(attrs, toTags(StringUtils.split(optVals[i], '/')));
  }

  @SuppressWarnings("static-access")
  public static void addEncodingOptions(Options opts) {
    opts.addOption(null, "group-len", false, rb.getString("group-len"));
    OptionGroup sqlenGroup = new OptionGroup();
    OptionBuilder.withLongOpt("expl-seq-len");
    OptionBuilder.withDescription(rb.getString("expl-seq-len"));
    sqlenGroup.addOption(
        OptionBuilder.create(null));
    OptionBuilder.withLongOpt("undef-seq-len");
    OptionBuilder.withDescription(rb.getString("undef-seq-len"));
    sqlenGroup.addOption(
        OptionBuilder.create(null));
    opts.addOptionGroup(sqlenGroup);
    OptionGroup itemlenGroup = new OptionGroup();
    OptionBuilder.withLongOpt("expl-item-len");
    OptionBuilder.withDescription(rb.getString("expl-item-len"));
    itemlenGroup.addOption(
        OptionBuilder.create(null));
    OptionBuilder.withLongOpt("undef-item-len");
    OptionBuilder.withDescription(rb.getString("undef-item-len"));
    itemlenGroup.addOption(
        OptionBuilder.create(null));
    opts.addOptionGroup(itemlenGroup);
  }

  @SuppressWarnings("static-access")
  public static void addFilesetInfoOptions(Options opts) {
    OptionBuilder.withLongOpt("fs-desc");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("txtfile");
    OptionBuilder
        .withDescription(rb.getString("fs-desc"));
    opts.addOption(OptionBuilder.create());
    OptionBuilder.withLongOpt("fs-desc-cs");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("code");
    OptionBuilder
        .withDescription(rb.getString("fs-desc-cs"));
    opts.addOption(OptionBuilder.create());
    OptionBuilder.withLongOpt("fs-id");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("id");
    OptionBuilder.withDescription(rb.getString("fs-id"));
    opts.addOption(
        OptionBuilder.create());
    OptionBuilder.withLongOpt("fs-uid");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("uid");
    OptionBuilder
        .withDescription(rb.getString("fs-uid"));
    opts.addOption(OptionBuilder.create());
  }

  @SuppressWarnings("static-access")
  public static void addPriorityOption(Options opts) {
    OptionGroup group = new OptionGroup();
    OptionBuilder.withLongOpt("prior-high");
    OptionBuilder.withDescription(rb.getString("prior-high"));
    group.addOption(OptionBuilder.create());
    OptionBuilder.withLongOpt("prior-low");
    OptionBuilder.withDescription(rb.getString("prior-low"));
    group.addOption(OptionBuilder.create());
    opts.addOptionGroup(group);
  }

  @SuppressWarnings("static-access")
  public static void addRequestTimeoutOption(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("ms");
    OptionBuilder.withDescription(rb.getString("request-timeout"));
    OptionBuilder
        .withLongOpt("request-timeout");
    opts.addOption(OptionBuilder.create(null));
  }

  @SuppressWarnings("static-access")
  public static void addResponseTimeoutOption(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("ms");
    OptionBuilder.withDescription(rb.getString("response-timeout"));
    OptionBuilder
        .withLongOpt("response-timeout");
    opts.addOption(OptionBuilder.create(null));
  }

  @SuppressWarnings("static-access")
  public static void addRetrieveTimeoutOption(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("ms");
    OptionBuilder.withDescription(rb.getString("retrieve-timeout"));
    OptionBuilder
        .withLongOpt("retrieve-timeout");
    opts.addOption(OptionBuilder.create(null));
  }

  @SuppressWarnings("static-access")
  public static void addSocketOptions(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("length");
    OptionBuilder.withDescription(rb.getString("sosnd-buffer"));
    OptionBuilder
        .withLongOpt("sosnd-buffer");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("length");
    OptionBuilder.withDescription(rb.getString("sorcv-buffer"));
    OptionBuilder
        .withLongOpt("sorcv-buffer");
    opts.addOption(OptionBuilder.create(null));
    opts.addOption(null, "tcp-delay", false, rb.getString("tcp-delay"));
  }

  public static void addTLSCipherOptions(Options opts) {
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("cipher");
    OptionBuilder.withDescription(rb.getString("tls-cipher"));
    OptionBuilder
        .withLongOpt("tls-cipher");
    opts.addOption(OptionBuilder.create(null));
    opts.addOption(null, "tls", false, rb.getString("tls"));
    opts.addOption(null, "tls-null", false, rb.getString("tls-null"));
    opts.addOption(null, "tls-3des", false, rb.getString("tls-3des"));
    opts.addOption(null, "tls-aes", false, rb.getString("tls-aes"));
  }

  @SuppressWarnings("static-access")
  public static void addTLSOptions(Options opts) {
    addTLSCipherOptions(opts);
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("protocol");
    OptionBuilder.withDescription(rb.getString("tls-protocol"));
    OptionBuilder
        .withLongOpt("tls-protocol");
    opts.addOption(OptionBuilder.create(null));
    opts.addOption(null, "tls1", false, rb.getString("tls1"));
    opts.addOption(null, "tls11", false, rb.getString("tls11"));
    opts.addOption(null, "tls12", false, rb.getString("tls12"));
    opts.addOption(null, "ssl3", false, rb.getString("ssl3"));
    opts.addOption(null, "ssl2Hello", false, rb.getString("ssl2Hello"));
    opts.addOption(null, "tls-noauth", false, rb.getString("tls-noauth"));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("file|url");
    OptionBuilder.withDescription(rb.getString("key-store"));
    OptionBuilder
        .withLongOpt("key-store");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("storetype");
    OptionBuilder.withDescription(rb.getString("key-store-type"));
    OptionBuilder
        .withLongOpt("key-store-type");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("password");
    OptionBuilder.withDescription(rb.getString("key-store-pass"));
    OptionBuilder
        .withLongOpt("key-store-pass");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("password");
    OptionBuilder.withDescription(rb.getString("key-pass"));
    OptionBuilder
        .withLongOpt("key-pass");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("file|url");
    OptionBuilder.withDescription(rb.getString("trust-store"));
    OptionBuilder
        .withLongOpt("trust-store");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("storetype");
    OptionBuilder.withDescription(rb.getString("trust-store-type"));
    OptionBuilder
        .withLongOpt("trust-store-type");
    opts.addOption(OptionBuilder.create(null));
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("password");
    OptionBuilder.withDescription(rb.getString("trust-store-pass"));
    OptionBuilder
        .withLongOpt("trust-store-pass");
    opts.addOption(OptionBuilder.create(null));
  }

  public static void addTransferSyntaxOptions(Options opts) {
    OptionGroup group = new OptionGroup();
    // group.addOption(OptionBuilder.withLongOpt("explicit-vr").withDescription(rb.getString("explicit-vr")).create());
    // group.addOption(OptionBuilder.withLongOpt("big-endian").withDescription(rb.getString("big-endian")).create());
    // group.addOption(OptionBuilder.withLongOpt("implicit-vr").withDescription(rb.getString("implicit-vr")).create());
    opts.addOptionGroup(group);
  }

  public static void configure(Connection conn, CommandLine cl) throws ParseException, IOException {
    conn.setReceivePDULength(getIntOption(cl, "max-pdulen-rcv", Connection.DEF_MAX_PDU_LENGTH));
    conn.setSendPDULength(getIntOption(cl, "max-pdulen-snd", Connection.DEF_MAX_PDU_LENGTH));
    if (cl.hasOption("not-async")) {
      conn.setMaxOpsInvoked(1);
      conn.setMaxOpsPerformed(1);
    } else {
      conn.setMaxOpsInvoked(getIntOption(cl, "max-ops-invoked", 0));
      conn.setMaxOpsPerformed(getIntOption(cl, "max-ops-performed", 0));
    }
    conn.setPackPDV(!cl.hasOption("not-pack-pdv"));
    conn.setConnectTimeout(getIntOption(cl, "connect-timeout", 0));
    conn.setRequestTimeout(getIntOption(cl, "request-timeout", 0));
    conn.setAcceptTimeout(getIntOption(cl, "accept-timeout", 0));
    conn.setReleaseTimeout(getIntOption(cl, "release-timeout", 0));
    conn.setResponseTimeout(getIntOption(cl, "response-timeout", 0));
    conn.setRetrieveTimeout(getIntOption(cl, "retrieve-timeout", 0));
    conn.setIdleTimeout(getIntOption(cl, "idle-timeout", 0));
    conn.setSocketCloseDelay(getIntOption(cl, "soclose-delay", Connection.DEF_SOCKETDELAY));
    conn.setSendBufferSize(getIntOption(cl, "sosnd-buffer", 0));
    conn.setReceiveBufferSize(getIntOption(cl, "sorcv-buffer", 0));
    conn.setTcpNoDelay(!cl.hasOption("tcp-delay"));
    configureTLS(conn, cl);
  }

  public static void configure(FilesetInfo fsInfo, CommandLine cl) {
    fsInfo.setFilesetUID(cl.getOptionValue("fs-uid"));
    fsInfo.setFilesetID(cl.getOptionValue("fs-id"));
    if (cl.hasOption("fs-desc"))
      fsInfo.setDescriptorFile(new File(cl.getOptionValue("fs-desc")));
    fsInfo.setDescriptorFileCharset(cl.getOptionValue("fs-desc-cs"));
  }

  public static void configureBind(Connection conn, ApplicationEntity ae, CommandLine cl) throws ParseException {
    if (cl.hasOption("b")) {
      String aeAtHostPort = cl.getOptionValue("b");
      String[] aeAtHostAndPort = split(aeAtHostPort, ':', 0);
      String[] aeHost = split(aeAtHostAndPort[0], '@', 0);
      ae.setAETitle(aeHost[0]);
      if (aeHost[1] != null)
        conn.setHostname(aeHost[1]);
      if (aeAtHostAndPort[1] != null)
        conn.setPort(Integer.parseInt(aeAtHostAndPort[1]));
    }
  }

  public static void configureBindServer(Connection conn, ApplicationEntity ae, CommandLine cl) throws ParseException {
    if (!cl.hasOption("b"))
      throw new MissingOptionException(rb.getString("missing-bind-opt"));
    String aeAtHostPort = cl.getOptionValue("b");
    String[] aeAtHostAndPort = split(aeAtHostPort, ':', 1);
    conn.setPort(Integer.parseInt(aeAtHostAndPort[1]));
    if (aeAtHostAndPort[0] != null) {
      String[] aeHost = split(aeAtHostAndPort[0], '@', 0);
      ae.setAETitle(aeHost[0]);
      if (aeHost[1] != null)
        conn.setHostname(aeHost[1]);
    }
  }

  public static void configureConnect(Connection conn, AAssociateRQ rq, CommandLine cl) throws ParseException {
    if (!cl.hasOption("c"))
      throw new MissingOptionException(rb.getString("missing-connect-opt"));
    String aeAtHostPort = cl.getOptionValue("c");
    String[] aeHostPort = split(aeAtHostPort, '@', 0);
    if (aeHostPort[1] == null)
      throw new ParseException(rb.getString("invalid-connect-opt"));

    String[] hostPort = split(aeHostPort[1], ':', 0);
    if (hostPort[1] == null)
      throw new ParseException(rb.getString("invalid-connect-opt"));

    rq.setCalledAET(aeHostPort[0]);
    conn.setHostname(hostPort[0]);
    conn.setPort(Integer.parseInt(hostPort[1]));

    conn.setHttpProxy(cl.getOptionValue("proxy"));

    if (cl.hasOption("user"))
      rq.setUserIdentityRQ(cl.hasOption("user-pass")
          ? new UserIdentityRQ(cl.getOptionValue("user"), cl.getOptionValue("user-pass").toCharArray())
          : new UserIdentityRQ(cl.getOptionValue("user"), cl.hasOption("user-rsp")));
  }

  private static void configureTLS(Connection conn, CommandLine cl) throws ParseException, IOException {
    if (!configureTLSCipher(conn, cl))
      return;

    if (cl.hasOption("tls12"))
      conn.setTlsProtocols("TLSv1.2");
    else if (cl.hasOption("tls11"))
      conn.setTlsProtocols("TLSv1.1");
    else if (cl.hasOption("tls1"))
      conn.setTlsProtocols("TLSv1");
    else if (cl.hasOption("ssl3"))
      conn.setTlsProtocols("SSLv3");
    else if (cl.hasOption("ssl2Hello"))
      conn.setTlsProtocols("SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2");
    else if (cl.hasOption("tls-protocol"))
      conn.setTlsProtocols(cl.getOptionValues("tls-protocol"));

    conn.setTlsNeedClientAuth(!cl.hasOption("tls-noauth"));

    String keyStoreURL = cl.getOptionValue("key-store", "resource:key.jks");
    String keyStoreType = cl.getOptionValue("key-store-type", "JKS");
    String keyStorePass = cl.getOptionValue("key-store-pass", "secret");
    String keyPass = cl.getOptionValue("key-pass", keyStorePass);
    String trustStoreURL = cl.getOptionValue("trust-store", "resource:cacerts.jks");
    String trustStoreType = cl.getOptionValue("trust-store-type", "JKS");
    String trustStorePass = cl.getOptionValue("trust-store-pass", "secret");

    Device device = conn.getDevice();
    try {
      device.setKeyManager(SSLManagerFactory.createKeyManager(keyStoreType, keyStoreURL, keyStorePass, keyPass));
      device.setTrustManager(SSLManagerFactory.createTrustManager(trustStoreType, trustStoreURL, trustStorePass));
    } catch (GeneralSecurityException e) {
      throw new IOException(e);
    }
  }

  public static boolean configureTLSCipher(Connection conn, CommandLine cl) throws ParseException {
    if (cl.hasOption("tls"))
      conn.setTlsCipherSuites("SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    else if (cl.hasOption("tls-null"))
      conn.setTlsCipherSuites("SSL_RSA_WITH_NULL_SHA");
    else if (cl.hasOption("tls-3des"))
      conn.setTlsCipherSuites("SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    else if (cl.hasOption("tls-aes"))
      conn.setTlsCipherSuites("TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA");
    else if (cl.hasOption("tls-cipher"))
      conn.setTlsCipherSuites(cl.getOptionValues("tls-cipher"));

    return conn.isTls();
  }

  public static DicomEncodingOptions encodingOptionsOf(CommandLine cl) throws ParseException {
    if (cl.hasOption("expl-item-len") && cl.hasOption("undef-item-len")
        || cl.hasOption("expl-seq-len") && cl.hasOption("undef-seq-len"))
      throw new ParseException(rb.getString("conflicting-enc-opts"));
    return new DicomEncodingOptions(cl.hasOption("group-len"), !cl.hasOption("expl-seq-len"),
        cl.hasOption("undef-seq-len"), !cl.hasOption("expl-item-len"), cl.hasOption("undef-item-len"));
  }

  public static int getIntOption(CommandLine cl, String opt, int defVal) {
    String optVal = cl.getOptionValue(opt);
    if (optVal == null)
      return defVal;

    return optVal.endsWith("H") ? Integer.parseInt(optVal.substring(0, optVal.length() - 1), 16)
        : Integer.parseInt(optVal);
  }

  public static Properties loadProperties(String url, Properties p) throws IOException {
    if (p == null)
      p = new Properties();
    InputStream in = StreamUtils.openFileOrURL(url);
    try {
      p.load(in);
    } finally {
      SafeClose.close(in);
    }
    return p;
  }

  public static CommandLine parseComandLine(String[] args, Options opts, ResourceBundle rb2, Class<?> clazz)
      throws ParseException {
    CommandLineParser parser = new DetectEndOfOptionsPosixParser();
    CommandLine cl = parser.parse(opts, args);
    if (cl.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(rb2.getString("usage"), rb2.getString("description"), opts, rb2.getString("example"));
      System.exit(0);
    }
    if (cl.hasOption("V")) {
      Package p = clazz.getPackage();
      String s = p.getName();
      System.out.println(s.substring(s.lastIndexOf('.') + 1) + ": " + p.getImplementationVersion());
      System.exit(0);
    }
    return cl;
  }

  public static int priorityOf(CommandLine cl) {
    return cl.hasOption("prior-high") ? Priority.HIGH : cl.hasOption("prior-low") ? Priority.LOW : Priority.NORMAL;
  }

  private static String[] split(String s, char delim, int defPos) {
    String[] s2 = new String[2];
    int pos = s.indexOf(delim);
    if (pos != -1) {
      s2[0] = s.substring(0, pos);
      s2[1] = s.substring(pos + 1);
    } else {
      s2[defPos] = s;
    }
    return s2;
  }

  public static int toTag(String tagOrKeyword) {
    try {
      return Integer.parseInt(tagOrKeyword, 16);
    } catch (IllegalArgumentException e) {
      int tag = ElementDictionary.tagForKeyword(tagOrKeyword, null);
      if (tag == -1)
        throw new IllegalArgumentException(tagOrKeyword);
      return tag;
    }
  }

  public static int[] toTags(String[] tagOrKeywords) {
    int[] tags = new int[tagOrKeywords.length];
    for (int i = 0; i < tags.length; i++)
      tags[i] = toTag(tagOrKeywords[i]);
    return tags;
  }

  public static String toUID(String uid) {
    uid = uid.trim();
    return (uid.equals("*") || Character.isDigit(uid.charAt(0))) ? uid : UID.forName(uid);
  }

  public static String[] toUIDs(String s) {
    if (s.equals("*"))
      return new String[] { "*" };

    String[] uids = StringUtils.split(s, ',');
    for (int i = 0; i < uids.length; i++)
      uids[i] = toUID(uids[i]);
    return uids;
  }

  public static String[] transferSyntaxesOf(CommandLine cl) {
    if (cl.hasOption("explicit-vr"))
      return EVR_LE_FIRST;
    if (cl.hasOption("big-endian"))
      return EVR_BE_FIRST;
    if (cl.hasOption("implicit-vr"))
      return IVR_LE_ONLY;
    return IVR_LE_FIRST;
  }

  public static boolean updateAttributes(Attributes data, Attributes attrs, String uidSuffix) {
    if (attrs.isEmpty() && uidSuffix == null)
      return false;
    if (uidSuffix != null) {
      data.setString(Tag.StudyInstanceUID, VR.UI, data.getString(Tag.StudyInstanceUID) + uidSuffix);
      data.setString(Tag.SeriesInstanceUID, VR.UI, data.getString(Tag.SeriesInstanceUID) + uidSuffix);
      data.setString(Tag.SOPInstanceUID, VR.UI, data.getString(Tag.SOPInstanceUID) + uidSuffix);
    }
    data.update(Attributes.UpdatePolicy.OVERWRITE, attrs, null);
    return true;
  }
}