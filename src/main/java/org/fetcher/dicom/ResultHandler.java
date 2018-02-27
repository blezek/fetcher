package org.fetcher.dicom;

import org.dcm4che3.data.Attributes;

public interface ResultHandler {
  /**
   * handle the result data from the C-MOVE.
   * 
   * @param data
   * @return should the association continue
   */
  boolean onResult(Attributes data);
}
