package com.nimble.gateway.service;

import com.nimble.gateway.dto.request.ChargeRequest;
import com.nimble.gateway.dto.response.ChargeResponse;
import com.nimble.gateway.enums.ChargeStatus;

import java.util.List;

public interface ChargeService {

    ChargeResponse create(String originatorCpf, ChargeRequest request);

    List<ChargeResponse> listSent(String originatorCpf, ChargeStatus status);

    List<ChargeResponse> listReceived(String recipientCpf, ChargeStatus status);

    ChargeResponse cancel(Long chargeId, String requesterCpf);
}
