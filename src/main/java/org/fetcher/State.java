package org.fetcher;

/**
 * Items are initalized in the CREATED state. They are moved to QUEUED state.
 * When an item is pulled off the queue, it moves to the PENDING state. IF the
 * execution succeeds, it moves to SUCCEEDED or FAILED state.
 * 
 * @author Daniel Blezek <blezek.daniel@mayo.edu>
 *
 */
public enum State {
  CREATED, QUEUED, FAILED, SUCCEEDED, PENDING;
}
