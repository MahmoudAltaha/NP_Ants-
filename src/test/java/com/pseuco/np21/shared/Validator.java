package com.pseuco.np21.shared;

import java.util.List;

/**
 * A {@link Validator} is a {@link Recorder} extension that can check whether a recording is valid.
 */
public interface Validator extends Recorder {
    /**
     * Check whether the recording is valid.
     *
     * @return {@code true} iff the recording is valid
     */
    boolean isRecordingValid();

    /**
     * Get list of errors encountered during recording.
     *
     * @return list of errors encountered during recording
     */
    List<String> errors();
}
