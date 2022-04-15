package com.pseuco.np21.shared;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Validator implementation that concatenates multiple validators.
 * <p>
 * Given a list of validators the {@link CatValidator} forwards all calls to the validators contained within that list.
 */
public class CatValidator<V extends Validator> extends CatRecorder<V> implements Validator {
    /**
     * Constructs a new concatenating validator.
     *
     * @param validators list of validators to forward the calls to
     */
    public CatValidator(List<V> validators) {
        super(validators);
    }

    @Override
    public boolean isRecordingValid() {
        return recorders.stream().allMatch(Validator::isRecordingValid);
    }

    @Override
    public List<String> errors() {
        return recorders.stream()
                .map(Validator::errors)
                .map(List::stream)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty)
                .collect(Collectors.toList());
    }
}
