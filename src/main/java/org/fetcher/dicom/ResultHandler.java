package org.fetcher.dicom;

import org.dcm4che3.data.Attributes;

public interface ResultHandler {
  void onResult(Attributes data);
}
